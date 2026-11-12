package com.natureisamazing.ai.chicken;

import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.animal.Chicken;

import java.util.Comparator;
import java.util.EnumSet;

/**
 * A lightweight flock-following goal for adult chickens.
 *
 * Each chicken follows the nearest nearby adult chicken with a lower UUID.
 * This ordering prevents circular follow relationships. It creates loose,
 * dynamic groups rather than persistent named flocks.
 */
public final class FollowChickenGoal extends Goal {
    private static final double SEARCH_RADIUS = 8.0D;
    private static final double STOP_DISTANCE_SQUARED = 9.0D; // 3 blocks
    private static final double MAX_FOLLOW_DISTANCE_SQUARED = 144.0D; // 12 blocks
    private static final int SEARCH_INTERVAL_TICKS = 20;
    private static final int PATH_RECALC_INTERVAL_TICKS = 10;
    private static final double FOLLOW_SPEED = 1.0D;

    private final Chicken chicken;
    private Chicken leader;
    private int searchCooldown;
    private int pathRecalculationCooldown;

    public FollowChickenGoal(Chicken chicken) {
        this.chicken = chicken;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        // Baby chickens already use vanilla FollowParentGoal.
        if (chicken.isBaby()) {
            return false;
        }

        if (searchCooldown > 0) {
            searchCooldown--;
            return false;
        }
        searchCooldown = SEARCH_INTERVAL_TICKS;

        leader = chicken.level().getEntitiesOfClass(
                        Chicken.class,
                        chicken.getBoundingBox().inflate(SEARCH_RADIUS),
                        candidate -> candidate != chicken
                                && candidate.isAlive()
                                && !candidate.isBaby()
                                && candidate.getUUID().compareTo(chicken.getUUID()) < 0
                )
                .stream()
                .min(Comparator.comparingDouble(chicken::distanceToSqr))
                .orElse(null);

        return leader != null
                && chicken.distanceToSqr(leader) > STOP_DISTANCE_SQUARED;
    }

    @Override
    public boolean canContinueToUse() {
        return leader != null
                && leader.isAlive()
                && !chicken.isBaby()
                && chicken.distanceToSqr(leader) > STOP_DISTANCE_SQUARED
                && chicken.distanceToSqr(leader) < MAX_FOLLOW_DISTANCE_SQUARED;
    }

    @Override
    public void start() {
        pathRecalculationCooldown = 0;
    }

    @Override
    public void stop() {
        leader = null;
        chicken.getNavigation().stop();
    }

    @Override
    public void tick() {
        if (leader == null) {
            return;
        }

        if (pathRecalculationCooldown-- <= 0) {
            pathRecalculationCooldown = PATH_RECALC_INTERVAL_TICKS;
            chicken.getNavigation().moveTo(leader, FOLLOW_SPEED);
        }
    }
}
