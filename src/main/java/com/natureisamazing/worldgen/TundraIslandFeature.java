package com.natureisamazing.worldgen;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;

/**
 * Generates a broad, gently irregular grass island on exposed deepslate/basalt.
 *
 * The maximum radius is intentionally 16 blocks: biome features are allowed to
 * write into the current chunk and its 8 neighbours (a 3x3 chunk region). A
 * larger radius could be clipped at the edge of that writable region.
 */
public final class TundraIslandFeature extends Feature<TundraIslandFeature.Configuration> {

    public TundraIslandFeature(Codec<Configuration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<Configuration> context) {
        WorldGenLevel level = context.level();
        BlockPos origin = context.origin();
        RandomSource random = context.random();
        Configuration config = context.config();

        int radius = config.radius().sample(random);
        int centerX = origin.getX();
        int centerZ = origin.getZ();
        int centerY = findSurfaceY(level, centerX, centerZ);

        if (centerY == Integer.MIN_VALUE) {
            return false;
        }

        // Low-frequency angular waves produce a natural outline without noisy pixels.
        double phaseA = random.nextDouble() * Math.PI * 2.0;
        double phaseB = random.nextDouble() * Math.PI * 2.0;
        double phaseC = random.nextDouble() * Math.PI * 2.0;
        double edgeRoughness = config.edgeRoughness();
        int maxRange = Math.min(16, radius + (int) Math.ceil(radius * edgeRoughness) + 1);
        int placedBlocks = 0;
        BlockState grass = Blocks.GRASS_BLOCK.defaultBlockState();

        for (int dx = -maxRange; dx <= maxRange; dx++) {
            for (int dz = -maxRange; dz <= maxRange; dz++) {
                if (dx == 0 && dz == 0) {
                    // The center is handled by the same checks below.
                }

                double distanceSquared = (double) dx * dx + (double) dz * dz;
                if (distanceSquared > (double) maxRange * maxRange) {
                    continue;
                }

                double angle = Math.atan2(dz, dx);
                double shape =
                        0.60 * Math.sin(3.0 * angle + phaseA)
                      + 0.27 * Math.sin(5.0 * angle + phaseB)
                      + 0.13 * Math.sin(7.0 * angle + phaseC);
                double localRadius = Math.min(16.0, radius * (1.0 + edgeRoughness * shape));

                if (distanceSquared > localRadius * localRadius) {
                    continue;
                }

                int x = centerX + dx;
                int z = centerZ + dz;
                int y = findSurfaceY(level, x, z);
                if (y == Integer.MIN_VALUE || Math.abs(y - centerY) > config.maxSurfaceVariation()) {
                    continue;
                }

                BlockPos surfacePos = new BlockPos(x, y, z);

                // A biome-filtered placed feature may reach into neighbouring chunks.
                // Re-check the biome at every write position to avoid painting adjacent biomes.
                if (!isTundraBiome(level, surfacePos)) {
                    continue;
                }

                BlockState surface = level.getBlockState(surfacePos);
                // Never replace grass, wool, shrubs, soil, snow, or any other feature.
                // Only exposed deepslate and smooth basalt are valid island substrate.
                if (!isTundraStone(surface)) {
                    continue;
                }

                // Guard the write radius explicitly; the configured radius is also capped at 16.
                if (!level.ensureCanWrite(surfacePos)) {
                    continue;
                }

                // Leave the blocks below the thin grass surface untouched.
                if (level.setBlock(surfacePos, grass, 2)) {
                    placedBlocks++;
                }
            }
        }

        return placedBlocks > 0;
    }

    /** Returns the top exposed deepslate/basalt block, or MIN_VALUE if none is found. */
    private static int findSurfaceY(WorldGenLevel level, int x, int z) {
        int y = level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, x, z) - 1;
        if (y < level.getMinBuildHeight()) {
            return Integer.MIN_VALUE;
        }

        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos(x, y, z);
        BlockState state = level.getBlockState(cursor);

        // Snow layers can sit above the rock surface. Ignore up to two thin layers,
        // but do not tunnel through other blocks or replace a snow block.
        int snowLayers = 0;
        while (state.is(Blocks.SNOW) && snowLayers < 2) {
            y--;
            snowLayers++;
            if (y < level.getMinBuildHeight()) {
                return Integer.MIN_VALUE;
            }
            cursor.setY(y);
            state = level.getBlockState(cursor);
        }

        return isTundraStone(state) ? y : Integer.MIN_VALUE;
    }


    private static boolean isTundraBiome(WorldGenLevel level, BlockPos pos) {
        return level.getBiome(pos).unwrapKey()
                .map(key -> key.location().equals(
                        ResourceLocation.fromNamespaceAndPath("natureisamazing", "tundra")))
                .orElse(false);
    }

    private static boolean isTundraStone(BlockState state) {
        return state.is(Blocks.DEEPSLATE) || state.is(Blocks.SMOOTH_BASALT);
    }

    public record Configuration(
            IntProvider radius,
            int maxSurfaceVariation,
            float edgeRoughness
    ) implements FeatureConfiguration {

        public static final Codec<Configuration> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                IntProvider.codec(1, 16).fieldOf("radius").forGetter(Configuration::radius),
                Codec.intRange(0, 8).optionalFieldOf("max_surface_variation", 3)
                        .forGetter(Configuration::maxSurfaceVariation),
                Codec.floatRange(0.0F, 0.20F).optionalFieldOf("edge_roughness", 0.07F)
                        .forGetter(Configuration::edgeRoughness)
        ).apply(instance, Configuration::new));
    }
}
