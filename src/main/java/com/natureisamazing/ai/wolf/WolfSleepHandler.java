package com.natureisamazing.ai.wolf;

import net.minecraft.world.entity.animal.Wolf;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;

import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;

/** Installs the daytime sleep goal on each server-side vanilla wolf once. */
public final class WolfSleepHandler {
    private final WolfPackHandler packHandler;
    private final Set<Wolf> configuredWolves = Collections.newSetFromMap(new WeakHashMap<>());

    public WolfSleepHandler(WolfPackHandler packHandler) {
        this.packHandler = packHandler;
    }

    @SubscribeEvent
    public void onEntityJoinLevel(EntityJoinLevelEvent event) {
        if (!(event.getEntity() instanceof Wolf wolf) || wolf.level().isClientSide) {
            return;
        }

        if (configuredWolves.add(wolf)) {
            wolf.goalSelector.addGoal(0, new WolfEatDroppedMeatGoal(wolf, this.packHandler));
            // A completed hunt takes precedence over routine daytime sleep.
            wolf.goalSelector.addGoal(1, new WolfPostHuntRestGoal(wolf));
            wolf.goalSelector.addGoal(2, new WolfDaySleepGoal(wolf));
            // Priority 6: patrol takes over idle wandering, but attacks,
            // swimming, sleeping and eating still interrupt it.
            wolf.goalSelector.addGoal(6, new WolfPatrolGoal(wolf, this.packHandler));
        }
    }
}
