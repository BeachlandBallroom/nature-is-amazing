package com.natureisamazing.client;

import com.natureisamazing.NatureIsAmazingMod;
import com.natureisamazing.client.renderer.SleepingWolfRenderer;
import net.minecraft.world.entity.EntityType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

/** Registers a vanilla wolf renderer with an additional sleep rendering path. */
@EventBusSubscriber(modid = NatureIsAmazingMod.MOD_ID, value = Dist.CLIENT)
public final class NatureIsAmazingClient {
    private NatureIsAmazingClient() {
    }

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(EntityType.WOLF, SleepingWolfRenderer::new);
    }
}
