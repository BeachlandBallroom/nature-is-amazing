package com.natureisamazing.ai.sheep;

import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.animal.Sheep;
import net.minecraft.world.entity.animal.Wolf;

import java.util.Comparator;
import java.util.EnumSet;

/** A ram briefly drives a wolf away when it threatens nearby lambs. */
public final class RamGuardGoal extends Goal {
    private static final double PROTECT_RADIUS = 10.0D;
    private static final double THREAT_RADIUS = 8.0D;
    private static final double CHARGE_SPEED = 1.35D;

    private final Sheep ram;
    private Wolf threat;
    private int headbuttCooldown;

    public RamGuardGoal(Sheep ram) {
        this.ram = ram;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        this.threat = findThreat();
        return this.threat != null && lambIsNearby();
    }

    @Override
    public boolean canContinueToUse() {
        return this.threat != null
                && this.threat.isAlive()
                && this.ram.distanceToSqr(this.threat) <= PROTECT_RADIUS * PROTECT_RADIUS
                && lambIsNearby();
    }

    @Override
    public void start() {
        this.headbuttCooldown = 0;
    }

    @Override
    public void stop() {
        this.threat = null;
    }

    @Override
    public void tick() {
        if (this.threat == null) {
            return;
        }

        this.ram.getLookControl().setLookAt(this.threat, 30.0F, 30.0F);
        this.ram.getNavigation().moveTo(this.threat, CHARGE_SPEED);
        if (this.headbuttCooldown > 0) {
            this.headbuttCooldown--;
        }

        if (this.headbuttCooldown == 0 && this.ram.distanceToSqr(this.threat) <= 4.0D) {
            this.threat.hurt(this.ram.damageSources().mobAttack(this.ram), 2.0F);
            this.threat.knockback(0.8D, this.ram.getX() - this.threat.getX(), this.ram.getZ() - this.threat.getZ());
            this.headbuttCooldown = 40;
        }
    }

    private Wolf findThreat() {
        return this.ram.level().getEntitiesOfClass(
                        Wolf.class,
                        this.ram.getBoundingBox().inflate(THREAT_RADIUS),
                        Wolf::isAlive
                )
                .stream()
                .min(Comparator.comparingDouble(this.ram::distanceToSqr))
                .orElse(null);
    }

    private boolean lambIsNearby() {
        return !this.ram.level().getEntitiesOfClass(
                Sheep.class,
                this.ram.getBoundingBox().inflate(PROTECT_RADIUS),
                Sheep::isBaby
        ).isEmpty();
    }
}
