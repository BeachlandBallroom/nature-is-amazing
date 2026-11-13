package com.natureisamazing.ai.wolf;

import net.minecraft.world.entity.animal.Wolf;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;

import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;

/** Installs the daytime sleep goal on each server-side vanilla wolf once. */
public final class WolfSleepHandler {
    private final Set<Wolf> configuredWolves = Collections.newSetFromMap(new WeakHashMap<>());

    @SubscribeEvent
    public void onEntityJoinLevel(EntityJoinLevelEvent event) {
        if (!(event.getEntity() instanceof Wolf wolf) || wolf.level().isClientSide) {
            return;
        }

        if (configuredWolves.add(wolf)) {
            wolf.goalSelector.addGoal(1, new WolfDaySleepGoal(wolf));
        }
    }
}
