package dev.pie.scanner;

import com.mojang.brigadier.CommandDispatcher;
import dev.pie.scanner.alert.AlertManager;
import dev.pie.scanner.config.PieConfig;
import dev.pie.scanner.gui.ConfigScreen;
import dev.pie.scanner.hud.HudRenderer;
import dev.pie.scanner.scanner.PieDataSource;
import dev.pie.scanner.scanner.PieScanner;
import java.util.function.Supplier;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public final class PieScannerMod implements ClientModInitializer {
    private static PieConfig config;
    private static final PieScanner SCANNER = new PieScanner();
    private static final AlertManager ALERTS = new AlertManager();
    private static Supplier<Screen> pendingScreen;

    public static PieConfig config() { return config; }
    public static PieScanner scanner() { return SCANNER; }
    public static AlertManager alerts() { return ALERTS; }

    @Override public void onInitializeClient() {
        config = PieConfig.load();

        ClientTickEvents.END_CLIENT_TICK.register(mc -> {
            if (pendingScreen != null) { Screen s = pendingScreen.get(); pendingScreen = null; mc.setScreen(s); }
            SCANNER.tick(mc, config, ALERTS);
        });
        HudRenderCallback.EVENT.register((graphics, tickCounter) -> HudRenderer.render(graphics));

        // Beim Welt-/Serverwechsel Zustand verwerfen (keine Altlasten, keine Leaks)
        ClientPlayConnectionEvents.DISCONNECT.register((handler, mc) -> {
            PieDataSource.reset(); SCANNER.resetBaselines(); ALERTS.clear();
        });

        ClientCommandRegistrationCallback.EVENT.register((dispatcher, access) -> registerCommands(dispatcher));
    }

    private void registerCommands(CommandDispatcher<FabricClientCommandSource> d) {
        d.register(ClientCommandManager.literal("piescan")
            .then(ClientCommandManager.literal("config").executes(c -> {
                pendingScreen = () -> new ConfigScreen(null); return 1; }))
            .then(ClientCommandManager.literal("toggle").executes(c -> {
                config.scannerEnabled = !config.scannerEnabled; config.save();
                c.getSource().sendFeedback(Component.literal("Pie Scanner: " + (config.scannerEnabled ? "ON" : "OFF"))); return 1; }))
            .then(ClientCommandManager.literal("test").executes(c -> {
                ALERTS.test(Minecraft.getInstance(), config); return 1; }))
            .then(ClientCommandManager.literal("dump").executes(c -> {
                var lines = SCANNER.dumpNames(25);
                if (lines.isEmpty()) c.getSource().sendFeedback(Component.literal("Keine Daten: Pie-Chart mit Shift+F3 einblenden."));
                else for (String l : lines) c.getSource().sendFeedback(Component.literal(l));
                return 1; }))
            .then(ClientCommandManager.literal("reset").executes(c -> {
                SCANNER.resetBaselines(); c.getSource().sendFeedback(Component.literal("Baselines zurueckgesetzt.")); return 1; }))
            .executes(c -> { pendingScreen = () -> new ConfigScreen(null); return 1; }));
    }
}
