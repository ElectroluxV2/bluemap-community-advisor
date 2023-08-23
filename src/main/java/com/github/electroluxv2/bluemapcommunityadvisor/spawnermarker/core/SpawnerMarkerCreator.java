package com.github.electroluxv2.bluemapcommunityadvisor.spawnermarker.core;

import net.minecraft.block.BlockState;
import net.minecraft.block.SpawnerBlock;
import net.minecraft.block.entity.MobSpawnerBlockEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class SpawnerMarkerCreator {
    public static Spawner getSpawnerInfo(World world, BlockPos spawnerPos, PlayerEntity player){
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
    public static void tryToCreateSpawnerMarker(PlayerEntity player){
        HitResult hit = player.raycast(10d, 0F, false);
        if(!hit.getType().equals(HitResult.Type.BLOCK)) return;

        Vec3d vec3d = hit.getPos();
        World world = player.getWorld();
        BlockPos raycastBlockPos = BlockPos.ofFloored(vec3d.getX(), vec3d.getY(), vec3d.getZ());
        BlockState raycastBlockState = world.getBlockState(raycastBlockPos);

        System.out.println(raycastBlockState.getBlock().toString());

        if(!(raycastBlockState.getBlock() instanceof SpawnerBlock)) return;
        Spawner spawnerData = getSpawnerInfo(world, raycastBlockPos, player);

        SpawnerMarkerManager.createSpawnerMarker(spawnerData);
    }
}
