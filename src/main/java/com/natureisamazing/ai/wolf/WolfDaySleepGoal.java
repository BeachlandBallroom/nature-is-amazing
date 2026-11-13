package com.natureisamazing.ai.wolf;

import com.natureisamazing.data.ModAttachments;
import com.natureisamazing.api.WolfSleepState;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.animal.Wolf;

import java.util.EnumSet;

/**
 * Makes an untamed adult wolf rest during the Minecraft daytime.
 * The server owns this state; vanilla synced entity data sends it to clients.
 */
public final class WolfDaySleepGoal extends Goal {
    private static final long FULL_DAY_TICKS = 24_000L;
    private static final long DAY_END_TICK = 12_000L;

    private final Wolf wolf;

    public WolfDaySleepGoal(Wolf wolf) {
        this.wolf = wolf;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK, Flag.JUMP));
    }

    private boolean isDaytime() {
        long timeOfDay = Math.floorMod(this.wolf.level().getDayTime(), FULL_DAY_TICKS);
        return timeOfDay < DAY_END_TICK;
    }

    private boolean canSleepNow() {
        return this.wolf.isAlive()
                && !this.wolf.isTame()
                && !this.wolf.isBaby()
                && this.wolf.onGround()
                && !this.wolf.isInWaterOrBubble()
                && !this.wolf.isPassenger()
                && this.wolf.getTarget() == null
                && this.wolf.hurtTime == 0
                && this.wolf.level().getGameTime() >= this.wolf.getData(ModAttachments.WOLF_MEAL_UNTIL)
                // Wolves first use WolfDayCycleHandler to reach shade. Without
                // this condition the high-priority sleep goal starts outdoors
                // and immediately cancels its own navigation every tick.
                && !this.wolf.level().canSeeSky(this.wolf.blockPosition())
                && WolfDayBehavior.shouldSleep(this.wolf)
                && isDaytime();
    }

    @Override
    public boolean canUse() {
        return canSleepNow();
    }

    @Override
    public boolean canContinueToUse() {
        // The wolf wakes at dusk, when hurt, or as soon as it gets a target.
        return canSleepNow();
    }

    @Override
    public void start() {
        this.wolf.getNavigation().stop();
        ((WolfSleepState) this.wolf).natureIsAmazing$setSleeping(true);
    }

    @Override
    public void tick() {
        this.wolf.getNavigation().stop();
        // Prevent residual horizontal motion when the goal first takes control.
        this.wolf.setDeltaMovement(0.0D, this.wolf.getDeltaMovement().y, 0.0D);
    }

    @Override
    public void stop() {
        this.wolf.getNavigation().stop();
        ((WolfSleepState) this.wolf).natureIsAmazing$setSleeping(false);
    }
}
