package com.natureisamazing.ai.sheep;

import com.natureisamazing.data.ModAttachments;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Sheep;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Blocks;

import java.util.List;

/**
 * Persistent sheep well-being state and the small wool-yield bonus tied to it.
 * All methods which mutate attachments or spawn drops are server-only.
 */
public final class SheepStress {
    public static final int MAX_STRESS = 100;

    private static final double CROWD_RADIUS_XZ = 8.0D;
    private static final double CROWD_RADIUS_Y = 4.0D;

    /** A local flock of up to five sheep is treated as comfortable. */
    private static final int COMFORTABLE_FLOCK_SIZE = 5;

    /** Added after a successful shearing; it fades naturally in calm conditions. */
    private static final int SHEARING_STRESS = 8;

    private SheepStress() {
    }

    public static int getStress(Sheep sheep) {
        return clamp(sheep.getData(ModAttachments.SHEEP_STRESS));
    }

    public static void addStress(Sheep sheep, int amount) {
        if (sheep.level().isClientSide || amount <= 0) {
            return;
        }

        sheep.setData(
                ModAttachments.SHEEP_STRESS,
                Math.min(MAX_STRESS, getStress(sheep) + amount)
        );
    }

    private static void reduceStress(Sheep sheep, int amount) {
        if (sheep.level().isClientSide || amount <= 0) {
            return;
        }

        sheep.setData(
                ModAttachments.SHEEP_STRESS,
                Math.max(0, getStress(sheep) - amount)
        );
    }

    /**
     * Re-evaluates the local environment. Called only once every 200 ticks per
     * sheep, with entity-id staggering performed by SheepNutritionHandler.
     * The AABB query uses the world's entity-section index; it does not scan
     * all entities in the dimension.
     */
    public static void updateFromEnvironment(Sheep sheep) {
        if (sheep.level().isClientSide || !sheep.isAlive()) {
            return;
        }

        // One local indexed query gathers both sheep density and wild-wolf
        // presence. This avoids running two separate AABB searches per sample.
        List<LivingEntity> nearbyAnimals = sheep.level().getEntitiesOfClass(
                LivingEntity.class,
                sheep.getBoundingBox().inflate(CROWD_RADIUS_XZ, CROWD_RADIUS_Y, CROWD_RADIUS_XZ),
                entity -> entity.isAlive()
                        && (entity instanceof Sheep
                        || (entity instanceof Wolf wolf && !wolf.isTame()))
        );

        int nearbySheep = 0;
        boolean predatorNearby = false;
        for (LivingEntity entity : nearbyAnimals) {
            if (entity instanceof Sheep) {
                nearbySheep++;
            } else if (entity instanceof Wolf) {
                predatorNearby = true;
            }
        }
        // The count includes this sheep. Predators are checked in the same
        // local 8 x 4 x 8 block volume as flock density.

        int satiety = sheep.getData(ModAttachments.SHEEP_HUNGER);

        // Crowding starts above five sheep. Additional groups of three nearby
        // sheep add pressure, capped so one extremely dense pen cannot overflow.
        int crowdingPressure = nearbySheep <= COMFORTABLE_FLOCK_SIZE
                ? 0
                : Math.min(4, 1 + (nearbySheep - COMFORTABLE_FLOCK_SIZE - 1) / 3);

        int pressure = crowdingPressure;
        if (predatorNearby) {
            pressure += 8;
        }
        if (satiety < 25) {
            pressure += 2;
        } else if (satiety < 50) {
            pressure += 1;
        }

        if (pressure > 0) {
            addStress(sheep, pressure);
            return;
        }

        // Good food, no nearby wild predator and a little personal space let
        // the animal recover. A calm solitary sheep recovers fastest.
        if (nearbySheep <= 4 && satiety >= 60) {
            reduceStress(sheep, 4);
        } else if (nearbySheep <= COMFORTABLE_FLOCK_SIZE && satiety >= 40) {
            reduceStress(sheep, 2);
        }
    }

    /**
     * Returns the chance of one additional wool block. Vanilla's normal 1-3
     * drops are never reduced, and this feature adds at most one extra block.
     */
    public static float getBonusWoolChance(int stress) {
        int value = clamp(stress);
        if (value <= 19) {
            return 0.35F;
        }
        if (value <= 39) {
            return 0.20F;
        }
        if (value <= 59) {
            return 0.08F;
        }
        return 0.0F;
    }

    /** Called from SheepMixin at the end of the vanilla shear method. */
    public static void applyShearingOutcome(Sheep sheep) {
        if (sheep.level().isClientSide || !sheep.isSheared()) {
            return;
        }

        float bonusChance = getBonusWoolChance(getStress(sheep));
        if (bonusChance > 0.0F && sheep.getRandom().nextFloat() < bonusChance) {
            sheep.spawnAtLocation(woolForColor(sheep.getColor()), 3);
        }

        // Shearing is a brief disturbance. The normal recovery rules will
        // gradually lower this value after the sheep returns to calm conditions.
        addStress(sheep, SHEARING_STRESS);
    }

    private static ItemLike woolForColor(DyeColor color) {
        return switch (color) {
            case WHITE -> Blocks.WHITE_WOOL;
            case ORANGE -> Blocks.ORANGE_WOOL;
            case MAGENTA -> Blocks.MAGENTA_WOOL;
            case LIGHT_BLUE -> Blocks.LIGHT_BLUE_WOOL;
            case YELLOW -> Blocks.YELLOW_WOOL;
            case LIME -> Blocks.LIME_WOOL;
            case PINK -> Blocks.PINK_WOOL;
            case GRAY -> Blocks.GRAY_WOOL;
            case LIGHT_GRAY -> Blocks.LIGHT_GRAY_WOOL;
            case CYAN -> Blocks.CYAN_WOOL;
            case PURPLE -> Blocks.PURPLE_WOOL;
            case BLUE -> Blocks.BLUE_WOOL;
            case BROWN -> Blocks.BROWN_WOOL;
            case GREEN -> Blocks.GREEN_WOOL;
            case RED -> Blocks.RED_WOOL;
            case BLACK -> Blocks.BLACK_WOOL;
        };
    }

    private static int clamp(int stress) {
        return Math.max(0, Math.min(MAX_STRESS, stress));
    }
}
