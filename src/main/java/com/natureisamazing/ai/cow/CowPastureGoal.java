package com.natureisamazing.ai.cow;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.animal.Cow;
import net.minecraft.world.level.block.Blocks;

import java.util.EnumSet;

/** Periodically moves a cow to nearby grass before it resumes vanilla grazing. */
public final class CowPastureGoal extends Goal {
    private static final int SEARCH_RADIUS = 12;
    private static final int SEARCH_ATTEMPTS = 32;
    private static final int GRAZE_TICKS = 80;

    private final Cow cow;
    private BlockPos pasture;
    private int grazingTicks;

    public CowPastureGoal(Cow cow) {
        this.cow = cow;
        this.setFlags(EnumSet.of(Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        if (this.cow.getRandom().nextInt(100) != 0) {
            return false;
        }

        BlockPos origin = this.cow.blockPosition();
        for (int attempt = 0; attempt < SEARCH_ATTEMPTS; attempt++) {
            BlockPos candidate = origin.offset(
                    this.cow.getRandom().nextInt(SEARCH_RADIUS * 2 + 1) - SEARCH_RADIUS,
                    this.cow.getRandom().nextInt(5) - 2,
                    this.cow.getRandom().nextInt(SEARCH_RADIUS * 2 + 1) - SEARCH_RADIUS
            );
            if (this.cow.level().getBlockState(candidate).is(Blocks.GRASS_BLOCK)
                    && this.cow.level().getBlockState(candidate.above()).isAir()) {
                this.pasture = candidate;
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean canContinueToUse() {
        return this.pasture != null && this.grazingTicks < GRAZE_TICKS;
    }

    @Override
    public void start() {
        this.grazingTicks = 0;
        this.cow.getNavigation().moveTo(
                this.pasture.getX() + 0.5D,
                this.pasture.getY() + 1.0D,
                this.pasture.getZ() + 0.5D,
                0.8D
        );
    }

    @Override
    public void tick() {
        if (this.cow.distanceToSqr(
                this.pasture.getX() + 0.5D,
                this.pasture.getY() + 1.0D,
                this.pasture.getZ() + 0.5D) <= 4.0D) {
            this.cow.getNavigation().stop();
            this.grazingTicks++;
        }
    }

    @Override
    public void stop() {
        this.pasture = null;
    }
}
