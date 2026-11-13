package com.natureisamazing.ai.sheep;

import net.minecraft.world.entity.animal.Sheep;

/** Stable, lightweight sex classification for wild-sheep social behaviour. */
final class SheepHerdBehavior {
    private SheepHerdBehavior() {
    }

    /** About one in four sheep is treated as a ram for herd AI purposes. */
    static boolean isRam(Sheep sheep) {
        return (sheep.getUUID().getLeastSignificantBits() & 3L) == 0L;
    }
}