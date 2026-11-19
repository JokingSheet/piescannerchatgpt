package dev.pie.scanner.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;
import net.fabricmc.loader.api.FabricLoader;

public final class PieConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static Path file() { return FabricLoader.getInstance().getConfigDir().resolve("pie_scanner.json"); }

    public boolean scannerEnabled = true;
    public boolean soundEnabled = true;
    public boolean chatEnabled = true;
    public boolean bannerEnabled = true;
    public boolean hudEnabled = true;
    public int sensitivity = 5;        // 1..10
    public int cooldownSeconds = 30;   // 5..300
    public int soundVolumePercent = 100;
    public AlarmSound alarmSound = AlarmSound.PLING;
    public int hudXPercent = 1;        // Position in % der Bildschirmbreite
    public int hudYPercent = 20;       // Position in % der Bildschirmhoehe
    public Set<String> disabledCategories = new HashSet<>();

    public static PieConfig load() {
        try {
            if (Files.exists(file())) {
                PieConfig c = GSON.fromJson(Files.readString(file()), PieConfig.class);
                if (c != null) { c.sanitize(); return c; }
            }
        } catch (Exception e) { System.err.println("[PieScanner] Config defekt, nutze Defaults: " + e); }
        return new PieConfig();
    }

    public void save() {
        try { Files.writeString(file(), GSON.toJson(this)); }
        catch (IOException e) { System.err.println("[PieScanner] Config nicht speicherbar: " + e); }
    }

    private void sanitize() {
        sensitivity = Math.max(1, Math.min(10, sensitivity));
        cooldownSeconds = Math.max(5, Math.min(300, cooldownSeconds));
        soundVolumePercent = Math.max(0, Math.min(100, soundVolumePercent));
        hudXPercent = Math.max(0, Math.min(95, hudXPercent));
        hudYPercent = Math.max(0, Math.min(95, hudYPercent));
        if (alarmSound == null) alarmSound = AlarmSound.PLING;
        if (disabledCategories == null) disabledCategories = new HashSet<>();
    }
}
