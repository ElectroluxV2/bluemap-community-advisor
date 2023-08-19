package com.github.electroluxv2.bluemapcommunityadvisor.core;

import de.bluecolored.bluemap.api.BlueMapAPI;
import de.bluecolored.bluemap.api.BlueMapMap;
import de.bluecolored.bluemap.api.gson.MarkerGson;
import de.bluecolored.bluemap.api.markers.MarkerSet;
import de.bluecolored.bluemap.api.markers.POIMarker;
import net.minecraft.world.explosion.Explosion;
import net.minecraft.world.gen.chunk.Blender;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

import static com.github.electroluxv2.bluemapcommunityadvisor.BlueMapCommunityAdvisor.LOGGER;

public class ShameHoleMarkerManager {
    private static final String markerLabel = "Creeper hole by %s";
    private static final String markerDetail = "Creeper hole by %s, convinced at %s, destroying %d blocks total.";
    private static final String markerKey = "creeper-hole-%d-%d";
    private static final String markerSetLabel = "Shame holes";
    private static final String markerSetKey = "creeper-holes-marker-set-%s";
    private static final String markerSetExtension = ".json5";
    private static final String markerSetKeyPrefix = markerSetKey.substring(0, markerSetKey.lastIndexOf("-") + 1);

    private static final Path configDirectory = Paths
            .get("")
            .toAbsolutePath()
            .resolve("config")
            .resolve("bluemap-community-advisor")
            .resolve("creeper-holes");

    public static void initialize() {
        try {
            Files.createDirectories(configDirectory);
        } catch (IOException e) {
            LOGGER.error("Failed to initialize config", e);
        }

        BlueMapAPI.onEnable(ShameHoleMarkerManager::loadMarkerSets);
        BlueMapAPI.onDisable(ShameHoleMarkerManager::saveMarkerSets);
    }

    private static void loadMarkerSets(final BlueMapAPI api) {
        final var markerSetsFiles = Stream
                .of(Objects.requireNonNull(configDirectory.toFile().listFiles()))
                .filter(x -> !x.isDirectory())
                .filter(x -> x.toPath().getFileName().toString().startsWith(markerSetKeyPrefix))
                .toList();

        for (final var markerSetFile : markerSetsFiles) {
            final var filename = markerSetFile.toPath().getFileName().toString();
            final var worldId = filename.substring(markerSetKeyPrefix.length(), filename.lastIndexOf(markerSetExtension));

            try (final var reader = new FileReader(markerSetFile)) {
                final var markerSet = MarkerGson.INSTANCE.fromJson(reader, MarkerSet.class);

                for (final var map : api.getWorld(worldId).orElseThrow().getMaps()) {
                    map.getMarkerSets().put(markerSetKey.formatted(worldId), markerSet);
                }

                LOGGER.info("Loaded marker set for world: %s".formatted(worldId));
            } catch (IOException ex) {
                LOGGER.error("Failed to load marker set for world: %s".formatted(worldId));
            }
        }
    }

    private static void saveMarkerSets(final BlueMapAPI api) {
        for (final var world : api.getWorlds()) {
            final var worldId = world.getId();

            for (final var map : world.getMaps()) {
                final var key = markerSetKey.formatted(worldId);

                if (!map.getMarkerSets().containsKey(key)) {
                    continue; // Marker set may not exist for some world/map yet
                }

                final var markerSet = map.getMarkerSets().get(key);

                try (final var writer = new FileWriter(configDirectory.resolve(markerSetKey.formatted(worldId) + markerSetExtension).toFile())) {
                    MarkerGson.INSTANCE.toJson(markerSet, writer);
                    LOGGER.info("Saved market set for world: %s".formatted(worldId));
                } catch (IOException ex) {
                    LOGGER.error("Failed to save marker set", ex);
                }
            }
        }
    }

    public static void createShameTag(final List<String> ignitionCulprits, final List<CreeperExplodedBlock> blocksDestroyedByCreeper, final Explosion explosion) {
        final var api = BlueMapAPI.getInstance().orElseThrow();

        final var blueMapWorld = api
                .getWorld(Objects.requireNonNull(explosion.getEntity()).getWorld())
                .orElseThrow();

        final var authors = String.join(", ", ignitionCulprits);
        final var markerPosition = explosion.bluemap_community_advisor$getVector3d().orElseThrow();

        final var marker = POIMarker.builder()
                .label(markerLabel.formatted(authors))
                .detail(markerDetail.formatted(authors, Instant.now().toString(), blocksDestroyedByCreeper.size()))
                .position(markerPosition)
                .maxDistance(200)
                .build();

        final var markerSet = blueMapWorld
                .getMaps()
                .stream()
                .flatMap(x -> x.getMarkerSets().entrySet().stream())
                .filter(x -> x.getKey().startsWith(markerSetKeyPrefix))
                .map(Map.Entry::getValue)
                .findFirst()
                .orElse(new MarkerSet(markerSetLabel));

        markerSet.getMarkers()
                .put(markerKey.formatted(authors.hashCode(), markerPosition.hashCode()), marker);

        blueMapWorld
                .getMaps()
                .stream()
                .map(BlueMapMap::getMarkerSets)
                .forEach(set -> set.put(markerSetKey.formatted(blueMapWorld.getId()), markerSet));
    }
}
