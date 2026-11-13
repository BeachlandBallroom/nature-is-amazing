package com.natureisamazing.mixins;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Wolf;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Predicate;

@Mixin(Wolf.class)
public class WolfMixin {
    @Shadow
    @Final
    @Mutable
    private static Predicate<LivingEntity> PREY_SELECTOR;

    @Inject(method = "<clinit>", at = @At("TAIL"))
    private static void onStaticInitEnd(CallbackInfo ci) {
        WolfMixin.PREY_SELECTOR = (livingEntity) -> {
            EntityType<?> entityType = livingEntity.getType();
            return entityType == EntityType.SHEEP
                    || entityType == EntityType.RABBIT
                    || entityType == EntityType.FOX
                    || entityType == EntityType.COW;
        };
    }
}
