package com.github.electroluxv2.bluemapcommunityadvisor;

import com.github.electroluxv2.bluemapcommunityadvisor.creeperholes.core.ShameHoleMarkerManager;
import de.bluecolored.bluemap.api.BlueMapAPI;
import de.bluecolored.bluemap.api.markers.MarkerSet;
import de.bluecolored.bluemap.api.markers.POIMarker;
import net.fabricmc.api.ModInitializer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BlueMapCommunityAdvisor implements ModInitializer {
	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
    public static final Logger LOGGER = LoggerFactory.getLogger("BlueMapCommunityAdvisor");

	@Override
	public void onInitialize() {
		// This code runs as soon as Minecraft is in a mod-load-ready state.
		// However, some things (like resources) may still be uninitialized.
		// Proceed with mild caution.

		LOGGER.info("Hello Fabric world!");

		ShameHoleMarkerManager.initialize();

		BlueMapAPI.onEnable(api -> {
			final var marker = POIMarker.builder()
					.label("My Marker")
					.detail("Example shame marker")
					.position(0, 0, 0d)
					.maxDistance(1000)
					.build();

			final var markerSet = MarkerSet.builder()
					.label("Test")
					.build();

			markerSet.getMarkers()
					.put("my-marker-id", marker);

			final var blueMapWorld = api
					.getWorlds()
					.stream()
					.flatMap(x -> x.getMaps().stream())
					.filter(x -> x.getName().equalsIgnoreCase("overworld"))
					.findFirst()
					.orElseThrow();

			blueMapWorld
					.getMarkerSets()
					.put("my-marker-set-id", markerSet);
		});
	}
}