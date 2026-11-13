package com.natureisamazing.ai.cow;

import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.animal.Cow;

import java.util.Comparator;
import java.util.EnumSet;

/**
 * A lightweight flock-following goal for adult chickens.
 *
 * Each chicken follows the nearest nearby adult chicken with a lower UUID.
 * This ordering prevents circular follow relationships. It creates loose,
 * dynamic groups rather than persistent named flocks.
 */
public final class FollowCowGoal extends Goal {
    private static final double SEARCH_RADIUS = 8.0D;
    private static final double STOP_DISTANCE_SQUARED = 9.0D; // 3 blocks
    private static final double MAX_FOLLOW_DISTANCE_SQUARED = 144.0D; // 12 blocks
    private static final int SEARCH_INTERVAL_TICKS = 20;
    private static final int PATH_RECALC_INTERVAL_TICKS = 10;
    private static final double FOLLOW_SPEED = 1.0D;

    private final Cow cow;
    private Cow leader;
    private int searchCooldown;
    private int pathRecalculationCooldown;

    public FollowCowGoal(Cow cow) {
        this.cow = cow;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        // Baby chickens already use vanilla FollowParentGoal.
        if (cow.isBaby()) {
            return false;
        }

        if (searchCooldown > 0) {
            searchCooldown--;
            return false;
        }
        searchCooldown = SEARCH_INTERVAL_TICKS;

        leader = cow.level().getEntitiesOfClass(
                        Cow.class,
                        cow.getBoundingBox().inflate(SEARCH_RADIUS),
                        candidate -> candidate != cow
                                && candidate.isAlive()
                                && !candidate.isBaby()
                                && candidate.getUUID().compareTo(cow.getUUID()) < 0
                )
                .stream()
                .min(Comparator.comparingDouble(cow::distanceToSqr))
                .orElse(null);

        return leader != null
                && cow.distanceToSqr(leader) > STOP_DISTANCE_SQUARED;
    }

    @Override
    public boolean canContinueToUse() {
        return leader != null
                && leader.isAlive()
                && !cow.isBaby()
                && cow.distanceToSqr(leader) > STOP_DISTANCE_SQUARED
                && cow.distanceToSqr(leader) < MAX_FOLLOW_DISTANCE_SQUARED;
    }

    @Override
    public void start() {
        pathRecalculationCooldown = 0;
    }

    @Override
    public void stop() {
        leader = null;
        cow.getNavigation().stop();
    }

    @Override
    public void tick() {
        if (leader == null) {
            return;
        }

        if (pathRecalculationCooldown-- <= 0) {
            pathRecalculationCooldown = PATH_RECALC_INTERVAL_TICKS;
            cow.getNavigation().moveTo(leader, FOLLOW_SPEED);
        }
    }
}
