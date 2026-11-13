package com.natureisamazing.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.natureisamazing.NatureIsAmazingMod;
import com.natureisamazing.api.WolfSleepState;
import com.natureisamazing.animatable.SleepingWolfAnimatable;
import com.natureisamazing.client.model.SleepingWolfGeoModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.WolfRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.animal.Wolf;
import software.bernie.geckolib.renderer.GeoReplacedEntityRenderer;

/**
 * Keeps the vanilla wolf renderer for every normal state and delegates only
 * the sleeping state to the custom GeckoLib pose.
 */
public final class SleepingWolfRenderer extends WolfRenderer {
    private final SleepPoseRenderer sleepPoseRenderer;

    public SleepingWolfRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.sleepPoseRenderer = new SleepPoseRenderer(context);
    }

    @Override
    public void render(Wolf wolf, float entityYaw, float partialTick, PoseStack poseStack,
                       MultiBufferSource bufferSource, int packedLight) {
        if (((WolfSleepState) wolf).natureIsAmazing$isSleeping()) {
            this.sleepPoseRenderer.render(wolf, entityYaw, partialTick, poseStack, bufferSource, packedLight);
            return;
        }

        super.render(wolf, entityYaw, partialTick, poseStack, bufferSource, packedLight);
    }

    private static final class SleepPoseRenderer extends GeoReplacedEntityRenderer<Wolf, SleepingWolfAnimatable> {
        private SleepPoseRenderer(EntityRendererProvider.Context context) {
            super(context, new SleepingWolfGeoModel(), NatureIsAmazingMod.SLEEPING_WOLF);
        }

        /**
         * The sleeping model has the same UV layout as a vanilla wolf, so use
         * the exact texture selected by vanilla for this wolf instead of the
         * single pale-wolf texture bundled with the animation assets.
         */
        @Override
        public ResourceLocation getTextureLocation(SleepingWolfAnimatable animatable) {
            return getCurrentEntity().getTexture();
        }
    }
}
