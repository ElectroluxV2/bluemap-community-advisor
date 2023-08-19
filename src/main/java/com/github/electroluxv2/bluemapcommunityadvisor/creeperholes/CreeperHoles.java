package com.github.electroluxv2.bluemapcommunityadvisor.creeperholes;

import com.github.electroluxv2.bluemapcommunityadvisor.creeperholes.core.ShameHoleDataManager;
import com.github.electroluxv2.bluemapcommunityadvisor.creeperholes.core.ShameHoleMarkerManager;

import java.nio.file.Path;
import java.nio.file.Paths;

public class CreeperHoles {
    public static final Path configDirectory = Paths
            .get("")
            .toAbsolutePath()
            .resolve("config")
            .resolve("bluemap-community-advisor")
            .resolve("creeper-holes");

    public static void initialize() {
        ShameHoleMarkerManager.initialize();
        ShameHoleDataManager.initialize();
    }
}
