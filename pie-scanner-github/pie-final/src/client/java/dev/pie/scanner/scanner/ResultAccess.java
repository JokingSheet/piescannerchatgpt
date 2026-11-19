package dev.pie.scanner.scanner;

import net.minecraft.util.profiling.ResultField;

/** Einziger Ort, der direkt auf ResultField zugreift (falls Mojang Felder/Accessoren aendert, nur hier anpassen). */
final class ResultAccess {
    private ResultAccess() {}
    static String name(ResultField f) { return f.name; }
    static double percent(ResultField f) { return f.percentage; }
    static double globalPercent(ResultField f) { return f.globalPercentage; }
    static long count(ResultField f) { return f.count; }
}
