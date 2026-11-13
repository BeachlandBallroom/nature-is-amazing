package com.natureisamazing.ai.cow;

import com.natureisamazing.data.ModAttachments;
import net.minecraft.world.entity.animal.Cow;

/** Shared constants and state transitions for cow feeding and breeding. */
public final class CowNutrition {
    public static final int MAX_SATIETY = 100;
    public static final int GRASS_SATIETY_RESTORE = 18;
    public static final int BREEDING_SATIETY_REQUIRED = 70;
    public static final int BREEDING_SATIETY_COST = 35;

    private CowNutrition() {
    }

    public static void eatGrass(Cow cow) {
        if (cow.level().isClientSide) {
            return;
        }

        int satiety = cow.getData(ModAttachments.SHEEP_HUNGER);
        cow.setData(ModAttachments.SHEEP_HUNGER, Math.min(MAX_SATIETY, satiety + GRASS_SATIETY_RESTORE));
    }

    public static boolean canBreed(Cow cow) {
        return cow.getData(ModAttachments.SHEEP_HUNGER) >= BREEDING_SATIETY_REQUIRED;
    }

    public static void spendForBreeding(Cow cow) {
        int satiety = cow.getData(ModAttachments.SHEEP_HUNGER);
        cow.setData(ModAttachments.SHEEP_HUNGER, Math.max(0, satiety - BREEDING_SATIETY_COST));
    }
}
