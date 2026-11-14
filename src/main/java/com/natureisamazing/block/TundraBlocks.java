package com.natureisamazing.block;

import com.mojang.serialization.MapCodec;
import com.natureisamazing.NatureIsAmazingMod;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CarpetBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;

/** Blocks and block items used by the Tundra biome. */
public final class TundraBlocks {
    private static final DeferredRegister<MapCodec<? extends Block>> BLOCK_TYPES =
            DeferredRegister.create(BuiltInRegistries.BLOCK_TYPE, NatureIsAmazingMod.MOD_ID);
    private static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(BuiltInRegistries.BLOCK, NatureIsAmazingMod.MOD_ID);
    private static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(BuiltInRegistries.ITEM, NatureIsAmazingMod.MOD_ID);

    private static BlockBehaviour.Properties shrubProperties() {
        return BlockBehaviour.Properties.of().noCollission().instabreak().sound(SoundType.GRASS);
    }

    private static BlockBehaviour.Properties logProperties() {
        return BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(2.0F)
                .sound(SoundType.WOOD).requiresCorrectToolForDrops();
    }

    private static final DeferredHolder<
            MapCodec<? extends Block>, MapCodec<TundraShrubBlock>> TUNDRA_SHRUB_CODEC =
            BLOCK_TYPES.register("tundra_shrub", () -> TundraShrubBlock.CODEC);

    public static final DeferredHolder<Block, Block> ORANGE_SHRUB = BLOCKS.register(
            "orange_shrub", () -> new TundraShrubBlock(shrubProperties()));
    public static final DeferredHolder<Block, Block> STUNTED_RED_SHRUB = BLOCKS.register(
            "stunted_red_shrub", () -> new TundraShrubBlock(shrubProperties()));
    public static final DeferredHolder<Block, Block> YELLOW_SHRUB = BLOCKS.register(
            "yellow_shrub", () -> new TundraShrubBlock(shrubProperties()));
    public static final DeferredHolder<Block, Block> YELLOW_MOSS_CARPET = BLOCKS.register(
            "yellow_moss_carpet", () -> new CarpetBlock(
                    BlockBehaviour.Properties.of().noCollission().strength(0.1F).sound(SoundType.WOOL)));
    public static final DeferredHolder<Block, Block> ORANGE_WOOL_CARPET = BLOCKS.register(
            "orange_wool_carpet", () -> new CarpetBlock(
                    BlockBehaviour.Properties.of().noCollission().strength(0.1F).sound(SoundType.WOOL)));
    public static final DeferredHolder<Block, Block> RED_WOOL_CARPET = BLOCKS.register(
            "red_wool_carpet", () -> new CarpetBlock(
                    BlockBehaviour.Properties.of().noCollission().strength(0.1F).sound(SoundType.WOOL)));

    public static final DeferredHolder<Block, Block> DWARFED_SPRUCE_LOG = BLOCKS.register(
            "dwarfed_spruce_log", () -> new RotatedPillarBlock(logProperties()));
    public static final DeferredHolder<Block, Block> STRIPPED_DWARFED_SPRUCE_LOG = BLOCKS.register(
            "stripped_dwarfed_spruce_log", () -> new RotatedPillarBlock(logProperties()));

    public static final DeferredHolder<Item, Item> ORANGE_SHRUB_ITEM = blockItem("orange_shrub", ORANGE_SHRUB);
    public static final DeferredHolder<Item, Item> STUNTED_RED_SHRUB_ITEM = blockItem("stunted_red_shrub", STUNTED_RED_SHRUB);
    public static final DeferredHolder<Item, Item> YELLOW_SHRUB_ITEM = blockItem("yellow_shrub", YELLOW_SHRUB);
    public static final DeferredHolder<Item, Item> YELLOW_MOSS_CARPET_ITEM = blockItem("yellow_moss_carpet", YELLOW_MOSS_CARPET);
    public static final DeferredHolder<Item, Item> ORANGE_WOOL_CARPET_ITEM = blockItem("orange_wool_carpet", ORANGE_WOOL_CARPET);
    public static final DeferredHolder<Item, Item> RED_WOOL_CARPET_ITEM = blockItem("red_wool_carpet", RED_WOOL_CARPET);
    public static final DeferredHolder<Item, Item> DWARFED_SPRUCE_LOG_ITEM = blockItem("dwarfed_spruce_log", DWARFED_SPRUCE_LOG);
    public static final DeferredHolder<Item, Item> STRIPPED_DWARFED_SPRUCE_LOG_ITEM = blockItem("stripped_dwarfed_spruce_log", STRIPPED_DWARFED_SPRUCE_LOG);

    private static DeferredHolder<Item, Item> blockItem(String name, DeferredHolder<Block, Block> block) {
        return ITEMS.register(name, () -> new BlockItem(block.get(), new Item.Properties()));
    }

    private TundraBlocks() {
    }

    public static void register(IEventBus modEventBus) {
        BLOCK_TYPES.register(modEventBus);
        BLOCKS.register(modEventBus);
        ITEMS.register(modEventBus);
    }

    public static void addCreativeTabItems(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.NATURAL_BLOCKS) {
            event.accept(ORANGE_SHRUB_ITEM.get());
            event.accept(STUNTED_RED_SHRUB_ITEM.get());
            event.accept(YELLOW_SHRUB_ITEM.get());
            event.accept(YELLOW_MOSS_CARPET_ITEM.get());
            event.accept(ORANGE_WOOL_CARPET_ITEM.get());
            event.accept(RED_WOOL_CARPET_ITEM.get());
        }
        if (event.getTabKey() == CreativeModeTabs.BUILDING_BLOCKS) {
            event.accept(DWARFED_SPRUCE_LOG_ITEM.get());
            event.accept(STRIPPED_DWARFED_SPRUCE_LOG_ITEM.get());
        }
    }
}
