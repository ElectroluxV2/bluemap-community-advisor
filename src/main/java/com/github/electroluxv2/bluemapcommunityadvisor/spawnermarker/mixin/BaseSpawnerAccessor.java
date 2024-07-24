package com.github.electroluxv2.bluemapcommunityadvisor.spawnermarker.mixin;

import net.minecraft.world.level.BaseSpawner;
import net.minecraft.world.level.SpawnData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(BaseSpawner.class)
public interface BaseSpawnerAccessor {
    @Accessor
    SpawnData getNextSpawnData();
}
