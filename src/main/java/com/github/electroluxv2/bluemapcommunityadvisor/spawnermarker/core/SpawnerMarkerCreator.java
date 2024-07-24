package com.github.electroluxv2.bluemapcommunityadvisor.spawnermarker.core;

import com.github.electroluxv2.bluemapcommunityadvisor.spawnermarker.mixin.BaseSpawnerAccessor;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.SpawnerBlock;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.minecraft.world.phys.HitResult;
import org.jetbrains.annotations.NotNull;

import java.time.Instant;
import java.util.*;

import static com.github.electroluxv2.bluemapcommunityadvisor.BlueMapCommunityAdvisor.EXECUTOR;
import static com.github.electroluxv2.bluemapcommunityadvisor.BlueMapCommunityAdvisor.LOGGER;

public class SpawnerMarkerCreator {

    public static void onPlayerTick(final ServerPlayer playerEntity, final float tickDelta) {
        EXECUTOR.execute(() -> tryToCreateSpawnerMarker(playerEntity, tickDelta));
    }

    private static void tryToCreateSpawnerMarker(final @NotNull Player player, float tickDelta) {
        final var chunkPos = player.chunkPosition();
        final var blockView = player.level().getChunk(chunkPos.x, chunkPos.z);

        final var result = player.pick(5, tickDelta, false);

        if (result.getType().equals(HitResult.Type.MISS)) {
            return;
        }

        final var rayCastBlockPos = BlockPos.containing(result.getLocation());

        // Check all siblings to the ray-cast hit, as ray-casting is not precise enough
        final var blockPoses = List.of(
            rayCastBlockPos,
            rayCastBlockPos.below(),
            rayCastBlockPos.above(),
            rayCastBlockPos.north(),
            rayCastBlockPos.east(),
            rayCastBlockPos.west(),
            rayCastBlockPos.south()
        );

        final var spawnerBlockPosOptional = blockPoses
                .stream()
                .filter(x -> blockView.getBlockState(x).getBlock() instanceof SpawnerBlock)
                .findFirst();

        if (spawnerBlockPosOptional.isEmpty()) return;

        final var spawnerBlockPos = spawnerBlockPosOptional.get();
        final var mobSpawnerBlockEntity = (SpawnerBlockEntity) blockView.getBlockEntity(spawnerBlockPos);

        if (mobSpawnerBlockEntity == null) {
            LOGGER.warn("Failed to read mob spawner block entity");
            return;
        }

        var spawnData = ((BaseSpawnerAccessor) mobSpawnerBlockEntity.getSpawner()).getNextSpawnData();
        var id = spawnData.getEntityToSpawn().getString("id");
        var description = EntityType.byString(id).orElseThrow().getDescription();

        final var center = spawnerBlockPos.getCenter();

        final var spawnerData = new Spawner(center.x, center.y, center.z, player, description, Instant.now());
        final var isCreated = SpawnerMarkerManager.createSpawnerMarker(spawnerData);

        if (isCreated) {
            player.displayClientMessage(Component.literal("Spawner found").append(CommonComponents.space()).append(description), true);
        }
    }
}
