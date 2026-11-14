package com.natureisamazing.mixins;

import com.natureisamazing.ai.sheep.SheepNutrition;
import com.natureisamazing.ai.sheep.SheepStress;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.animal.Sheep;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Awards satiety exactly when vanilla Sheep finishes eating grass. */
@Mixin(Sheep.class)
public class SheepMixin {
    @Inject(method = "ate", at = @At("TAIL"))
    private void restoreSatietyFromGrass(CallbackInfo ci) {
        SheepNutrition.eatGrass((Sheep) (Object) this);
    }

    /**
     * Adds at most one stress-based bonus wool block after vanilla has spawned
     * its normal 1-3 blocks. NeoForge captures spawnAtLocation calls made by
     * Sheep#shear, so this extra drop is included in the regular shear result
     * for players and other IShearable callers.
     */
    @Inject(method = "shear", at = @At("TAIL"))
    private void addStressBasedWoolBonus(SoundSource category, CallbackInfo ci) {
        SheepStress.applyShearingOutcome((Sheep) (Object) this);
    }
}
