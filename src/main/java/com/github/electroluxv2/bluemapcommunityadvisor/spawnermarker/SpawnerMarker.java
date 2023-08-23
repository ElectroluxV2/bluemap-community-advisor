package com.github.electroluxv2.bluemapcommunityadvisor.spawnermarker;

import com.github.electroluxv2.bluemapcommunityadvisor.spawnermarker.core.SpawnerMarkerManager;

import java.nio.file.Path;
import java.nio.file.Paths;

public class SpawnerMarker {
    public static final Path configDirectory = Paths
            .get("")
            .toAbsolutePath()
            .resolve("config")
            .resolve("bluemap-community-advisor")
            .resolve("spawner-marker");

    public static void initialize() {
        SpawnerMarkerManager.initialize();
    }
}
