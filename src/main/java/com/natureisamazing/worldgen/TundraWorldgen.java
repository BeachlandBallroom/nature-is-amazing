package com.natureisamazing.worldgen;

import com.natureisamazing.NatureIsAmazingMod;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import terrablender.api.Regions;

/** Registers the Tundra's Overworld region after TerraBlender has initialized. */
public final class TundraWorldgen {
    private TundraWorldgen() {
    }

    public static void register(IEventBus modEventBus) {
        modEventBus.addListener(TundraWorldgen::commonSetup);
    }

    private static void commonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            Regions.register(new TundraRegion(
                    ResourceLocation.fromNamespaceAndPath(NatureIsAmazingMod.MOD_ID, "overworld"),
                    5
            ));
            TundraSurfaceRules.register();
        });
    }
}
