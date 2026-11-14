package com.natureisamazing.ai.wolf;

import com.natureisamazing.api.WolfSleepState;
import com.natureisamazing.data.ModAttachments;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.pathfinder.Path;

import java.util.EnumSet;

/**
 * Gives each wild wolf short, low-frequency patrol routes around its pack's
 * persisted territory center. It yields immediately to combat, eating, rest,
 * sleep, swimming and player-ordered sitting.
 */
public final class WolfPatrolGoal extends Goal {
    private static final int TERRITORY_RADIUS = 32;
    private static final int INNER_PATROL_RADIUS = 10;
    private static final int OUTER_PATROL_RADIUS = 27;
    private static final int TARGET_SEARCH_ATTEMPTS = 5;
    private static final long RETRY_INTERVAL_TICKS = 60L;
    private static final long MIN_COOLDOWN_TICKS = 80L;
    private static final long MAX_COOLDOWN_TICKS = 160L;
    private static final long MAX_PATROL_DURATION_TICKS = 400L;
    private static final double PATROL_SPEED = 0.85D;
    private static final double ARRIVAL_DISTANCE_SQR = 4.0D;

    private final Wolf wolf;
    private final WolfPackHandler packHandler;

    private BlockPos destination;
    private Path patrolPath;
    private long nextSearchAt;
    private long stopAt;
    private long nextPatrolAt;

    public WolfPatrolGoal(Wolf wolf, WolfPackHandler packHandler) {
        this.wolf = wolf;
        this.packHandler = packHandler;
        // Stagger the first route so packmates do not all pathfind on the same tick.
        this.nextSearchAt = wolf.level().getGameTime() + wolf.getRandom().nextInt(100);
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        long now = this.wolf.level().getGameTime();
        if (now < this.nextPatrolAt || now < this.nextSearchAt || !canPatrol()) {
            return false;
        }

        // Do not perform a terrain/path query every tick.
        this.nextSearchAt = now + RETRY_INTERVAL_TICKS;
        return choosePatrolPath();
    }

    @Override
    public boolean canContinueToUse() {
        if (!canPatrol() || this.destination == null
                || this.wolf.level().getGameTime() >= this.stopAt
                || this.wolf.getNavigation().isDone()) {
            return false;
        }

        return this.wolf.distanceToSqr(
                this.destination.getX() + 0.5D,
                this.destination.getY(),
                this.destination.getZ() + 0.5D
        ) > ARRIVAL_DISTANCE_SQR;
    }

    @Override
    public void start() {
        this.stopAt = this.wolf.level().getGameTime() + MAX_PATROL_DURATION_TICKS;
        if (this.patrolPath == null
                || !this.wolf.getNavigation().moveTo(this.patrolPath, PATROL_SPEED)) {
            this.stopAt = this.wolf.level().getGameTime();
        }
    }

    @Override
    public void tick() {
        if (this.destination != null) {
            this.wolf.getLookControl().setLookAt(
                    this.destination.getX() + 0.5D,
                    this.destination.getY(),
                    this.destination.getZ() + 0.5D
            );
        }
    }

    @Override
    public void stop() {
        this.wolf.getNavigation().stop();
        this.destination = null;
        this.patrolPath = null;
        this.nextPatrolAt = this.wolf.level().getGameTime()
                + MIN_COOLDOWN_TICKS
                + this.wolf.getRandom().nextInt((int) (MAX_COOLDOWN_TICKS - MIN_COOLDOWN_TICKS + 1));
    }

    private boolean canPatrol() {
        if (!this.wolf.isAlive() || this.wolf.level().isClientSide
                || this.wolf.isTame() || this.wolf.isBaby()
                || !this.wolf.level().isNight()
                || !this.wolf.onGround() || this.wolf.isInWaterOrBubble()
                || this.wolf.isPassenger() || this.wolf.isInLove()
                || this.wolf.isOrderedToSit() || this.wolf.hurtTime > 0
                || this.wolf.getTarget() != null) {
            return false;
        }

        long now = this.wolf.level().getGameTime();
        if (now < this.wolf.getData(ModAttachments.WOLF_REST_UNTIL)
                || now < this.wolf.getData(ModAttachments.WOLF_MEAL_UNTIL)) {
            return false;
        }

        return !((WolfSleepState) this.wolf).natureIsAmazing$isSleeping();
    }

    private boolean choosePatrolPath() {
        BlockPos center = this.packHandler.getTerritoryCenter(this.wolf);
        if (center == null) {
            return false;
        }

        Level level = this.wolf.level();
        PathNavigation navigation = this.wolf.getNavigation();
        int territoryRadius = (int) WolfPackHandler.TERRITORY_RADIUS;
        int maxRadius = Math.min(OUTER_PATROL_RADIUS, territoryRadius - 3);

        for (int attempt = 0; attempt < TARGET_SEARCH_ATTEMPTS; attempt++) {
            double angle = this.wolf.getRandom().nextDouble() * (Math.PI * 2.0D);
            double radius = INNER_PATROL_RADIUS
                    + this.wolf.getRandom().nextDouble() * (maxRadius - INNER_PATROL_RADIUS);
            int x = center.getX() + (int) Math.round(Math.cos(angle) * radius);
            int z = center.getZ() + (int) Math.round(Math.sin(angle) * radius);

            BlockPos column = new BlockPos(x, center.getY(), z);
            if (!level.hasChunkAt(column)) {
                continue;
            }

            BlockPos candidate = level.getHeightmapPos(
                    Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, column
            );

            if (!isSafePatrolBlock(level, candidate, center)) {
                continue;
            }

            // A reachable path is selected once per patrol leg, not every tick.
            Path path = navigation.createPath(candidate, 0);
            if (path != null && path.canReach()) {
                this.destination = candidate.immutable();
                this.patrolPath = path;
                return true;
            }
        }

        return false;
    }

    private boolean isSafePatrolBlock(Level level, BlockPos pos, BlockPos center) {
        if (!level.hasChunkAt(pos) || !level.hasChunkAt(pos.below())) {
            return false;
        }

        if (!level.getBlockState(pos).isAir()
                || !level.getFluidState(pos).isEmpty()
                || !level.getBlockState(pos.below()).blocksMotion()) {
            return false;
        }

        double dx = pos.getX() - center.getX();
        double dz = pos.getZ() - center.getZ();
        double safeRadius = WolfPackHandler.TERRITORY_RADIUS - 2.0D;
        return dx * dx + dz * dz <= safeRadius * safeRadius;
    }
}
