package com.github.electroluxv2.bluemapcommunityadvisor.spawnermarker.core;

import net.minecraft.block.SpawnerBlock;
import net.minecraft.block.entity.MobSpawnerBlockEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.*;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import static com.github.electroluxv2.bluemapcommunityadvisor.BlueMapCommunityAdvisor.LOGGER;

public class SpawnerMarkerCreator {
    private static final Executor executor = Executors.newCachedThreadPool();

    public static void onPlayerTick(final ServerPlayerEntity playerEntity, final float tickDelta) {
//        executor.execute(() -> tryToCreateSpawnerMarker(playerEntity, tickDelta));
        tryToCreateSpawnerMarker(playerEntity, tickDelta);
    }

    private static Spawner getSpawnerInfo(World world, BlockPos spawnerPos, PlayerEntity player){
        MobSpawnerBlockEntity spawnerBlockEntity = (MobSpawnerBlockEntity) world.getBlockEntity(spawnerPos);

        NbtCompound spawnerNbtData = new NbtCompound();
        spawnerBlockEntity.getLogic().writeNbt(spawnerNbtData);

        System.out.println("Dupa");

        String mobName = EntityType.get(spawnerNbtData.getList("SpawnPotentials", NbtElement.COMPOUND_TYPE)
                .getCompound(0)
                .getCompound("data")
                .getCompound("entity")
                .getString("id")
        ).orElseThrow().getUntranslatedName();
        String parsedName = mobName.substring(0, 1).toUpperCase() +
                mobName.replace("_", " ").substring(1);

        System.out.println(mobName);

        return new Spawner(
                spawnerPos.getX(),
                spawnerPos.getY(),
                spawnerPos.getZ(),
                player,
                parsedName
        );
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

        if (spawnerBlockPosOptional.isEmpty()) {
            player.sendMessage(Text.of("Empty"), true);
            return;
        }

        final var spawnerBlockPos = spawnerBlockPosOptional.get();

        player.sendMessage(Text.of("Present"), true);

        final var spawnerBlock = (SpawnerBlock) blockView.getBlockState(spawnerBlockPos).getBlock();
        final var mobSpawnerBlockEntity = (MobSpawnerBlockEntity) player.getWorld().getBlockEntity(spawnerBlockPos);

        if (mobSpawnerBlockEntity == null) {
            LOGGER.warn("Failed to read mob spawner block entity");
            return;
        }

//        final ItemStack spawnerStack = 
//
//        blockView.getBlockState(spawnerBlockPos).getBlock().

        NbtCompound spawnerNbtData = new NbtCompound();
        mobSpawnerBlockEntity.getLogic().writeNbt(spawnerNbtData);

        final var itemStack = ItemStack.EMPTY;
        mobSpawnerBlockEntity.readNbt(itemStack.getOrCreateNbt());

        itemStack.setNbt(spawnerNbtData);

        final var s = spawnerNbtData
                .getCompound("SpawnData")
                .getCompound("entity")
                .getString("id");

        LOGGER.info(s);

        final var tooltipText = spawnerBlock.bluemap_community_advisor$getEntityNameForTooltip(itemStack);

        if (tooltipText.isEmpty()) {
            LOGGER.warn("Empty");
            return;
        }

        final var spawnerData = new Spawner(spawnerBlockPos.getX(), spawnerBlockPos.getY(), spawnerBlockPos.getZ(), player, tooltipText.get().toString());
        SpawnerMarkerManager.createSpawnerMarker(spawnerData);
    }
}
