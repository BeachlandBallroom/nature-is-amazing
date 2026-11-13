package com.natureisamazing.ai.cow;

import com.natureisamazing.ai.cow.CowPastureGoal;
import com.natureisamazing.data.ModAttachments;
import net.minecraft.world.entity.animal.Cow;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;

/** Installs additional goals on vanilla cow on the logical server. */
public final class CowAIHandler {
    private static final float WOLF_DETECTION_DISTANCE = 15.0F;
    private static final double FLEE_SPEED = 1.4D;
    private static final double FLEE_SPRINT_SPEED = 1.8D;

    @SubscribeEvent
    public void onCowJoinLevel(EntityJoinLevelEvent event) {
        if (!(event.getEntity() instanceof Cow cow)) {
            return;
        }

        if (cow.level().isClientSide) {
            return;
        }

        // EntityJoinLevelEvent can happen more than once for an entity instance.
        if (cow.getData(ModAttachments.COW_GOALS_INSTALLED)) {
            return;
        }

        // Highest priority: run away from nearby wolves before ordinary movement goals.
        cow.goalSelector.addGoal(
                -1,
                new AvoidEntityGoal<>(
                        cow,
                        Wolf.class,
                        WOLF_DETECTION_DISTANCE,
                        FLEE_SPEED,
                        FLEE_SPRINT_SPEED
                )
        );

        // Adults loosely group together. Baby cow continue to use vanilla parent-following AI.
        cow.goalSelector.addGoal(4, new FollowCowGoal(cow));
        cow.goalSelector.addGoal(7, new CowPastureGoal(cow));

        cow.setData(ModAttachments.COW_GOALS_INSTALLED, true);
    }
}
