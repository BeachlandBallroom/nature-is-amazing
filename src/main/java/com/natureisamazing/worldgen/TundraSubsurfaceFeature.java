package com.natureisamazing.worldgen;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

/**
 * Converts the solid underground stone layers of a Tundra chunk to deepslate.
 * It only touches ordinary stone host blocks; ores, caves, fluids, bedrock,
 * existing blocks, and columns belonging to another biome are left untouched.
 * The feature scans exactly one 16x16 chunk and therefore stays within the
 * world-generation write radius.
 */
public final class TundraSubsurfaceFeature extends Feature<NoneFeatureConfiguration> {
    public TundraSubsurfaceFeature() {
        super(NoneFeatureConfiguration.CODEC);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        BlockPos origin = context.origin();

        int chunkMinX = origin.getX() & ~15;
        int chunkMinZ = origin.getZ() & ~15;
        int minY = level.getMinBuildHeight() + 1;
        BlockState deepslate = Blocks.DEEPSLATE.defaultBlockState();
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        int changed = 0;

        for (int dx = 0; dx < 16; dx++) {
            int x = chunkMinX + dx;
            for (int dz = 0; dz < 16; dz++) {
                int z = chunkMinZ + dz;
                int surfaceY = level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, x, z) - 1;
                int lastUndergroundY = surfaceY - 3; // preserve the surface layers for surface rules and island features
                if (lastUndergroundY <= minY) {
                    continue;
                }

                // Biomes are normally vertically stable in the Overworld; sampling
                // near the surface protects neighbouring biome columns at borders.
                pos.set(x, Math.min(surfaceY, level.getMaxBuildHeight() - 1), z);
                if (!level.getBiome(pos).is(TundraBiomeKeys.TUNDRA)) {
                    continue;
                }

                for (int y = minY; y <= lastUndergroundY; y++) {
                    pos.setY(y);
                    BlockState current = level.getBlockState(pos);
                    if (isReplaceableStone(current)) {
                        if (level.setBlock(pos, deepslate, 2)) {
                            changed++;
                        }
                    }
                }
            }
        }

        return changed > 0;
    }

    private static boolean isReplaceableStone(BlockState state) {
        return state.is(Blocks.STONE)
                || state.is(Blocks.GRANITE)
                || state.is(Blocks.DIORITE)
                || state.is(Blocks.ANDESITE)
                || state.is(Blocks.TUFF);
    }
}
