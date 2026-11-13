package com.natureisamazing.ai.sheep;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.animal.Sheep;
import net.minecraft.world.level.block.Blocks;

import java.util.EnumSet;

/** Periodically moves a sheep to nearby grass before it resumes vanilla grazing. */
public final class SheepPastureGoal extends Goal {
    private static final int SEARCH_RADIUS = 12;
    private static final int SEARCH_ATTEMPTS = 32;
    private static final int GRAZE_TICKS = 80;

    private final Sheep sheep;
    private BlockPos pasture;
    private int grazingTicks;

    public SheepPastureGoal(Sheep sheep) {
        this.sheep = sheep;
        this.setFlags(EnumSet.of(Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        if (this.sheep.getRandom().nextInt(100) != 0) {
            return false;
        }

        BlockPos origin = this.sheep.blockPosition();
        for (int attempt = 0; attempt < SEARCH_ATTEMPTS; attempt++) {
            BlockPos candidate = origin.offset(
                    this.sheep.getRandom().nextInt(SEARCH_RADIUS * 2 + 1) - SEARCH_RADIUS,
                    this.sheep.getRandom().nextInt(5) - 2,
                    this.sheep.getRandom().nextInt(SEARCH_RADIUS * 2 + 1) - SEARCH_RADIUS
            );
            if (this.sheep.level().getBlockState(candidate).is(Blocks.GRASS_BLOCK)
                    && this.sheep.level().getBlockState(candidate.above()).isAir()) {
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
        this.sheep.getNavigation().moveTo(
                this.pasture.getX() + 0.5D,
                this.pasture.getY() + 1.0D,
                this.pasture.getZ() + 0.5D,
                0.8D
        );
    }

    @Override
    public void tick() {
        if (this.sheep.distanceToSqr(
                this.pasture.getX() + 0.5D,
                this.pasture.getY() + 1.0D,
                this.pasture.getZ() + 0.5D) <= 4.0D) {
            this.sheep.getNavigation().stop();
            this.grazingTicks++;
        }
    }

    @Override
    public void stop() {
        this.pasture = null;
    }
}
