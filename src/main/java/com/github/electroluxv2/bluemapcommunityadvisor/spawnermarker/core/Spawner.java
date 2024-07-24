package com.github.electroluxv2.bluemapcommunityadvisor.spawnermarker.core;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

import java.time.Instant;

public record Spawner(double x, double y, double z, Player finder, Component type, Instant timeOfDiscovery) {}
