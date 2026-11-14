package com.natureisamazing.worldgen;

import com.natureisamazing.NatureIsAmazingMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biome;

/** Dynamic biome key; the biome itself is defined in data/natureisamazing/worldgen/biome/tundra.json. */
public final class TundraBiomeKeys {
    public static final ResourceKey<Biome> TUNDRA = ResourceKey.create(
            Registries.BIOME,
            ResourceLocation.fromNamespaceAndPath(NatureIsAmazingMod.MOD_ID, "tundra")
    );

    private TundraBiomeKeys() {
    }
}
