package com.natureisamazing.ai.wolf;

import com.natureisamazing.data.ModAttachments;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Wolf;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.living.LivingChangeTargetEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;


/**
 * Adds a simple hunger loop to wild wolves.
 * Tamed wolves keep their vanilla behavior and do not lose hunger.
 */
public final class WolfHungerHandler {
    /** One hunger point is lost every 200 game ticks (10 seconds). */
    private static final int HUNGER_DECAY_INTERVAL_TICKS = 200;

    /** Wild wolves may start hunting when hunger is at or below this value. */
    private static final int HUNTING_HUNGER_THRESHOLD = 30;

    private static final int MAX_HUNGER = 100;
    private static final int CRITICAL_HUNGER = 10;
    private static final int STARVATION_DEATH_TICKS = 2_400;

    private final WolfPackHandler packHandler;

    public WolfHungerHandler(WolfPackHandler packHandler) {
        this.packHandler = packHandler;
    }

    @SubscribeEvent
    public void onWolfTick(EntityTickEvent.Post event) {
        if (!(event.getEntity() instanceof Wolf wolf)) {
            return;
        }

        // All AI and hunger calculations run on the logical server only.
        if (wolf.level().isClientSide || wolf.isTame()) {
            return;
        }

        if (wolf.tickCount % 20 == 0) {
            updateStarvation(wolf);
        }

        if (wolf.tickCount % HUNGER_DECAY_INTERVAL_TICKS != 0) {
            return;
        }

        int hunger = wolf.getData(ModAttachments.WOLF_HUNGER);
        if (hunger > 0) {
            wolf.setData(ModAttachments.WOLF_HUNGER, hunger - 1);
        }
    }

    /**
     * Prevent a wild wolf from selecting natural prey while it is not hungry.
     * This does not block retaliation against entities that are not in the
     * vanilla wolf prey selector.
     */
    @SubscribeEvent
    public void onWolfChangeTarget(LivingChangeTargetEvent event) {
        if (!(event.getEntity() instanceof Wolf wolf)) {
            return;
        }

        if (wolf.level().isClientSide || wolf.isTame()) {
            return;
        }

        LivingEntity newTarget = event.getNewAboutToBeSetTarget();
        if (newTarget == null || !Wolf.PREY_SELECTOR.test(newTarget)) {
            return;
        }

        int hunger = wolf.getData(ModAttachments.WOLF_HUNGER);
        // A fed wolf normally does not initiate a hunt, but it may join a
        // nearby packmate that has already committed to the same prey.
        if (hunger > HUNTING_HUNGER_THRESHOLD && !this.packHandler.isPackHunt(wolf, newTarget)) {
            event.setCanceled(true);
        }
    }

    /** Replenish hunger after a wild wolf kills an animal it naturally hunts. */
    @SubscribeEvent
    public void onPreyDeath(LivingDeathEvent event) {
        if (!(event.getSource().getEntity() instanceof Wolf wolf)) {
            return;
        }

        if (wolf.level().isClientSide || wolf.isTame()) {
            return;
        }

        if (!Wolf.PREY_SELECTOR.test(event.getEntity())) {
            return;
        }

        if (wolf.getData(ModAttachments.WOLF_HUNGER) < MAX_HUNGER) {
            // Loot is spawned immediately after this event; the dedicated goal
            // picks it up as soon as the ItemEntity appears.
            wolf.setData(ModAttachments.WOLF_MEAL_UNTIL, wolf.level().getGameTime() + 200L);
        } else {
            this.packHandler.beginPostHuntRest(wolf);
        }
    }

    private void updateStarvation(Wolf wolf) {
        int hunger = wolf.getData(ModAttachments.WOLF_HUNGER);
        if (hunger > CRITICAL_HUNGER) {
            wolf.setData(ModAttachments.WOLF_STARVATION_TICKS, 0);
            return;
        }

        int starvingFor = wolf.getData(ModAttachments.WOLF_STARVATION_TICKS) + 20;
        wolf.setData(ModAttachments.WOLF_STARVATION_TICKS, starvingFor);
        if (starvingFor >= STARVATION_DEATH_TICKS) {
            wolf.hurt(wolf.damageSources().starve(), Float.MAX_VALUE);
        }
    }

}
