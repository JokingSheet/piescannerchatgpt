# Pie Chart Scanner & Alert System (Fabric, MC 1.21.11, client-only)

Liest die F3-Pie-Charts (Shift+F3) aus und warnt bei auffaelligen Kategorien (Chests, Hopper, Shulker ...).
Zeigt nur Zeitanteile/Profiler-Aufrufe - keine Block-Anzahlen.

Build: GitHub Actions (Tab "Actions" -> letzter Lauf -> Artifact "Artifacts") oder lokal `./gradlew build` (JDK 25).
JAR: build/libs/pie-scanner-1.0.0.jar (NICHT -sources). Zusammen mit Fabric API in .minecraft/mods.

Befehle: /piescan (Config) | /piescan test | /piescan dump | /piescan toggle | /piescan reset
