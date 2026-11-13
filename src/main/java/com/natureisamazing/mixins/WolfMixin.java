package com.natureisamazing.mixins;

import com.natureisamazing.api.WolfSleepState;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Predicate;

@Mixin(Wolf.class)
public class WolfMixin implements WolfSleepState {
    @Unique
    private static final EntityDataAccessor<Boolean> NATURE_IS_AMAZING_SLEEPING =
            SynchedEntityData.defineId(Wolf.class, EntityDataSerializers.BOOLEAN);

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

    @Inject(method = "defineSynchedData", at = @At("TAIL"))
    private void defineSleepData(SynchedEntityData.Builder builder, CallbackInfo ci) {
        builder.define(NATURE_IS_AMAZING_SLEEPING, false);
    }

    @Override
    public boolean natureIsAmazing$isSleeping() {
        return ((Wolf) (Object) this).getEntityData().get(NATURE_IS_AMAZING_SLEEPING);
    }

    @Override
    public void natureIsAmazing$setSleeping(boolean sleeping) {
        ((Wolf) (Object) this).getEntityData().set(NATURE_IS_AMAZING_SLEEPING, sleeping);
    }

}
