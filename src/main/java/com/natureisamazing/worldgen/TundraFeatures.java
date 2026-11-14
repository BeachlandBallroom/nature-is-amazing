package com.natureisamazing.worldgen;

import com.natureisamazing.NatureIsAmazingMod;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

/** Static registry for custom Tundra world-generation feature types. */
public final class TundraFeatures {
    public static final DeferredRegister<Feature<?>> FEATURES =
            DeferredRegister.create(BuiltInRegistries.FEATURE, NatureIsAmazingMod.MOD_ID);

    public static final Supplier<Feature<TundraIslandFeature.Configuration>> TUNDRA_ISLAND =
            FEATURES.register("tundra_island", () ->
                    new TundraIslandFeature(TundraIslandFeature.Configuration.CODEC));

    public static final Supplier<Feature<NoneFeatureConfiguration>> TUNDRA_SUBSURFACE_STONE =
            FEATURES.register("tundra_subsurface_stone", TundraSubsurfaceFeature::new);

    private TundraFeatures() {
    }
}
