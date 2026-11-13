package com.natureisamazing.ai.sheep;

import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.animal.Sheep;

import java.util.Comparator;
import java.util.EnumSet;

/**
 * A lightweight flock-following goal for adult sheep.
 *
 * Ewes form mixed-age flocks, while rams prefer other rams as a loose
 * bachelor group outside the breeding season.
 * This ordering prevents circular follow relationships. It creates loose,
 * dynamic groups rather than persistent named flocks.
 */
public final class FollowSheepGoal extends Goal {
    private static final double SEARCH_RADIUS = 8.0D;
    private static final double STOP_DISTANCE_SQUARED = 9.0D; // 3 blocks
    private static final double MAX_FOLLOW_DISTANCE_SQUARED = 144.0D; // 12 blocks
    private static final int SEARCH_INTERVAL_TICKS = 20;
    private static final int PATH_RECALC_INTERVAL_TICKS = 10;
    private static final double FOLLOW_SPEED = 1.0D;

    private final Sheep sheep;
    private Sheep leader;
    private int searchCooldown;
    private int pathRecalculationCooldown;

    public FollowSheepGoal(Sheep sheep) {
        this.sheep = sheep;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        // Lambs already use vanilla FollowParentGoal.
        if (sheep.isBaby()) {
            return false;
        }

        if (searchCooldown > 0) {
            searchCooldown--;
            return false;
        }
        searchCooldown = SEARCH_INTERVAL_TICKS;

        leader = sheep.level().getEntitiesOfClass(
                        Sheep.class,
                        sheep.getBoundingBox().inflate(SEARCH_RADIUS),
                        candidate -> candidate != sheep
                                && candidate.isAlive()
                                && !candidate.isBaby()
                                && SheepHerdBehavior.isRam(candidate) == SheepHerdBehavior.isRam(sheep)
                                && candidate.getUUID().compareTo(sheep.getUUID()) < 0
                )
                .stream()
                .min(Comparator.comparingDouble(sheep::distanceToSqr))
                .orElse(null);

        return leader != null
                && sheep.distanceToSqr(leader) > STOP_DISTANCE_SQUARED;
    }

    @Override
    public boolean canContinueToUse() {
        return leader != null
                && leader.isAlive()
                && !sheep.isBaby()
                && sheep.distanceToSqr(leader) > STOP_DISTANCE_SQUARED
                && sheep.distanceToSqr(leader) < MAX_FOLLOW_DISTANCE_SQUARED;
    }

    @Override
    public void start() {
        pathRecalculationCooldown = 0;
    }

    @Override
    public void stop() {
        leader = null;
        sheep.getNavigation().stop();
    }

    @Override
    public void tick() {
        if (leader == null) {
            return;
        }

        if (pathRecalculationCooldown-- <= 0) {
            pathRecalculationCooldown = PATH_RECALC_INTERVAL_TICKS;
            sheep.getNavigation().moveTo(leader, FOLLOW_SPEED);
        }
    }
}
