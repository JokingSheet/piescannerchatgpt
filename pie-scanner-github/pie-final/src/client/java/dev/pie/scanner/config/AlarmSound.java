package dev.pie.scanner.config;

import java.util.function.Supplier;
import net.minecraft.core.Holder;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;

public enum AlarmSound {
    PLING("Pling", () -> SoundEvents.NOTE_BLOCK_PLING),
    BELL("Bell", () -> SoundEvents.NOTE_BLOCK_BELL),
    BIT("Bit", () -> SoundEvents.NOTE_BLOCK_BIT),
    CHIME("Chime", () -> SoundEvents.NOTE_BLOCK_CHIME),
    DIDGERIDOO("Didgeridoo", () -> SoundEvents.NOTE_BLOCK_DIDGERIDOO);

    public final String label;
    private final Supplier<Holder<SoundEvent>> sound;

    AlarmSound(String label, Supplier<Holder<SoundEvent>> sound) { this.label = label; this.sound = sound; }

    public Holder<SoundEvent> holder() { return sound.get(); }

    public AlarmSound next() { return values()[(ordinal() + 1) % values().length]; }
}
