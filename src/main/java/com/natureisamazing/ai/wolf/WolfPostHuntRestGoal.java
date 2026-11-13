package com.natureisamazing.ai.wolf;

import com.natureisamazing.api.WolfSleepState;
import com.natureisamazing.data.ModAttachments;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.animal.Wolf;

import java.util.EnumSet;

/** Keeps a wolf still for a short time after its pack has made a kill. */
public final class WolfPostHuntRestGoal extends Goal {
    private final Wolf wolf;

    public WolfPostHuntRestGoal(Wolf wolf) {
        this.wolf = wolf;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK, Flag.JUMP));
    }

    private boolean canRestNow() {
        return this.wolf.isAlive()
                && !this.wolf.isTame()
                && this.wolf.onGround()
                && !this.wolf.isInWaterOrBubble()
                && this.wolf.getTarget() == null
                && this.wolf.hurtTime == 0
                && this.wolf.level().getGameTime() >= this.wolf.getData(ModAttachments.WOLF_MEAL_UNTIL)
                && this.wolf.level().getGameTime() < this.wolf.getData(ModAttachments.WOLF_REST_UNTIL);
    }

    @Override
    public boolean canUse() {
        return canRestNow();
    }

    @Override
    public boolean canContinueToUse() {
        return canRestNow();
    }

    @Override
    public void start() {
        this.wolf.getNavigation().stop();
        ((WolfSleepState) this.wolf).natureIsAmazing$setSleeping(true);
    }

    @Override
    public void tick() {
        this.wolf.getNavigation().stop();
        this.wolf.setDeltaMovement(0.0D, this.wolf.getDeltaMovement().y, 0.0D);
    }

    @Override
    public void stop() {
        ((WolfSleepState) this.wolf).natureIsAmazing$setSleeping(false);
    }
}
