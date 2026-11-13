package com.natureisamazing.ai.sheep;

import com.natureisamazing.data.ModAttachments;
import net.minecraft.world.entity.animal.Sheep;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;

/** Installs additional goals on vanilla sheep on the logical server. */
public final class SheepAIHandler {
    private static final float WOLF_DETECTION_DISTANCE = 15.0F;
    private static final double FLEE_SPEED = 1.4D;
    private static final double FLEE_SPRINT_SPEED = 1.8D;

    @SubscribeEvent
    public void onSheepJoinLevel(EntityJoinLevelEvent event) {
        if (!(event.getEntity() instanceof Sheep sheep)) {
            return;
        }

        if (sheep.level().isClientSide) {
            return;
        }

        // EntityJoinLevelEvent can happen more than once for an entity instance.
        if (sheep.getData(ModAttachments.SHEEP_GOALS_INSTALLED)) {
            return;
        }

        // Highest priority: run away from nearby wolves before ordinary movement goals.
        sheep.goalSelector.addGoal(
                -1,
                new AvoidEntityGoal<>(
                        sheep,
                        Wolf.class,
                        WOLF_DETECTION_DISTANCE,
                        FLEE_SPEED,
                        FLEE_SPRINT_SPEED
                )
        );

        // Rams take brief defensive charges when a wolf threatens lambs. This
        // is higher priority than fleeing, but only at close range.
        if (SheepHerdBehavior.isRam(sheep) && !sheep.isBaby()) {
            sheep.goalSelector.addGoal(-2, new RamGuardGoal(sheep));
        }

        // Adults form loose flocks; lambs continue to use vanilla parent-following AI.
        sheep.goalSelector.addGoal(4, new FollowSheepGoal(sheep));
        sheep.goalSelector.addGoal(7, new SheepPastureGoal(sheep));

        sheep.setData(ModAttachments.SHEEP_GOALS_INSTALLED, true);
    }
}
