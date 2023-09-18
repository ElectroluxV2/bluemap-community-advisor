package com.github.electroluxv2.bluemapcommunityadvisor;

import com.github.electroluxv2.bluemapcommunityadvisor.creeperholes.CreeperHoles;
import com.github.electroluxv2.bluemapcommunityadvisor.spawnermarker.SpawnerMarker;
import net.fabricmc.api.DedicatedServerModInitializer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class BlueMapCommunityAdvisor implements DedicatedServerModInitializer {
	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
    public static final Logger LOGGER = LoggerFactory.getLogger("BlueMapCommunityAdvisor");
	public static final Executor EXECUTOR = Executors.newCachedThreadPool();

	@Override
	public void onInitializeServer() {
		LOGGER.info("Hello Fabric world!");
		CreeperHoles.initialize();
		SpawnerMarker.initialize();
	}
}