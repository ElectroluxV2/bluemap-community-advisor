package com.github.electroluxv2.bluemapcommunityadvisor.spawnermarker.core;

import net.minecraft.entity.player.PlayerEntity;

public record Spawner(int x, int y, int z, PlayerEntity finder, String type) {}
