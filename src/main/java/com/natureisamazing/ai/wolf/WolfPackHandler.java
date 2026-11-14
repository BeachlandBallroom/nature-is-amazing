package com.natureisamazing.ai.wolf;

import com.natureisamazing.data.ModAttachments;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Server-side coordination for wild-wolf packs. It supplements vanilla goals:
 * vanilla still handles pathfinding and attacking once a target is assigned.
 */
public final class WolfPackHandler {
    private static final double PACK_JOIN_RADIUS = 24.0D;
    private static final double HUNT_SHARE_RADIUS = 28.0D;
    private static final double MAX_PURSUIT_DISTANCE = 48.0D;
    static final double TERRITORY_RADIUS = 32.0D;
    private static final double DEFENCE_RADIUS = 16.0D;
    private static final long REST_AFTER_HUNT_TICKS = 600L;

    private final Map<String, BlockPos> territories = new HashMap<>();

    @SubscribeEvent
    public void onWolfJoin(EntityJoinLevelEvent event) {
        if (!(event.getEntity() instanceof Wolf wolf) || wolf.level().isClientSide || wolf.isTame()) {
            return;
        }

        String packId = getOrCreatePackId(wolf);
        getOrCreateTerritoryCenter(wolf, packId);
    }

    @SubscribeEvent
    public void onWolfTick(EntityTickEvent.Post event) {
        if (!(event.getEntity() instanceof Wolf wolf) || wolf.level().isClientSide || wolf.isTame()) {
            return;
        }

        // Pack decisions need not run every tick; vanilla movement and attack
        // goals continue to update at their normal rate.
        if (wolf.tickCount % 20 != 0) {
            return;
        }

        String packId = getOrCreatePackId(wolf);
        getOrCreateTerritoryCenter(wolf, packId);

        if (wolf.hurtTime > 0) {
            wolf.setData(ModAttachments.WOLF_REST_UNTIL, 0L);
        }

        if (isResting(wolf)) {
            wolf.setTarget(null);
            return;
        }

        LivingEntity target = wolf.getTarget();
        if (target != null && (!target.isAlive() || wolf.distanceToSqr(target) > MAX_PURSUIT_DISTANCE * MAX_PURSUIT_DISTANCE)) {
            wolf.setTarget(null);
            target = null;
        }

        if (target != null && Wolf.PREY_SELECTOR.test(target)) {
            sharePreyTarget(wolf, packId, target);
            return;
        }

        defendTerritory(wolf, packId);
    }

    /** Starts a shared resting period after a pack member kills natural prey. */
    public void beginPostHuntRest(Wolf hunter) {
        if (hunter.level().isClientSide || hunter.isTame()) {
            return;
        }

        String packId = getOrCreatePackId(hunter);
        long restUntil = hunter.level().getGameTime() + REST_AFTER_HUNT_TICKS;
        for (Wolf packmate : nearbyWolves(hunter.level(), hunter, HUNT_SHARE_RADIUS)) {
            if (packId.equals(getOrCreatePackId(packmate))) {
                packmate.setTarget(null);
                packmate.setData(ModAttachments.WOLF_REST_UNTIL, restUntil);
            }
        }
    }

    /** True when a nearby member of this wolf's pack is already pursuing prey. */
    public boolean isPackHunt(Wolf wolf, LivingEntity prey) {
        if (!Wolf.PREY_SELECTOR.test(prey) || wolf.isTame()) {
            return false;
        }

        String packId = getOrCreatePackId(wolf);
        return nearbyWolves(wolf.level(), wolf, HUNT_SHARE_RADIUS).stream()
                .anyMatch(packmate -> packmate != wolf
                        && packId.equals(getOrCreatePackId(packmate))
                        && packmate.getTarget() == prey);
    }

    private void sharePreyTarget(Wolf leader, String packId, LivingEntity prey) {
        for (Wolf packmate : nearbyWolves(leader.level(), leader, HUNT_SHARE_RADIUS)) {
            if (packmate != leader
                    && packId.equals(getOrCreatePackId(packmate))
                    && !isResting(packmate)
                    && packmate.getTarget() == null) {
                packmate.setTarget(prey);
            }
        }
    }

    /**
     * Returns the stable home position used by this wild wolf's pack patrol.
     * The center is stored on the wolf as a packed BlockPos so it survives saves.
     */
    public BlockPos getTerritoryCenter(Wolf wolf) {
        if (wolf.level().isClientSide || wolf.isTame()) {
            return null;
        }

        String packId = getOrCreatePackId(wolf);
        return getOrCreateTerritoryCenter(wolf, packId);
    }

    private BlockPos getOrCreateTerritoryCenter(Wolf wolf, String packId) {
        String key = territoryKey(wolf, packId);
        long packedCenter = wolf.getData(ModAttachments.WOLF_TERRITORY_CENTER);

        // A saved center is authoritative. This also restores the runtime map
        // when the first member of a pack is loaded after a server restart.
        if (packedCenter != Long.MIN_VALUE) {
            BlockPos savedCenter = BlockPos.of(packedCenter).immutable();
            this.territories.put(key, savedCenter);
            return savedCenter;
        }

        BlockPos center = this.territories.computeIfAbsent(
                key, ignored -> wolf.blockPosition().immutable()
        );
        wolf.setData(ModAttachments.WOLF_TERRITORY_CENTER, center.asLong());
        return center;
    }

    private String territoryKey(Wolf wolf, String packId) {
        // Pack ids are saved on wolves; including the dimension prevents
        // unrelated packs in different dimensions from sharing one center.
        return wolf.level().dimension().location() + "|" + packId;
    }

    private void defendTerritory(Wolf wolf, String packId) {
        BlockPos territoryCenter = getOrCreateTerritoryCenter(wolf, packId);
        if (territoryCenter == null
                || wolf.distanceToSqr(territoryCenter.getX() + 0.5D, territoryCenter.getY(), territoryCenter.getZ() + 0.5D)
                > TERRITORY_RADIUS * TERRITORY_RADIUS) {
            return;
        }

        Wolf rival = nearbyWolves(wolf.level(), wolf, DEFENCE_RADIUS).stream()
                .filter(other -> !packId.equals(getOrCreatePackId(other)))
                .min((first, second) -> Double.compare(wolf.distanceToSqr(first), wolf.distanceToSqr(second)))
                .orElse(null);

        if (rival != null) {
            wolf.setTarget(rival);
        }
    }

    private String getOrCreatePackId(Wolf wolf) {
        String existingId = wolf.getData(ModAttachments.WOLF_PACK_ID);
        if (!existingId.isEmpty()) {
            return existingId;
        }

        String nearbyPackId = nearbyWolves(wolf.level(), wolf, PACK_JOIN_RADIUS).stream()
                .filter(other -> other != wolf)
                .map(other -> other.getData(ModAttachments.WOLF_PACK_ID))
                .filter(id -> !id.isEmpty())
                .findFirst()
                .orElse(UUID.randomUUID().toString());
        wolf.setData(ModAttachments.WOLF_PACK_ID, nearbyPackId);
        return nearbyPackId;
    }

    private boolean isResting(Wolf wolf) {
        return wolf.level().getGameTime() < wolf.getData(ModAttachments.WOLF_REST_UNTIL);
    }

    private List<Wolf> nearbyWolves(Level level, Wolf wolf, double radius) {
        return level.getEntitiesOfClass(
                Wolf.class,
                new AABB(wolf.blockPosition()).inflate(radius),
                other -> !other.isTame() && other.isAlive()
        );
    }
}
