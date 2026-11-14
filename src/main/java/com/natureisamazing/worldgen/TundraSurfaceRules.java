package com.natureisamazing.worldgen;

import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.SurfaceRules;
import terrablender.api.SurfaceRuleManager;

/** Makes deepslate the exposed stone surface only in Nature Is Amazing's tundra. */
public final class TundraSurfaceRules {
    private TundraSurfaceRules() {}

    public static void register() {
        SurfaceRules.RuleSource tundraRock = SurfaceRules.sequence(
                SurfaceRules.ifTrue(SurfaceRules.ON_FLOOR, SurfaceRules.state(Blocks.DEEPSLATE.defaultBlockState())),
                SurfaceRules.ifTrue(SurfaceRules.UNDER_FLOOR, SurfaceRules.state(Blocks.DEEPSLATE.defaultBlockState()))
        );
        SurfaceRuleManager.addSurfaceRules(
                SurfaceRuleManager.RuleCategory.OVERWORLD,
                "natureisamazing",
                SurfaceRules.ifTrue(SurfaceRules.isBiome(TundraBiomeKeys.TUNDRA), tundraRock)
        );
    }
}
