package com.github.electroluxv2.bluemapcommunityadvisor.spawnermarker.core;

import com.flowpowered.math.vector.Vector2i;
import com.flowpowered.math.vector.Vector3d;
import com.github.electroluxv2.bluemapcommunityadvisor.spawnermarker.SpawnerMarker;
import de.bluecolored.bluemap.api.BlueMapAPI;
import de.bluecolored.bluemap.api.BlueMapMap;
import de.bluecolored.bluemap.api.gson.MarkerGson;
import de.bluecolored.bluemap.api.markers.MarkerSet;
import de.bluecolored.bluemap.api.markers.POIMarker;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.explosion.Explosion;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

import static com.github.electroluxv2.bluemapcommunityadvisor.BlueMapCommunityAdvisor.LOGGER;
import static com.github.electroluxv2.bluemapcommunityadvisor.spawnermarker.SpawnerMarker.configDirectory;

public class SpawnerMarkerManager {
    private static final String markerLabel = "%s spawner";
    private static final String markerDetail = "%s spawner, found by %s";
    private static final String markerKey = "spawner-marker-%d-%d";
    private static final String markerSetLabel = "Spawners";
    private static final String markerSetKey = "spawners-marker-set-%s";
    private static final String markerSetExtension = ".json5";
    private static final String markerSetKeyPrefix = markerSetKey.substring(0, markerSetKey.lastIndexOf("-") + 1);
    public static void initialize() {
        try {
            Files.createDirectories(configDirectory);
        } catch (IOException e) {
            LOGGER.error("Failed to initialize config", e);
        }

        BlueMapAPI.onEnable(SpawnerMarkerManager::loadMarkerSets);
        BlueMapAPI.onDisable(SpawnerMarkerManager::saveMarkerSets);
    }

    private static void loadMarkerSets(final BlueMapAPI api) {
        final var markerSetsFiles = Stream
                .of(Objects.requireNonNull(SpawnerMarker.configDirectory.toFile().listFiles()))
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

                try (final var writer = new FileWriter(SpawnerMarker.configDirectory.resolve(markerSetKey.formatted(worldId) + markerSetExtension).toFile())) {
                    MarkerGson.INSTANCE.toJson(markerSet, writer);
                    LOGGER.info("Saved market set for world: %s".formatted(worldId));
                } catch (IOException ex) {
                    LOGGER.error("Failed to save marker set", ex);
                }
            }
        }
    }

    public static String createSpawnerMarker(Spawner spawner) {
        final var api = BlueMapAPI.getInstance().orElseThrow();

        final var blueMapWorld = api
                .getWorld(Objects.requireNonNull(spawner.finder()).getWorld())
                .orElseThrow();

        final String finder = spawner.finder().getName().getString();
        final var markerPosition = new Vector3d(spawner.x(), spawner.y(), spawner.z());

        final var marker = POIMarker.builder()
                .label(markerLabel.formatted(spawner.type()))
                .detail(markerDetail.formatted(spawner.type(), finder))
                .position(markerPosition)
                .maxDistance(200)
                .icon("assets/spawner.webp", new Vector2i(0, 0))
                .build();

        final var markerId = markerKey.formatted(finder.hashCode(), markerPosition.hashCode());

        final var markerSet = blueMapWorld
                .getMaps()
                .stream()
                .flatMap(x -> x.getMarkerSets().entrySet().stream())
                .filter(x -> x.getKey().startsWith(markerSetKeyPrefix))
                .map(Map.Entry::getValue)
                .findFirst()
                .orElse(new MarkerSet(markerSetLabel));

        markerSet.getMarkers()
                .put(markerId, marker);

        blueMapWorld
                .getMaps()
                .stream()
                .map(BlueMapMap::getMarkerSets)
                .forEach(set -> set.put(markerSetKey.formatted(blueMapWorld.getId()), markerSet));

        saveMarkerSets(BlueMapAPI.getInstance().orElseThrow());

        return markerId;
    }

}
