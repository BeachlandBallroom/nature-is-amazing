package com.natureisamazing.mixins;

import com.natureisamazing.ai.sheep.SheepNutrition;
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
}
