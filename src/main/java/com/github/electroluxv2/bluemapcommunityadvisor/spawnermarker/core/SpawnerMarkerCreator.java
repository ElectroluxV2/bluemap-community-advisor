package com.github.electroluxv2.bluemapcommunityadvisor.spawnermarker.core;

import net.minecraft.block.SpawnerBlock;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.entity.MobSpawnerBlockEntity;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import static com.github.electroluxv2.bluemapcommunityadvisor.BlueMapCommunityAdvisor.LOGGER;

public class SpawnerMarkerCreator {
    private static final Executor executor = Executors.newCachedThreadPool();

    public static void onPlayerTick(final ServerPlayerEntity playerEntity, final float tickDelta) {
        executor.execute(() -> tryToCreateSpawnerMarker(playerEntity, tickDelta));
    }

    private static void tryToCreateSpawnerMarker(final PlayerEntity player, float tickDelta) {
        final var chunkPos = player.getChunkPos();
        final var blockView = player.getWorld().getChunkAsView(chunkPos.x, chunkPos.z);

        if (blockView == null) {
            LOGGER.warn("Failed to get BlockView for %s".formatted(chunkPos.toString()));
            return;
        }

        final var result = player.raycast(5, tickDelta, false);

        if (result.getType().equals(HitResult.Type.MISS)) {
            return;
        }

        final var rayCastBlockPos = BlockPos.ofFloored(result.getPos());

        final var blockPoses = List.of(
            rayCastBlockPos,
            rayCastBlockPos.down(),
            rayCastBlockPos.up(),
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

        final var spawnerBlock = (SpawnerBlock) blockView.getBlockState(spawnerBlockPos).getBlock();

        final var mobSpawnerBlockEntity = (MobSpawnerBlockEntity) blockView.getBlockEntity(spawnerBlockPos);


        if (mobSpawnerBlockEntity == null) {
            LOGGER.warn("Failed to read mob spawner block entity");
            return;
        }

        NbtCompound spawnerNbtData = new NbtCompound();
        mobSpawnerBlockEntity.getLogic().writeNbt(spawnerNbtData);

        final var spawnerItemStack = new ItemStack(spawnerBlock);

        BlockItem.setBlockEntityNbt(spawnerItemStack, BlockEntityType.MOB_SPAWNER, spawnerNbtData);

        final var tooltipText = new ArrayList<Text>();
        spawnerBlock.appendTooltip(spawnerItemStack, player.getWorld(), tooltipText, TooltipContext.ADVANCED);

        final var spawnerData = new Spawner(spawnerBlockPos.getX(), spawnerBlockPos.getY(), spawnerBlockPos.getZ(), player, tooltipText, Instant.now());
        final var isCreated = SpawnerMarkerManager.createSpawnerMarker(spawnerData);

        if(isCreated) {
            player.sendMessage(Text.of("Spawner found"), true);
        }
    }
}
