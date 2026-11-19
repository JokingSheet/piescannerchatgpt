package dev.pie.scanner.mixin;

import dev.pie.scanner.scanner.PieDataSource;
import java.util.List;
import net.minecraft.util.profiling.FilledProfileResults;
import net.minecraft.util.profiling.ProfileResults;
import net.minecraft.util.profiling.ResultField;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Die Pie-Chart-Anzeige fragt jedes Frame getTimes(path) auf den aktuellen Profiler-Ergebnissen ab.
 * Wir merken uns nur die Referenz. Ausgewertet wird spaeter im Client-Tick.
 * defaultRequire=0: schlaegt der Hook fehl, laeuft die Mod weiter und meldet ehrlich "Keine Daten".
 */
@Mixin(FilledProfileResults.class)
public abstract class FilledProfileResultsMixin {
    @Inject(method = "getTimes", at = @At("HEAD"))
    private void pieScanner$capture(String path, CallbackInfoReturnable<List<ResultField>> cir) {
        PieDataSource.offer((ProfileResults) (Object) this);
    }
}
