package com.natureisamazing;

import com.natureisamazing.ai.chicken.ChickenAIHandler;
import com.natureisamazing.ai.wolf.WolfHungerHandler;
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

    public NatureIsAmazingMod(IEventBus modEventBus) {
        // Register custom entity data attachments on the mod event bus.
        ModAttachments.ATTACHMENTS.register(modEventBus);

        // Register gameplay event handlers on the NeoForge event bus.
        NeoForge.EVENT_BUS.register(new WolfHungerHandler());
        NeoForge.EVENT_BUS.register(new ChickenAIHandler());
    }
}
