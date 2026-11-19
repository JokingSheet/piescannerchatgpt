package dev.pie.scanner.alert;

import dev.pie.scanner.config.PieConfig;
import dev.pie.scanner.scanner.Reading;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;

public final class AlertManager {
    private final Map<String, Long> lastByCategory = new HashMap<>();
    public volatile String lastAlertText = "-";
    public volatile long lastAlertMs = 0;
    public volatile long lastCooldownMs = 0;
    public volatile String[] bannerLines = null;
    public volatile long bannerUntilMs = 0;

    /** Loest einen Alarm aus, ausser die Kategorie ist noch im Cooldown. */
    public void trigger(Minecraft mc, PieConfig cfg, Reading r, double baseline, long now) {
        long cooldownMs = cfg.cooldownSeconds * 1000L;
        Long last = lastByCategory.get(r.category().key());
        if (last != null && now - last < cooldownMs) return;
        lastByCategory.put(r.category().key(), now);
        fire(mc, cfg, "minecraft:" + r.category().key(), r.nodeName(),
            String.format(Locale.ROOT, "%.2f%% Zeitanteil (Basis %.2f%%)%s", r.globalPercent(), baseline,
                r.count() > 0 ? ", " + r.count() + " Profiler-Aufrufe" : ""), now, cooldownMs, false);
    }

    /** Klar gekennzeichneter Testalarm (/piescan test) - KEINE Erkennung. */
    public void test(Minecraft mc, PieConfig cfg) {
        long now = System.currentTimeMillis();
        fire(mc, cfg, "TEST", "-", "kein echter Messwert (Testalarm)", now, cfg.cooldownSeconds * 1000L, true);
    }

    private void fire(Minecraft mc, PieConfig cfg, String category, String node, String value,
                      long now, long cooldownMs, boolean test) {
        lastAlertText = category + (test ? "" : " (" + node + ")");
        lastAlertMs = now; lastCooldownMs = cooldownMs;
        String head = test ? "[PIE ALERT TEST] Testalarm" : "[PIE ALERT] Verdaechtige Aktivitaet erkannt!";
        String status = test ? "Test" : "Auffaellig";

        if (cfg.chatEnabled && mc.player != null) {
            mc.player.displayClientMessage(Component.literal(head).withStyle(ChatFormatting.RED, ChatFormatting.BOLD), false);
            mc.player.displayClientMessage(Component.literal("Kategorie: " + category).withStyle(ChatFormatting.YELLOW), false);
            mc.player.displayClientMessage(Component.literal("Messwert: " + value).withStyle(ChatFormatting.YELLOW), false);
            mc.player.displayClientMessage(Component.literal("Status: " + status).withStyle(ChatFormatting.YELLOW), false);
        }
        if (cfg.bannerEnabled) {
            bannerLines = new String[] { head, "Kategorie: " + category, "Messwert: " + value };
            bannerUntilMs = now + 5000;
        }
        if (cfg.soundEnabled && cfg.soundVolumePercent > 0) {
            mc.getSoundManager().play(SimpleSoundInstance.forUI(cfg.alarmSound.holder(), 1.0f, cfg.soundVolumePercent / 100f));
        }
    }

    public long cooldownRemainingMs(long now) {
        return Math.max(0, lastCooldownMs - (now - lastAlertMs));
    }

    public void clear() { lastByCategory.clear(); bannerLines = null; }
}
