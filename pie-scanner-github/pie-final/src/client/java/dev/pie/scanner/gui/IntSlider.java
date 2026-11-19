package dev.pie.scanner.gui;

import java.util.function.IntConsumer;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.network.chat.Component;

public final class IntSlider extends AbstractSliderButton {
    private final String label; private final int min, max; private final IntConsumer onChange;

    public IntSlider(int x, int y, int w, int h, String label, int min, int max, int value, IntConsumer onChange) {
        super(x, y, w, h, Component.empty(), (value - min) / (double) (max - min));
        this.label = label; this.min = min; this.max = max; this.onChange = onChange;
        updateMessage();
    }

    private int current() { return min + (int) Math.round(value * (max - min)); }

    @Override protected void updateMessage() { setMessage(Component.literal(label + ": " + current())); }

    @Override protected void applyValue() { onChange.accept(current()); }
}
