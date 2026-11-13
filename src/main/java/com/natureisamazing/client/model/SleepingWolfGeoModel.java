package com.natureisamazing.client.model;

import com.natureisamazing.NatureIsAmazingMod;
import com.natureisamazing.animatable.SleepingWolfAnimatable;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.DefaultedEntityGeoModel;

public final class SleepingWolfGeoModel extends DefaultedEntityGeoModel<SleepingWolfAnimatable> {
    public SleepingWolfGeoModel() {
        super(ResourceLocation.fromNamespaceAndPath(NatureIsAmazingMod.MOD_ID, "wolf/sleeping_wolf"));
    }

    @Override
    public ResourceLocation getAnimationResource(SleepingWolfAnimatable animatable) {
        return ResourceLocation.fromNamespaceAndPath(
                NatureIsAmazingMod.MOD_ID, "animations/entity/wolf/sleeping_wolf.animation.json");
    }
}
