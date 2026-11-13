package com.natureisamazing.ai.wolf;

import net.minecraft.world.entity.animal.Wolf;

import java.util.Map;
import java.util.WeakHashMap;

/**
 * Chooses one daytime activity per wild wolf and per Minecraft day. The state
 * is intentionally runtime-only: a reloaded wolf simply chooses a fresh plan.
 */
final class WolfDayBehavior {
    private static final long FULL_DAY_TICKS = 24_000L;
    private static final int SLEEP_CHANCE = 45;
    private static final int SHADE_WANDER_CHANCE = 35;
    private static final int ROAM_CHANCE = 17;

    private static final Map<Wolf, DayPlan> PLANS = new WeakHashMap<>();

    private WolfDayBehavior() {
    }

    static Activity activity(Wolf wolf) {
        return getPlan(wolf).activity();
    }

    static boolean shouldSleep(Wolf wolf) {
        DayPlan plan = getPlan(wolf);
        return plan.activity() == Activity.SLEEP
                && wolf.level().getGameTime() >= plan.sleepAfterGameTick();
    }

    private static DayPlan getPlan(Wolf wolf) {
        long currentDay = Math.floorDiv(wolf.level().getDayTime(), FULL_DAY_TICKS);
        DayPlan previous = PLANS.get(wolf);
        if (previous != null && previous.day() == currentDay) {
            return previous;
        }

        int roll = wolf.getRandom().nextInt(100);
        Activity activity = roll < SLEEP_CHANCE ? Activity.SLEEP
                : roll < SLEEP_CHANCE + SHADE_WANDER_CHANCE ? Activity.SHADE_WANDER
                : roll < SLEEP_CHANCE + SHADE_WANDER_CHANCE + ROAM_CHANCE ? Activity.ROAM
                : Activity.HUNT;

        // A wolf selected to sleep wanders or rests in shade for 10–50 seconds
        // before it is even allowed to start the sleep goal.
        long sleepAfter = wolf.level().getGameTime() + 200L + wolf.getRandom().nextInt(801);
        DayPlan plan = new DayPlan(currentDay, activity, sleepAfter);
        PLANS.put(wolf, plan);
        return plan;
    }

    enum Activity {
        SLEEP,
        SHADE_WANDER,
        ROAM,
        HUNT
    }

    private record DayPlan(long day, Activity activity, long sleepAfterGameTick) {
    }
}
