package com.natureisamazing.data;

import com.natureisamazing.NatureIsAmazingMod;
import com.mojang.serialization.Codec;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.function.Supplier;

/**
 * Custom data stored on vanilla entities.
 */
public final class ModAttachments {
    public static final DeferredRegister<AttachmentType<?>> ATTACHMENTS =
            DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, NatureIsAmazingMod.MOD_ID);

    /**
     * Hunger of a wild wolf, from 0 (starving) to 100 (full).
     * Codec.INT makes the value persist when the entity is saved.
     */
    public static final Supplier<AttachmentType<Integer>> WOLF_HUNGER = ATTACHMENTS.register(
            "wolf_hunger",
            () -> AttachmentType.builder(() -> 100)
                    .serialize(Codec.INT)
                    .build()
    );

    /**
     * Runtime-only guard to avoid installing the chicken goals more than once
     * on the same entity instance. It is intentionally not serialized because
     * AI goals themselves are recreated when an entity is loaded from disk.
     */
    public static final Supplier<AttachmentType<Boolean>> CHICKEN_GOALS_INSTALLED = ATTACHMENTS.register(
            "chicken_goals_installed",
            () -> AttachmentType.builder(() -> false).build()
    );

    private ModAttachments() {
    }
}
