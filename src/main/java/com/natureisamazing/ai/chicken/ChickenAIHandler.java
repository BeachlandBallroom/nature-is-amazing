package com.natureisamazing.ai.chicken;

import com.natureisamazing.data.ModAttachments;
import net.minecraft.world.entity.animal.Chicken;
import net.minecraft.world.entity.animal.Fox;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;

/** Installs additional goals on vanilla chickens on the logical server. */
public final class ChickenAIHandler {
    private static final float FOX_DETECTION_DISTANCE = 15.0F;
    private static final double FLEE_SPEED = 1.4D;
    private static final double FLEE_SPRINT_SPEED = 1.8D;

    @SubscribeEvent
    public void onChickenJoinLevel(EntityJoinLevelEvent event) {
        if (!(event.getEntity() instanceof Chicken chicken)) {
            return;
        }

        if (chicken.level().isClientSide) {
            return;
        }

        // EntityJoinLevelEvent can happen more than once for an entity instance.
        if (chicken.getData(ModAttachments.CHICKEN_GOALS_INSTALLED)) {
            return;
        }

        // Highest priority: run away from nearby wolves before ordinary movement goals.
        chicken.goalSelector.addGoal(
                -1,
                new AvoidEntityGoal<>(
                        chicken,
                        Fox.class,
                        FOX_DETECTION_DISTANCE,
                        FLEE_SPEED,
                        FLEE_SPRINT_SPEED
                )
        );

        // Adults loosely group together. Baby chickens continue to use vanilla parent-following AI.
        chicken.goalSelector.addGoal(4, new FollowChickenGoal(chicken));

        chicken.setData(ModAttachments.CHICKEN_GOALS_INSTALLED, true);
    }
}
