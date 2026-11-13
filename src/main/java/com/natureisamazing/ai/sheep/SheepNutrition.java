package com.natureisamazing.ai.sheep;

import com.natureisamazing.data.ModAttachments;
import net.minecraft.world.entity.animal.Sheep;

/** Shared constants and state transitions for sheep feeding and breeding. */
public final class SheepNutrition {
    public static final int MAX_SATIETY = 100;
    public static final int GRASS_SATIETY_RESTORE = 18;
    public static final int BREEDING_SATIETY_REQUIRED = 70;
    public static final int BREEDING_SATIETY_COST = 35;

    private SheepNutrition() {
    }

    public static void eatGrass(Sheep sheep) {
        if (sheep.level().isClientSide) {
            return;
        }

        int satiety = sheep.getData(ModAttachments.SHEEP_HUNGER);
        sheep.setData(ModAttachments.SHEEP_HUNGER, Math.min(MAX_SATIETY, satiety + GRASS_SATIETY_RESTORE));
    }

    public static boolean canBreed(Sheep sheep) {
        return sheep.getData(ModAttachments.SHEEP_HUNGER) >= BREEDING_SATIETY_REQUIRED;
    }

    public static void spendForBreeding(Sheep sheep) {
        int satiety = sheep.getData(ModAttachments.SHEEP_HUNGER);
        sheep.setData(ModAttachments.SHEEP_HUNGER, Math.max(0, satiety - BREEDING_SATIETY_COST));
    }
}
