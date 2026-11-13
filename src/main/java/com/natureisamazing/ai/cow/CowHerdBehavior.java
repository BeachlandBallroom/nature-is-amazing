package com.natureisamazing.ai.cow;

import net.minecraft.world.entity.animal.Cow;

/** Stable, lightweight sex classification for wild-cow social behaviour. */
final class CowHerdBehavior {
    private CowHerdBehavior() {
    }

    /** About one in four cow is treated as a ram for herd AI purposes. */
    static boolean isBull(Cow cow) {
        return (cow.getUUID().getLeastSignificantBits() & 3L) == 0L;
    }
}