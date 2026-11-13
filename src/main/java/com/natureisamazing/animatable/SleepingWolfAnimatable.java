package com.natureisamazing.animatable;

import com.natureisamazing.api.WolfSleepState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Wolf;
import software.bernie.geckolib.animatable.GeoReplacedEntity;
import software.bernie.geckolib.animatable.SingletonGeoAnimatable;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.constant.DataTickets;
import software.bernie.geckolib.util.GeckoLibUtil;

/** GeckoLib animatable used only while a vanilla wolf is sleeping. */
public final class SleepingWolfAnimatable implements GeoReplacedEntity {
    public static final RawAnimation SLEEP = RawAnimation.begin().thenLoop("sleeping");

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public SleepingWolfAnimatable() {
        SingletonGeoAnimatable.registerSyncedAnimatable(this);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "wolf_sleep", 5, state -> {
            Entity entity = state.getData(DataTickets.ENTITY);
            if (entity instanceof Wolf wolf
                    && ((WolfSleepState) wolf).natureIsAmazing$isSleeping()) {
                return state.setAndContinue(SLEEP);
            }
            return PlayState.STOP;
        }));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }

    @Override
    public EntityType<?> getReplacingEntityType() {
        return EntityType.WOLF;
    }
}
