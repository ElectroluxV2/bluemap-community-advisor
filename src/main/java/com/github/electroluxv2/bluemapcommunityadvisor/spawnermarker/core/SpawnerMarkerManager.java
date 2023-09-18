package com.github.electroluxv2.bluemapcommunityadvisor.spawnermarker.core;

import com.flowpowered.math.vector.Vector2i;
import com.flowpowered.math.vector.Vector3d;
import com.github.electroluxv2.bluemapcommunityadvisor.spawnermarker.SpawnerMarker;
import de.bluecolored.bluemap.api.BlueMapAPI;
import de.bluecolored.bluemap.api.BlueMapMap;
import de.bluecolored.bluemap.api.gson.MarkerGson;
import de.bluecolored.bluemap.api.markers.MarkerSet;
import de.bluecolored.bluemap.api.markers.POIMarker;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

import static com.github.electroluxv2.bluemapcommunityadvisor.BlueMapCommunityAdvisor.LOGGER;
import static com.github.electroluxv2.bluemapcommunityadvisor.spawnermarker.SpawnerMarker.configDirectory;

public class SpawnerMarkerManager {
    private static final String markerLabel = "%s spawner";
    private static final String markerDetail = "%s spawner, found by %s on %s";
    private static final String markerKey = "spawner-marker-%d";
    private static final String markerSetLabel = "Spawners";
    private static final String markerSetKey = "spawners-marker-set-%s";
    private static final String markerSetExtension = ".json5";
    private static final String markerSetKeyPrefix = markerSetKey.substring(0, markerSetKey.lastIndexOf("-") + 1);
    private static final String SPAWNER_ICON = "assets/bmca/spawner.avif";

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

    public static boolean createSpawnerMarker(Spawner spawner) {
        final var api = BlueMapAPI.getInstance().orElseThrow();

        final var blueMapWorld = api
                .getWorld(Objects.requireNonNull(spawner.finder()).getWorld())
                .orElseThrow();

        final var markerSet = blueMapWorld
                .getMaps()
                .stream()
                .flatMap(x -> x.getMarkerSets().entrySet().stream())
                .filter(x -> x.getKey().startsWith(markerSetKeyPrefix))
                .map(Map.Entry::getValue)
                .findFirst()
                .orElse(new MarkerSet(markerSetLabel));

        final String finder = spawner.finder().getName().getString();
        final var markerPosition = new Vector3d(spawner.x(), spawner.y(), spawner.z());
        var label = spawner.type().get(0).getString();
        label = label.isEmpty() ? "Empty" : label;
        DateTimeFormatter dateFormatter = DateTimeFormatter.ISO_DATE.withZone(ZoneId.from(ZoneOffset.UTC));
        String dateOfDiscovery = dateFormatter.format(spawner.timeOfDiscovery());
        dateOfDiscovery = dateOfDiscovery.substring(0, dateOfDiscovery.length()-1);

        final var marker = POIMarker.builder()
                .label(markerLabel.formatted(label))
                .detail(markerDetail.formatted(label, finder, dateOfDiscovery))
                .position(markerPosition)
                .maxDistance(200)
                .icon(SPAWNER_ICON, new Vector2i(32, 32))
                .build();

        final var markerId = markerKey.formatted(markerPosition.hashCode());

        if(markerSet.getMarkers().containsKey(markerId)){
            return false;
        }

        markerSet.getMarkers()
                .put(markerId, marker);

        blueMapWorld
                .getMaps()
                .stream()
                .map(BlueMapMap::getMarkerSets)
                .forEach(set -> set.put(markerSetKey.formatted(blueMapWorld.getId()), markerSet));

        saveMarkerSets(BlueMapAPI.getInstance().orElseThrow());

        return true;
    }

}
