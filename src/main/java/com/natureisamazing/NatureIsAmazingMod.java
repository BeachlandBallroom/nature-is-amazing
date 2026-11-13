package com.natureisamazing;

import com.natureisamazing.ai.chicken.ChickenAIHandler;
import com.natureisamazing.ai.cow.CowAIHandler;
import com.natureisamazing.ai.sheep.SheepAIHandler;
import com.natureisamazing.ai.sheep.SheepNutritionHandler;
import com.natureisamazing.ai.wolf.WolfDayCycleHandler;
import com.natureisamazing.ai.wolf.WolfHungerHandler;
import com.natureisamazing.ai.wolf.WolfPackHandler;
import com.natureisamazing.ai.wolf.WolfSleepHandler;
import com.natureisamazing.animatable.SleepingWolfAnimatable;
import com.natureisamazing.data.ModAttachments;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;

/**
 * Main entry point of the Animal Behavior mod.
 */
@Mod(NatureIsAmazingMod.MOD_ID)
public final class NatureIsAmazingMod {
    public static final String MOD_ID = "natureisamazing";

    public static final SleepingWolfAnimatable SLEEPING_WOLF = new SleepingWolfAnimatable();

    public NatureIsAmazingMod(IEventBus modEventBus) {
        // Register custom entity data attachments on the mod event bus.
        ModAttachments.ATTACHMENTS.register(modEventBus);

        // Register gameplay event handlers on the NeoForge event bus.
        WolfPackHandler wolfPackHandler = new WolfPackHandler();
        NeoForge.EVENT_BUS.register(wolfPackHandler);
        NeoForge.EVENT_BUS.register(new WolfHungerHandler(wolfPackHandler));
        NeoForge.EVENT_BUS.register(new WolfDayCycleHandler());
        NeoForge.EVENT_BUS.register(new WolfSleepHandler(wolfPackHandler));
        NeoForge.EVENT_BUS.register(new ChickenAIHandler());
        NeoForge.EVENT_BUS.register(new SheepAIHandler());
        NeoForge.EVENT_BUS.register(new SheepNutritionHandler());
        NeoForge.EVENT_BUS.register(new CowAIHandler());
    }
}
