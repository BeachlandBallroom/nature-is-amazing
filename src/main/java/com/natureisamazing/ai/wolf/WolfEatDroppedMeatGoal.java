package com.natureisamazing.ai.wolf;

import com.natureisamazing.data.ModAttachments;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.Comparator;
import java.util.EnumSet;

/** Makes a hungry wolf collect and eat raw meat dropped by its own recent kill. */
public final class WolfEatDroppedMeatGoal extends Goal {
    private static final double SEARCH_RADIUS = 12.0D;
    private static final double EAT_DISTANCE_SQUARED = 6.25D;
    private static final int HUNGER_RESTORED_PER_MEAT = 45;

    private final Wolf wolf;
    private final WolfPackHandler packHandler;
    private ItemEntity meal;

    public WolfEatDroppedMeatGoal(Wolf wolf, WolfPackHandler packHandler) {
        this.wolf = wolf;
        this.packHandler = packHandler;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        this.meal = findMeal();
        return this.meal != null;
    }

    @Override
    public boolean canContinueToUse() {
        return hasPendingMeal() && this.meal != null && this.meal.isAlive();
    }

    @Override
    public void start() {
        moveToMeal();
    }

    @Override
    public void tick() {
        if (this.meal == null || !this.meal.isAlive()) {
            this.meal = findMeal();
            if (this.meal == null) {
                return;
            }
        }

        this.wolf.getLookControl().setLookAt(this.meal, 30.0F, 30.0F);
        if (this.wolf.distanceToSqr(this.meal) > EAT_DISTANCE_SQUARED) {
            moveToMeal();
            return;
        }

        ItemStack stack = this.meal.getItem();
        stack.shrink(1);
        if (stack.isEmpty()) {
            this.meal.discard();
        } else {
            this.meal.setItem(stack);
        }

        int hunger = this.wolf.getData(ModAttachments.WOLF_HUNGER);
        this.wolf.setData(ModAttachments.WOLF_HUNGER, Math.min(100, hunger + HUNGER_RESTORED_PER_MEAT));
        this.wolf.setData(ModAttachments.WOLF_MEAL_UNTIL, 0L);
        this.packHandler.beginPostHuntRest(this.wolf);
    }

    @Override
    public void stop() {
        this.meal = null;
    }

    private boolean hasPendingMeal() {
        return this.wolf.level().getGameTime() < this.wolf.getData(ModAttachments.WOLF_MEAL_UNTIL);
    }

    private ItemEntity findMeal() {
        if (!hasPendingMeal()) {
            return null;
        }

        return this.wolf.level().getEntitiesOfClass(
                        ItemEntity.class,
                        this.wolf.getBoundingBox().inflate(SEARCH_RADIUS),
                        item -> isPreyMeat(item.getItem())
                )
                .stream()
                .min(Comparator.comparingDouble(this.wolf::distanceToSqr))
                .orElse(null);
    }

    private void moveToMeal() {
        this.wolf.getNavigation().moveTo(this.meal, 1.2D);
    }

    private boolean isPreyMeat(ItemStack stack) {
        return stack.is(Items.BEEF)
                || stack.is(Items.MUTTON)
                || stack.is(Items.RABBIT)
                || stack.is(Items.CHICKEN);
    }
}
