package com.github.electroluxv2.bluemapcommunityadvisor.spawnermarker.core;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;

import java.time.Instant;
import java.util.List;

public record Spawner(double x, double y, double z, PlayerEntity finder, List<Text> type, Instant timeOfDiscovery) {}
