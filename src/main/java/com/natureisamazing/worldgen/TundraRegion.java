package com.natureisamazing.worldgen;

import com.mojang.datafixers.util.Pair;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Climate;
import terrablender.api.Region;
import terrablender.api.RegionType;

import java.util.function.Consumer;

/** Adds a cold, dry, low-relief climate region to TerraBlender's Overworld. */
public final class TundraRegion extends Region {
    public TundraRegion(ResourceLocation name, int weight) {
        super(name, RegionType.OVERWORLD, weight);
    }

    @Override
    public void addBiomes(Registry<Biome> registry,
                          Consumer<Pair<Climate.ParameterPoint, ResourceKey<Biome>>> mapper) {
        Climate.ParameterPoint tundraClimate = Climate.parameters(
                Climate.Parameter.span(-1.0F, -0.35F),  // frozen to cold
                Climate.Parameter.span(-0.75F, 0.15F),  // dry to near-neutral
                Climate.Parameter.span(0.15F, 1.0F),   // inland
                Climate.Parameter.span(0.84F, 1.0F),   // strongly favors broad, eroded lowlands
                Climate.Parameter.span(-0.015F, 0.015F), // keep terrain depth very close to neutral
                Climate.Parameter.span(-0.05F, 0.05F), // reduce selection of strong ridges and peaks
                0L
        );
        mapper.accept(Pair.of(tundraClimate, TundraBiomeKeys.TUNDRA));
    }
}
