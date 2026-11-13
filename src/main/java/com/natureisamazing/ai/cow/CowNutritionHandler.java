package com.natureisamazing.ai.cow;

import com.natureisamazing.data.ModAttachments;
import net.minecraft.world.entity.animal.Cow;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.living.BabyEntitySpawnEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

/** Applies the slow hunger drain and nutrition-gated cow breeding. */
public final class CowNutritionHandler {
    private static final int SATIETY_DECAY_INTERVAL_TICKS = 300;
    private static final int NATURAL_BREEDING_CHECK_INTERVAL_TICKS = 200;
    private static final double MATE_SEARCH_RADIUS = 8.0D;

    @SubscribeEvent
    public void onCowTick(EntityTickEvent.Post event) {
        if (!(event.getEntity() instanceof Cow cow) || cow.level().isClientSide) {
            return;
        }

        if (cow.tickCount % SATIETY_DECAY_INTERVAL_TICKS == 0) {
            int satiety = cow.getData(ModAttachments.SHEEP_HUNGER);
            if (satiety > 0) {
                cow.setData(ModAttachments.SHEEP_HUNGER, satiety - 1);
            }
        }

        if (cow.tickCount % NATURAL_BREEDING_CHECK_INTERVAL_TICKS == 0) {
            tryStartNaturalBreeding(cow);
        }
    }

    @SubscribeEvent
    public void onCowBreed(BabyEntitySpawnEvent event) {
        if (!(event.getParentA() instanceof Cow firstParent)
                || !(event.getParentB() instanceof Cow secondParent)) {
            return;
        }

        if (!CowNutrition.canBreed(firstParent) || !CowNutrition.canBreed(secondParent)) {
            event.setCanceled(true);
            return;
        }

        CowNutrition.spendForBreeding(firstParent);
        CowNutrition.spendForBreeding(secondParent);
    }

    private void tryStartNaturalBreeding(Cow cow) {
        if (cow.isBaby()
                || !cow.canFallInLove()
                || !CowNutrition.canBreed(cow)
                || cow.getRandom().nextInt(3) != 0) {
            return;
        }

        Cow mate = cow.level().getEntitiesOfClass(
                        Cow.class,
                        cow.getBoundingBox().inflate(MATE_SEARCH_RADIUS),
                        candidate -> candidate != cow
                                && !candidate.isBaby()
                                && candidate.canFallInLove()
                                && CowHerdBehavior.isBull(candidate) != CowHerdBehavior.isBull(cow)
                                && CowNutrition.canBreed(candidate)
                )
                .stream()
                .findFirst()
                .orElse(null);
        if (mate == null) {
            return;
        }

        // The vanilla BreedGoal takes over from here and still produces the
        // usual heart particles, cooldowns, XP, and child attributes.
        cow.setInLove(null);
        mate.setInLove(null);
    }
}
