package dev.pie.scanner.scanner;

import dev.pie.scanner.alert.AlertManager;
import dev.pie.scanner.config.PieConfig;
import java.util.*;
import net.minecraft.client.Minecraft;
import net.minecraft.util.profiling.ProfileResults;
import net.minecraft.util.profiling.ResultField;

/** Liest den Profiler-Baum, ordnet Knoten Kategorien zu, vergleicht mit Verlauf (EMA) und meldet Auffaelligkeiten. */
public final class PieScanner {
    private static final char SEP = '\u001e';
    private static final int MAX_DEPTH = 14;
    private static final int MAX_NODES = 5000;
    private static final long STALE_MS = 3000;
    private static final int WARMUP = 5;

    private static final class Baseline { double ema; int samples; }

    private final Map<String, Baseline> baselines = new HashMap<>();
    private final Set<String> seenEver = new HashSet<>();
    private ProfileResults lastAnalyzed;

    // --- Zustand fuer HUD ---
    public volatile ScanStatus status = ScanStatus.NO_DATA;
    public volatile String statusDetail = "Pie-Chart nicht sichtbar (Shift+F3)";
    public volatile List<Reading> readings = List.of();
    public volatile Set<String> suspicious = Set.of();
    public volatile int nodesRead;
    public volatile Reading top;
    public volatile int enabledCount, visibleCount;

    public void tick(Minecraft mc, PieConfig cfg, AlertManager alerts) {
        if (!cfg.scannerEnabled) return;
        ProfileResults res = PieDataSource.latest();
        if (res == null || PieDataSource.ageMs() > STALE_MS) {
            status = ScanStatus.NO_DATA;
            statusDetail = "Pie-Chart nicht sichtbar oder keine Ergebnisse (Shift+F3)";
            readings = List.of(); suspicious = Set.of(); top = null; nodesRead = 0; lastAnalyzed = null;
            return;
        }
        if (res == lastAnalyzed) return; // diese Messung wurde schon ausgewertet
        lastAnalyzed = res;

        List<Node> nodes = new ArrayList<>();
        PieDataSource.setInternal(true);
        try { walk(res, "root", 0, nodes); }
        catch (Throwable t) { // z.B. Strukturaenderung -> ehrlich melden statt raten
            status = ScanStatus.NO_DATA; statusDetail = "Lesefehler: " + t.getClass().getSimpleName();
            return;
        } finally { PieDataSource.setInternal(false); }

        nodesRead = nodes.size();
        if (nodes.isEmpty()) {
            status = ScanStatus.NO_DATA; statusDetail = "Pie-Chart enthaelt keine Eintraege";
            readings = List.of(); suspicious = Set.of(); top = null; return;
        }

        List<Reading> out = new ArrayList<>();
        Set<String> susp = new LinkedHashSet<>();
        int enabled = 0, visible = 0;
        long now = System.currentTimeMillis();

        for (Category cat : Category.ALL) {
            if (cfg.disabledCategories.contains(cat.key())) continue;
            enabled++;
            Node best = null;
            for (Node n : nodes) if (cat.matches(n.name) && (best == null || n.global > best.global)) best = n;
            if (best == null) continue; // nicht sichtbar: kein Wert, KEIN erfundener Wert
            Baseline b = baselines.computeIfAbsent(cat.key(), k -> new Baseline());
            visible++;
            seenEver.add(cat.key());
            Reading r = new Reading(cat, best.name, best.path, best.percent, best.global, best.count);
            out.add(r);

            boolean warm = b.samples >= WARMUP;
            int s = cfg.sensitivity; // 1 (unempfindlich) .. 10 (empfindlich)
            double hard = 6.0 - 0.5 * s;
            double ratio = 2.6 - 0.15 * s;
            double minDelta = 0.5 - 0.04 * s;
            boolean isSusp = r.globalPercent() >= hard
                || (warm && r.globalPercent() > b.ema * ratio && r.globalPercent() - b.ema >= minDelta);
            double alpha = isSusp ? 0.05 : 0.2;
            b.ema = b.samples == 0 ? r.globalPercent() : b.ema + alpha * (r.globalPercent() - b.ema);
            b.samples++;
            if (isSusp) {
                susp.add(cat.key());
                alerts.trigger(mc, cfg, r, b.ema, now);
            }
        }

        out.sort(Comparator.comparingDouble(Reading::globalPercent).reversed());
        readings = out; suspicious = susp; top = out.isEmpty() ? null : out.get(0);
        enabledCount = enabled; visibleCount = visible;

        if (!susp.isEmpty()) { status = ScanStatus.SUSPICIOUS; statusDetail = susp.size() + " Kategorie(n) auffaellig"; }
        else if (visible < enabled) { status = ScanStatus.PARTIAL; statusDetail = visible + "/" + enabled + " Kategorien im Chart sichtbar"; }
        else { status = ScanStatus.OK; statusDetail = "alle aktivierten Kategorien sichtbar"; }
    }

    /** Fuer /piescan dump: alle gelesenen Knotennamen (zeigt ehrlich, was das Pie-Chart wirklich hergibt). */
    public List<String> dumpNames(int limit) {
        ProfileResults res = PieDataSource.latest();
        if (res == null) return List.of();
        List<Node> nodes = new ArrayList<>();
        PieDataSource.setInternal(true);
        try { walk(res, "root", 0, nodes); } catch (Throwable ignored) {} finally { PieDataSource.setInternal(false); }
        nodes.sort(Comparator.comparingDouble((Node n) -> n.global).reversed());
        List<String> lines = new ArrayList<>();
        Set<String> seen = new HashSet<>();
        for (Node n : nodes) {
            if (!seen.add(n.name)) continue;
            lines.add(String.format(Locale.ROOT, "%s  %.2f%%  (n=%d)  %s", n.name, n.global, n.count, n.path.replace(SEP, '>')));
            if (lines.size() >= limit) break;
        }
        return lines;
    }

    public boolean everSeen(String key) { return seenEver.contains(key); }

    public void resetBaselines() { baselines.clear(); seenEver.clear(); lastAnalyzed = null; }

    private void walk(ProfileResults res, String path, int depth, List<Node> out) {
        if (depth > MAX_DEPTH || out.size() > MAX_NODES) return;
        List<ResultField> list = res.getTimes(path);
        if (list == null || list.isEmpty()) return;
        int start = ResultAccess.name(list.get(0)).equals(lastSegment(path)) ? 1 : 0; // erster Eintrag = Elternknoten
        for (int i = start; i < list.size(); i++) {
            ResultField f = list.get(i);
            String name = ResultAccess.name(f);
            String childPath = path + SEP + name;
            out.add(new Node(name, childPath, ResultAccess.percent(f), ResultAccess.globalPercent(f), ResultAccess.count(f)));
            walk(res, childPath, depth + 1, out);
        }
    }

    private static String lastSegment(String path) {
        int i = path.lastIndexOf(SEP);
        return i < 0 ? path : path.substring(i + 1);
    }

    private record Node(String name, String path, double percent, double global, long count) {}
}
