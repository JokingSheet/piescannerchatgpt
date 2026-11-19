package dev.pie.scanner.hud;

import dev.pie.scanner.PieScannerMod;
import dev.pie.scanner.alert.AlertManager;
import dev.pie.scanner.config.PieConfig;
import dev.pie.scanner.scanner.PieScanner;
import dev.pie.scanner.scanner.Reading;
import dev.pie.scanner.scanner.ScanStatus;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;

public final class HudRenderer {
    private HudRenderer() {}

    public static void render(GuiGraphics g) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.options.hideGui) return;
        PieConfig cfg = PieScannerMod.config();
        PieScanner sc = PieScannerMod.scanner();
        AlertManager al = PieScannerMod.alerts();
        long now = System.currentTimeMillis();

        if (cfg.hudEnabled) drawPanel(g, mc, cfg, sc, al, now);
        drawBanner(g, mc, al, now);
    }

    private static void drawPanel(GuiGraphics g, Minecraft mc, PieConfig cfg, PieScanner sc, AlertManager al, long now) {
        List<String> lines = new ArrayList<>();
        List<Integer> colors = new ArrayList<>();
        lines.add("Pie Scanner: " + (cfg.scannerEnabled ? "ON" : "OFF")); colors.add(cfg.scannerEnabled ? 0xFF55FF55 : 0xFFFF5555);
        if (cfg.scannerEnabled) {
            ScanStatus s = sc.status;
            lines.add("Status: " + s.text); colors.add(switch (s) {
                case OK -> 0xFF55FF55; case PARTIAL -> 0xFFFFFF55; case NO_DATA -> 0xFFAAAAAA; case SUSPICIOUS -> 0xFFFF5555; });
            lines.add(sc.statusDetail); colors.add(0xFFAAAAAA);
            Reading top = sc.top;
            lines.add("Analysiert (Top): " + (top == null ? "keine" : top.category().label())); colors.add(0xFFFFFFFF);
            List<Reading> rs = sc.readings;
            if (rs.isEmpty()) { lines.add("Messwerte: nicht verfuegbar"); colors.add(0xFFAAAAAA); }
            else {
                lines.add("Messwerte (Zeitanteil):"); colors.add(0xFFFFFFFF);
                for (int i = 0; i < Math.min(6, rs.size()); i++) {
                    Reading r = rs.get(i);
                    boolean sus = sc.suspicious.contains(r.category().key());
                    lines.add(String.format(Locale.ROOT, " %s: %.2f%%%s", r.category().label(), r.globalPercent(),
                        r.count() > 0 ? " (n=" + r.count() + ")" : ""));
                    colors.add(sus ? 0xFFFF5555 : 0xFFDDDDDD);
                }
            }
            lines.add("Verdaechtig: " + sc.suspicious.size()); colors.add(sc.suspicious.isEmpty() ? 0xFFFFFFFF : 0xFFFF5555);
        }
        lines.add("Letzter Alarm: " + (al.lastAlertMs == 0 ? "-" : al.lastAlertText + " vor " + (now - al.lastAlertMs) / 1000 + "s")); colors.add(0xFFFFFFFF);
        long cd = al.cooldownRemainingMs(now);
        lines.add("Cooldown: " + (cd > 0 ? (cd / 1000 + 1) + "s" : "bereit") + " / " + cfg.cooldownSeconds + "s"); colors.add(0xFFFFFFFF);

        int w = 0;
        for (String l : lines) w = Math.max(w, mc.font.width(l));
        int x = g.guiWidth() * cfg.hudXPercent / 100;
        int y = g.guiHeight() * cfg.hudYPercent / 100;
        int h = lines.size() * 10 + 6;
        g.fill(x, y, x + w + 8, y + h, 0xA0000000);
        for (int i = 0; i < lines.size(); i++) g.drawString(mc.font, lines.get(i), x + 4, y + 4 + i * 10, colors.get(i), true);
    }

    private static void drawBanner(GuiGraphics g, Minecraft mc, AlertManager al, long now) {
        String[] b = al.bannerLines;
        if (b == null || now > al.bannerUntilMs) return;
        boolean blink = (now / 300) % 2 == 0;
        int w = 0;
        for (String l : b) w = Math.max(w, mc.font.width(l));
        int x = (g.guiWidth() - w) / 2 - 8, y = 20, h = b.length * 11 + 10;
        g.fill(x, y, x + w + 16, y + h, blink ? 0xD0AA0000 : 0xD0550000);
        for (int i = 0; i < b.length; i++) {
            String l = b[i];
            g.drawString(mc.font, l, (g.guiWidth() - mc.font.width(l)) / 2, y + 5 + i * 11, 0xFFFFFFFF, true);
        }
    }
}
