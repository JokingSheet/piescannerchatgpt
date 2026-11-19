package dev.pie.scanner.gui;

import dev.pie.scanner.PieScannerMod;
import dev.pie.scanner.config.PieConfig;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public final class ConfigScreen extends Screen {
    private final PieConfig cfg = PieScannerMod.config();
    private final Screen parent;

    public ConfigScreen(Screen parent) { super(Component.literal("Pie Scanner - Einstellungen")); this.parent = parent; }

    private void toggle(int x, int y, String label, BooleanSupplier get, Consumer<Boolean> set) {
        addRenderableWidget(Button.builder(msg(label, get.getAsBoolean()), b -> {
            set.accept(!get.getAsBoolean()); b.setMessage(msg(label, get.getAsBoolean())); cfg.save();
        }).bounds(x, y, 150, 20).build());
    }

    private static Component msg(String label, boolean on) { return Component.literal(label + ": " + (on ? "AN" : "AUS")); }

    @Override protected void init() {
        int cx = width / 2, y = 36, L = cx - 155, R = cx + 5;
        toggle(L, y, "Scanner", () -> cfg.scannerEnabled, v -> cfg.scannerEnabled = v);
        toggle(R, y, "HUD", () -> cfg.hudEnabled, v -> cfg.hudEnabled = v);
        y += 24;
        toggle(L, y, "Alarmton", () -> cfg.soundEnabled, v -> cfg.soundEnabled = v);
        toggle(R, y, "Chat-Meldung", () -> cfg.chatEnabled, v -> cfg.chatEnabled = v);
        y += 24;
        toggle(L, y, "Bildschirmmeldung", () -> cfg.bannerEnabled, v -> cfg.bannerEnabled = v);
        addRenderableWidget(Button.builder(Component.literal("Ton: " + cfg.alarmSound.label), b -> {
            cfg.alarmSound = cfg.alarmSound.next(); b.setMessage(Component.literal("Ton: " + cfg.alarmSound.label));
            cfg.save(); PieScannerMod.alerts().test(minecraft, cfg);
        }).bounds(R, y, 150, 20).build());
        y += 24;
        addRenderableWidget(new IntSlider(L, y, 150, 20, "Empfindlichkeit", 1, 10, cfg.sensitivity, v -> { cfg.sensitivity = v; cfg.save(); }));
        addRenderableWidget(new IntSlider(R, y, 150, 20, "Cooldown (s)", 5, 300, cfg.cooldownSeconds, v -> { cfg.cooldownSeconds = v; cfg.save(); }));
        y += 24;
        addRenderableWidget(new IntSlider(L, y, 150, 20, "Lautstaerke %", 0, 100, cfg.soundVolumePercent, v -> { cfg.soundVolumePercent = v; cfg.save(); }));
        y += 24;
        addRenderableWidget(new IntSlider(L, y, 150, 20, "HUD X %", 0, 95, cfg.hudXPercent, v -> { cfg.hudXPercent = v; cfg.save(); }));
        addRenderableWidget(new IntSlider(R, y, 150, 20, "HUD Y %", 0, 95, cfg.hudYPercent, v -> { cfg.hudYPercent = v; cfg.save(); }));
        y += 28;
        addRenderableWidget(Button.builder(Component.literal("Kategorien..."),
            b -> minecraft.setScreen(new CategoryScreen(this))).bounds(L, y, 150, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Baselines zuruecksetzen"), b -> PieScannerMod.scanner().resetBaselines())
            .bounds(R, y, 150, 20).build());
        y += 28;
        addRenderableWidget(Button.builder(Component.literal("Fertig"), b -> onClose()).bounds(cx - 75, y, 150, 20).build());
    }

    @Override public void render(GuiGraphics g, int mx, int my, float delta) {
        super.render(g, mx, my, delta);
        g.drawCenteredString(font, title, width / 2, 14, 0xFFFFFFFF);
    }

    @Override public void onClose() { cfg.save(); minecraft.setScreen(parent); }
}
