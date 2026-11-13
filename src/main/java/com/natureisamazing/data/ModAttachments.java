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

    /** Persistent identifier of the wild wolf pack this wolf belongs to. */
    public static final Supplier<AttachmentType<String>> WOLF_PACK_ID = ATTACHMENTS.register(
            "wolf_pack_id",
            () -> AttachmentType.builder(() -> "")
                    .serialize(Codec.STRING)
                    .build()
    );

    /** Game tick until which a wolf rests after a successful pack hunt. */
    public static final Supplier<AttachmentType<Long>> WOLF_REST_UNTIL = ATTACHMENTS.register(
            "wolf_rest_until",
            () -> AttachmentType.builder(() -> 0L)
                    .serialize(Codec.LONG)
                    .build()
    );

    /** Number of consecutive game ticks a wolf has spent critically hungry. */
    public static final Supplier<AttachmentType<Integer>> WOLF_STARVATION_TICKS = ATTACHMENTS.register(
            "wolf_starvation_ticks",
            () -> AttachmentType.builder(() -> 0)
                    .serialize(Codec.INT)
                    .build()
    );

    /** Deadline for collecting meat dropped by the wolf's most recent prey. */
    public static final Supplier<AttachmentType<Long>> WOLF_MEAL_UNTIL = ATTACHMENTS.register(
            "wolf_meal_until",
            () -> AttachmentType.builder(() -> 0L)
                    .serialize(Codec.LONG)
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

    public static final Supplier<AttachmentType<Boolean>> SHEEP_GOALS_INSTALLED = ATTACHMENTS.register(
            "sheep_goals_installed",
            () -> AttachmentType.builder(() -> false).build()
    );

    public static final Supplier<AttachmentType<Boolean>> COW_GOALS_INSTALLED = ATTACHMENTS.register(
            "cow_goals_installed",
            () -> AttachmentType.builder(() -> false).build()
    );

    private ModAttachments() {
    }
}
