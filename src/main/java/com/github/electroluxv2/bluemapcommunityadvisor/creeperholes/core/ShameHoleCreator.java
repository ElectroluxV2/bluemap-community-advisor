package com.github.electroluxv2.bluemapcommunityadvisor.creeperholes.core;

import com.github.electroluxv2.bluemapcommunityadvisor.creeperholes.utils.Position;
import net.minecraft.block.SnowBlock;
import net.minecraft.entity.mob.CreeperEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.explosion.Explosion;

import java.util.*;
import java.util.stream.Collectors;

import static com.github.electroluxv2.bluemapcommunityadvisor.BlueMapCommunityAdvisor.LOGGER;
import static com.github.electroluxv2.bluemapcommunityadvisor.creeperholes.core.ShameHoleDataManager.saveHole;
import static com.github.electroluxv2.bluemapcommunityadvisor.creeperholes.core.ShameHoleMarkerManager.createShameMarker;

public class ShameHoleCreator {
    private final static Map<CreeperEntity, List<String>> ignitionCulpritsMap = new HashMap<>();
    private final static Map<Explosion, Set<BlockPos>> affectedBlocksMap = new HashMap<>();
    private final static Map<Explosion, HashSet<BlockPos>> explodedBlocksMap = new HashMap<>();

    /** Collect metadata for explosion */
    public static void onCreeperExploded(final CreeperEntity creeperEntity) {
        final var explosionPosition = creeperEntity.getPos();
        final var target = creeperEntity.getTarget();

        final var culprits = new ArrayList<String>();

        if (target instanceof PlayerEntity targetPlayer) {
            final var nickname = targetPlayer.getGameProfile().getName();
            culprits.add(nickname);
        }

        // When player was not target, scan for player around
        if (culprits.isEmpty()) {
            final var playersNearbyExplosion = creeperEntity.getWorld()
                    .getPlayers()
                    .stream()
                    .filter(x -> x.getPos().distanceTo(explosionPosition) < creeperEntity.bluemap_community_advisor$getEplosionRadius().orElseThrow() * 5)
                    .map(x -> x.getGameProfile().getName())
                    .toList();

            culprits.addAll(playersNearbyExplosion);
        }

        ignitionCulpritsMap.put(creeperEntity, culprits);
    }

    /** Explosion removes block from existence, so we need to save all possibly affected blocks before this happens */
    public static void afterExplosionCollectBlocks(final World world, final Explosion explosion) {
        final var nonAirExplodedBlocks = explosion
                .getAffectedBlocks()
                .stream()
                .filter(pos -> !world.getBlockState(pos).isAir())
                .filter(pos -> !(world.getBlockState(pos).getBlock() instanceof SnowBlock))
                .filter(pos -> !world.getBlockState(pos).isReplaceable())
                .filter(pos -> world.getBlockState(pos).getBlock().getBlastResistance() >= 0.5)
                .collect(Collectors.toUnmodifiableSet());

        affectedBlocksMap.put(explosion, nonAirExplodedBlocks);
    }

    /** Save positions of exploded blocks, so we may filter affected ones from destroyed */
    public static void onBlockDestroyedByExplosion(final Explosion explosion, final BlockPos pos) {
        explodedBlocksMap.computeIfAbsent(explosion, x -> new HashSet<>());
        explodedBlocksMap.get(explosion).add(pos);
    }

    /** Once explosion has affected world, we should have all required metadata, to save shame tag
     * {@link #affectedBlocksMap} contains blocks that may not have been destroyed,
     * so we need to intersect it with {@link #explodedBlocksMap} to get list of blocks actually destroyed by explosion */
    public static void afterExplosionAffectWorld(final Explosion explosion) {
        final var affectedBlocks = affectedBlocksMap.get(explosion);
        affectedBlocksMap.remove(explosion);

        final var explodedBlocks = explodedBlocksMap.getOrDefault(explosion, new HashSet<>());
        explodedBlocksMap.remove(explosion);

        final var creeperEntity = (CreeperEntity) explosion.getEntity();
        final var ignitionCulprits = ignitionCulpritsMap.get(creeperEntity);
        ignitionCulpritsMap.remove(creeperEntity);

        if (affectedBlocks == null || explodedBlocks == null) {
            LOGGER.warn("Failed to synchronize events");
            return;
        }

        if (ignitionCulprits == null || ignitionCulprits.isEmpty()) {
            LOGGER.warn("Missing culprit metadata");
            return;
        }

        final var affectedBlocksPositions = new HashSet<>(affectedBlocks);

        // Make intersect to leave only exploded blocks
        affectedBlocksPositions.retainAll(explodedBlocks);

        final var blocksDestroyedByCreeper = affectedBlocks
                .stream()
                .filter(affectedBlocksPositions::contains)
                .toList();

        if (blocksDestroyedByCreeper.isEmpty()) {
            return;
        }

        final Runnable processing = () -> {
            final var markerId = createShameMarker(ignitionCulprits, blocksDestroyedByCreeper, explosion);
            final var list = blocksDestroyedByCreeper
                    .stream()
                    .map(Position::new)
                    .collect(Collectors.toSet());

            saveHole(markerId, new ShameHole(list.size(), list));
        };

        final var thread = new Thread(processing);
        thread.start();
    }
}
