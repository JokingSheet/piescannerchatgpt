package dev.pie.scanner.scanner;

/**
 * Ein ausgelesener Messwert. percent/globalPercent = Zeitanteil im Profiler (KEINE Block-Anzahl!).
 * count = Profiler-Eintritte der Sektion (nur >0 wenn der Profiler sie liefert; KEINE Block-Anzahl).
 */
public record Reading(Category category, String nodeName, String path,
                      double percent, double globalPercent, long count) {}
