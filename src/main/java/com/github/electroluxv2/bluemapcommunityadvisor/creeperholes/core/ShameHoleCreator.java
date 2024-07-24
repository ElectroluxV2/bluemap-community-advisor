package com.github.electroluxv2.bluemapcommunityadvisor.creeperholes.core;

import com.github.electroluxv2.bluemapcommunityadvisor.creeperholes.utils.Position;
import com.github.electroluxv2.bluemapcommunityadvisor.spawnermarker.mixin.CreeperAccessor;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.SnowLayerBlock;

import java.util.*;
import java.util.stream.Collectors;

import static com.github.electroluxv2.bluemapcommunityadvisor.BlueMapCommunityAdvisor.LOGGER;
import static com.github.electroluxv2.bluemapcommunityadvisor.creeperholes.core.ShameHoleDataManager.saveHole;
import static com.github.electroluxv2.bluemapcommunityadvisor.creeperholes.core.ShameHoleMarkerManager.createShameMarker;

public class ShameHoleCreator {
    private final static Map<Creeper, List<String>> ignitionCulpritsMap = new HashMap<>();
    private final static Map<Explosion, Set<BlockPos>> affectedBlocksMap = new HashMap<>();
    private final static Map<Explosion, HashSet<BlockPos>> explodedBlocksMap = new HashMap<>();

    /** Collect metadata for explosion */
    public static void onCreeperExploded(final Creeper creeperEntity) {
        final var explosionPosition = creeperEntity.position();
        final var target = creeperEntity.getTarget();

        final var culprits = new ArrayList<String>();

        if (target instanceof Player targetPlayer) {
            final var nickname = targetPlayer.getGameProfile().getName();
            culprits.add(nickname);
        }

        // When player was not target, scan for player around
        if (culprits.isEmpty()) {
            final var playersNearbyExplosion = creeperEntity.level()
                    .players()
                    .stream()
                    .filter(x -> x.position().distanceTo(explosionPosition) < ((CreeperAccessor) creeperEntity).getExplosionRadius() * 5)
                    .map(x -> x.getGameProfile().getName())
                    .toList();

            culprits.addAll(playersNearbyExplosion);
        }

        ignitionCulpritsMap.put(creeperEntity, culprits);
    }

    /** Explosion removes block from existence, so we need to save all possibly affected blocks before this happens */
    public static void afterExplosionCollectBlocks(final Level level, final Explosion explosion) {
        final var nonAirExplodedBlocks = explosion
                .getToBlow()
                .stream()
                .filter(pos -> !level.getBlockState(pos).isAir())
                .filter(pos -> !(level.getBlockState(pos).getBlock() instanceof SnowLayerBlock))
                .filter(pos -> !level.getBlockState(pos).canBeReplaced())
                .filter(pos -> level.getBlockState(pos).getBlock().getExplosionResistance() >= 0.5)
                .collect(Collectors.toUnmodifiableSet());

        affectedBlocksMap.put(explosion, nonAirExplodedBlocks);
    }

    /** Save positions of exploded blocks, so we may filter affected ones from destroyed */
    public static void onBlockDestroyedByExplosion(final Explosion explosion, final BlockPos pos) {
        explodedBlocksMap.computeIfAbsent(explosion, x -> new HashSet<>());
        explodedBlocksMap.get(explosion).add(pos);
    }

    /** Once explosion has affected level, we should have all required metadata, to save shame tag
     * {@link #affectedBlocksMap} contains blocks that may not have been destroyed,
     * so we need to intersect it with {@link #explodedBlocksMap} to get list of blocks actually destroyed by explosion */
    public static void afterExplosionAffectWorld(final Explosion explosion) {
        final var affectedBlocks = affectedBlocksMap.get(explosion);
        affectedBlocksMap.remove(explosion);

        final var explodedBlocks = explodedBlocksMap.getOrDefault(explosion, new HashSet<>());
        explodedBlocksMap.remove(explosion);

        if (!(explosion.getDirectSourceEntity() instanceof final Creeper creeperEntity)) return;

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
