package com.github.electroluxv2.bluemapcommunityadvisor.spawnermarker.core;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;

import java.util.List;

public record Spawner(int x, int y, int z, PlayerEntity finder, List<Text> type) {}
