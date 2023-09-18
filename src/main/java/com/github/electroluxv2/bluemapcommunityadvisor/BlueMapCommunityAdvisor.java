package com.github.electroluxv2.bluemapcommunityadvisor;

import com.github.electroluxv2.bluemapcommunityadvisor.creeperholes.CreeperHoles;
import com.github.electroluxv2.bluemapcommunityadvisor.spawnermarker.SpawnerMarker;
import com.github.electroluxv2.bluemapcommunityadvisor.utils.FileUtils;
import de.bluecolored.bluemap.api.BlueMapAPI;
import net.fabricmc.api.DedicatedServerModInitializer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Objects;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class BlueMapCommunityAdvisor implements DedicatedServerModInitializer {
	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
    public static final Logger LOGGER = LoggerFactory.getLogger("BlueMapCommunityAdvisor");
	public static final Executor EXECUTOR = Executors.newCachedThreadPool();
	public static Path ASSETS;

	@Override
	public void onInitializeServer() {
		LOGGER.info("Hello Fabric world!");
		CreeperHoles.initialize();
		SpawnerMarker.initialize();

		BlueMapAPI.onEnable(api -> {
			ASSETS = api.getWebApp().getWebRoot().resolve("assets/bmca");

			try {
				final var assetsSourceDirectory = Path
						.of(Objects.requireNonNull(BlueMapCommunityAdvisor.class.getResource("/blue-map-community-advisor-assets")).toURI());

				FileUtils.copyDirectory(assetsSourceDirectory, ASSETS, StandardCopyOption.REPLACE_EXISTING);
			} catch (URISyntaxException | IOException e) {
				LOGGER.error("Failed to extract resources", e);
			}
		});
	}
}