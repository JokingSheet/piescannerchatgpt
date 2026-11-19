package dev.pie.scanner.scanner;

import net.minecraft.util.profiling.ProfileResults;

/** Haelt die zuletzt vom Pie-Chart abgefragten Profiler-Ergebnisse. */
public final class PieDataSource {
    private static volatile ProfileResults latest;
    private static volatile long lastOfferMs;
    private static volatile boolean internal;

    private PieDataSource() {}

    public static void offer(ProfileResults r) {
        if (internal) return; // eigene Scanner-Abfragen zaehlen nicht als "Pie-Chart ist aktiv"
        latest = r;
        lastOfferMs = System.currentTimeMillis();
    }

    public static ProfileResults latest() { return latest; }

    public static long ageMs() { return lastOfferMs == 0 ? Long.MAX_VALUE : System.currentTimeMillis() - lastOfferMs; }

    static void setInternal(boolean v) { internal = v; }

    public static void reset() { latest = null; lastOfferMs = 0; }
}
