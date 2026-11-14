package com.natureisamazing.ai.sheep;

import com.natureisamazing.data.ModAttachments;
import net.minecraft.world.entity.animal.Sheep;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.living.BabyEntitySpawnEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

/** Applies sheep nutrition, breeding checks, and low-frequency stress updates. */
public final class SheepNutritionHandler {
    private static final int SATIETY_DECAY_INTERVAL_TICKS = 300;
    private static final int NATURAL_BREEDING_CHECK_INTERVAL_TICKS = 200;
    private static final double MATE_SEARCH_RADIUS = 8.0D;
    private static final int STRESS_UPDATE_INTERVAL_TICKS = 200;

    @SubscribeEvent
    public void onSheepTick(EntityTickEvent.Post event) {
        if (!(event.getEntity() instanceof Sheep sheep) || sheep.level().isClientSide) {
            return;
        }

        if (sheep.tickCount % SATIETY_DECAY_INTERVAL_TICKS == 0) {
            int satiety = sheep.getData(ModAttachments.SHEEP_HUNGER);
            if (satiety > 0) {
                sheep.setData(ModAttachments.SHEEP_HUNGER, satiety - 1);
            }
        }

        if (sheep.tickCount % NATURAL_BREEDING_CHECK_INTERVAL_TICKS == 0) {
            tryStartNaturalBreeding(sheep);
        }

        // Spread local-entity queries across the 10-second interval instead of
        // making every sheep scan its surroundings on the same server tick.
        int stressPhase = Math.floorMod(sheep.getId(), STRESS_UPDATE_INTERVAL_TICKS);
        if (Math.floorMod(sheep.tickCount, STRESS_UPDATE_INTERVAL_TICKS) == stressPhase) {
            SheepStress.updateFromEnvironment(sheep);
        }
    }

    @SubscribeEvent
    public void onSheepDamaged(LivingDamageEvent.Post event) {
        if (!(event.getEntity() instanceof Sheep sheep) || sheep.level().isClientSide) {
            return;
        }

        float actualDamage = event.getNewDamage();
        if (actualDamage <= 0.0F) {
            return;
        }

        // Minor bumps still cause a small reaction; a heavy hit is capped.
        int stressIncrease = Math.max(6, Math.min(25, Math.round(actualDamage * 5.0F)));
        SheepStress.addStress(sheep, stressIncrease);
    }

    @SubscribeEvent
    public void onSheepBreed(BabyEntitySpawnEvent event) {
        if (!(event.getParentA() instanceof Sheep firstParent)
                || !(event.getParentB() instanceof Sheep secondParent)) {
            return;
        }

        if (!SheepNutrition.canBreed(firstParent) || !SheepNutrition.canBreed(secondParent)) {
            event.setCanceled(true);
            return;
        }

        SheepNutrition.spendForBreeding(firstParent);
        SheepNutrition.spendForBreeding(secondParent);
    }

    private void tryStartNaturalBreeding(Sheep sheep) {
        if (sheep.isBaby()
                || !sheep.canFallInLove()
                || !SheepNutrition.canBreed(sheep)
                || sheep.getRandom().nextInt(3) != 0) {
            return;
        }

        Sheep mate = sheep.level().getEntitiesOfClass(
                        Sheep.class,
                        sheep.getBoundingBox().inflate(MATE_SEARCH_RADIUS),
                        candidate -> candidate != sheep
                                && !candidate.isBaby()
                                && candidate.canFallInLove()
                                && SheepHerdBehavior.isRam(candidate) != SheepHerdBehavior.isRam(sheep)
                                && SheepNutrition.canBreed(candidate)
                )
                .stream()
                .findFirst()
                .orElse(null);
        if (mate == null) {
            return;
        }

        // The vanilla BreedGoal takes over from here and still produces the
        // usual heart particles, cooldowns, XP, and child attributes.
        sheep.setInLove(null);
        mate.setInLove(null);
    }
}
