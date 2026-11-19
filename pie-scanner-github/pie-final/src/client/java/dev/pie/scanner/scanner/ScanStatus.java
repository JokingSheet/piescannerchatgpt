package dev.pie.scanner.scanner;

public enum ScanStatus {
    OK("Daten ausgelesen"),
    PARTIAL("Daten teilweise verfuegbar"),
    NO_DATA("Keine Daten verfuegbar"),
    SUSPICIOUS("Verdaechtige Werte erkannt");

    public final String text;
    ScanStatus(String t) { text = t; }
}
