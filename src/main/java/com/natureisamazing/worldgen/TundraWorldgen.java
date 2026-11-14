package com.natureisamazing.worldgen;

import com.natureisamazing.NatureIsAmazingMod;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import terrablender.api.Regions;

/** Registers custom feature types, Tundra surface rules, and the TerraBlender region. */
public final class TundraWorldgen {
    private TundraWorldgen() {
    }

    public static void register(IEventBus modEventBus) {
        TundraFeatures.FEATURES.register(modEventBus);
        modEventBus.addListener(TundraWorldgen::commonSetup);
    }

    private static void commonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            Regions.register(new TundraRegion(
                    ResourceLocation.fromNamespaceAndPath(NatureIsAmazingMod.MOD_ID, "overworld"),
                    5
            ));
            // Important: the old implementation did not always call this method,
            // so the deepslate surface material rule could remain inactive.
            TundraSurfaceRules.register();
        });
    }
}
