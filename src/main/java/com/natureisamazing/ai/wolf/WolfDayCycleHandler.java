package com.natureisamazing.ai.wolf;

import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.living.LivingChangeTargetEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

public final class WolfDayCycleHandler {

    private static final int HIDE_SEARCH_INTERVAL = 40;
    private static final int HIDE_SEARCH_RADIUS = 10;
    private static final int HIDE_SEARCH_ATTEMPTS = 96;
    private static final double OTHER_WOLF_CLEARANCE = 3.0D;

    @SubscribeEvent
    public void onWolfChangeTarget(LivingChangeTargetEvent event) {
        if (!(event.getEntity() instanceof Wolf wolf)) {
            return;
        }

        if (wolf.level().isClientSide || wolf.isTame()) {
            return;
        }

        if (wolf.level().isDay() && WolfDayBehavior.activity(wolf) != WolfDayBehavior.Activity.HUNT) {
            LivingEntity newTarget = event.getNewAboutToBeSetTarget();
            if (newTarget != null) {
                event.setCanceled(true);
            }
        }
    }

    @SubscribeEvent
    public void onWolfTick(EntityTickEvent.Post event) {
        if (!(event.getEntity() instanceof Wolf wolf)) {
            return;
        }

        if (wolf.level().isClientSide || wolf.isTame()) {
            return;
        }

        Level level = wolf.level();

        if (level.isNight()) {
            return;
        }

        WolfDayBehavior.Activity activity = WolfDayBehavior.activity(wolf);
        if (activity != WolfDayBehavior.Activity.SLEEP
                && activity != WolfDayBehavior.Activity.SHADE_WANDER) {
            return;
        }

        if (wolf.tickCount % HIDE_SEARCH_INTERVAL != 0) {
            return;
        }

        if (!level.canSeeSky(wolf.blockPosition())) {
            return;
        }

        BlockPos hidingSpot = findHidingSpot(level, wolf, wolf.blockPosition());
        if (hidingSpot != null) {
            wolf.getNavigation().moveTo(
                    hidingSpot.getX() + 0.5,
                    hidingSpot.getY(),
                    hidingSpot.getZ() + 0.5,
                    1.0D
            );
        }
    }

    private BlockPos findHidingSpot(Level level, Wolf wolf, BlockPos wolfPos) {
        // Do not scan from one fixed corner: otherwise every nearby wolf picks
        // the same first valid block and they pile into a single spot.
        for (int attempt = 0; attempt < HIDE_SEARCH_ATTEMPTS; attempt++) {
            BlockPos pos = wolfPos.offset(
                    wolf.getRandom().nextInt(HIDE_SEARCH_RADIUS * 2 + 1) - HIDE_SEARCH_RADIUS,
                    wolf.getRandom().nextInt(7) - 3,
                    wolf.getRandom().nextInt(HIDE_SEARCH_RADIUS * 2 + 1) - HIDE_SEARCH_RADIUS
            );

            boolean anotherWolfIsNearby = !level.getEntitiesOfClass(
                    Wolf.class,
                    new AABB(pos).inflate(OTHER_WOLF_CLEARANCE),
                    otherWolf -> otherWolf != wolf
            ).isEmpty();

            if (!anotherWolfIsNearby
                    && !level.canSeeSky(pos)
                    && level.getBlockState(pos).isAir()
                    && level.getBlockState(pos.above()).isAir()) {
                return pos.immutable();
            }
        }
        return null; // Укрытие не найдено или всё подходящее уже занято.
    }
}
