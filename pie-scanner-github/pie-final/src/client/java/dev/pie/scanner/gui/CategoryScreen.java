package dev.pie.scanner.gui;

import dev.pie.scanner.PieScannerMod;
import dev.pie.scanner.config.PieConfig;
import dev.pie.scanner.scanner.Category;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public final class CategoryScreen extends Screen {
    private final PieConfig cfg = PieScannerMod.config();
    private final Screen parent;

    public CategoryScreen(Screen parent) { super(Component.literal("Pie Scanner - Kategorien")); this.parent = parent; }

    private Component msg(Category c) {
        boolean on = !cfg.disabledCategories.contains(c.key());
        return Component.literal(c.label() + ": " + (on ? "AN" : "AUS"));
    }

    @Override protected void init() {
        int cx = width / 2, y0 = 34;
        for (int i = 0; i < Category.ALL.size(); i++) {
            Category c = Category.ALL.get(i);
            int x = cx - 155 + (i % 2) * 160, y = y0 + (i / 2) * 22;
            addRenderableWidget(Button.builder(msg(c), b -> {
                if (!cfg.disabledCategories.remove(c.key())) cfg.disabledCategories.add(c.key());
                b.setMessage(msg(c)); cfg.save();
            }).bounds(x, y, 150, 20).build());
        }
        int y = y0 + ((Category.ALL.size() + 1) / 2) * 22 + 8;
        addRenderableWidget(Button.builder(Component.literal("Zurueck"), b -> onClose()).bounds(cx - 75, y, 150, 20).build());
    }

    @Override public void render(GuiGraphics g, int mx, int my, float delta) {
        super.render(g, mx, my, delta);
        g.drawCenteredString(font, title, width / 2, 14, 0xFFFFFFFF);
    }

    @Override public void onClose() { cfg.save(); minecraft.setScreen(parent); }
}
