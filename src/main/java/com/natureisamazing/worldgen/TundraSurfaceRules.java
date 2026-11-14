package com.natureisamazing.worldgen;

import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.SurfaceRules;
import terrablender.api.SurfaceRuleManager;

/** Applies the Tundra's dark rock material to exposed ground and cave surfaces. */
public final class TundraSurfaceRules {
    private TundraSurfaceRules() {
    }

    public static void register() {
        SurfaceRules.RuleSource deepslate = SurfaceRules.state(Blocks.DEEPSLATE.defaultBlockState());
        SurfaceRules.RuleSource tundraRock = SurfaceRules.sequence(
                SurfaceRules.ifTrue(SurfaceRules.ON_FLOOR, deepslate),
                SurfaceRules.ifTrue(SurfaceRules.UNDER_FLOOR, deepslate),
                SurfaceRules.ifTrue(SurfaceRules.DEEP_UNDER_FLOOR, deepslate),
                SurfaceRules.ifTrue(SurfaceRules.VERY_DEEP_UNDER_FLOOR, deepslate),
                SurfaceRules.ifTrue(SurfaceRules.ON_CEILING, deepslate),
                SurfaceRules.ifTrue(SurfaceRules.UNDER_CEILING, deepslate)
        );

        SurfaceRuleManager.addSurfaceRules(
                SurfaceRuleManager.RuleCategory.OVERWORLD,
                "natureisamazing",
                SurfaceRules.ifTrue(SurfaceRules.isBiome(TundraBiomeKeys.TUNDRA), tundraRock)
        );
    }
}
