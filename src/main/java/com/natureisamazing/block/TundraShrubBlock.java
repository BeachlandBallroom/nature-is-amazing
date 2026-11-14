package com.natureisamazing.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.state.BlockState;

/** Small tundra shrubs that can grow on dirt-like blocks and snow. */
public class TundraShrubBlock extends BushBlock {
    public static final MapCodec<TundraShrubBlock> CODEC = simpleCodec(TundraShrubBlock::new);

    public TundraShrubBlock(Properties properties) {
        super(properties);
    }

    @Override
    public MapCodec<TundraShrubBlock> codec() {
        return CODEC;
    }

    @Override
    protected boolean mayPlaceOn(BlockState state, BlockGetter level, BlockPos pos) {
        return state.is(BlockTags.DIRT) || state.is(Blocks.SNOW_BLOCK) || state.is(Blocks.SNOW);
    }
}
