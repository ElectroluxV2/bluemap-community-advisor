package com.github.electroluxv2.bluemapcommunityadvisor.creeperholes.core;

import com.flowpowered.math.vector.Vector3d;
import com.github.electroluxv2.bluemapcommunityadvisor.spawnermarker.mixin.ExplosionAccessor;
import de.bluecolored.bluemap.api.BlueMapAPI;
import de.bluecolored.bluemap.api.BlueMapMap;
import de.bluecolored.bluemap.api.gson.MarkerGson;
import de.bluecolored.bluemap.api.markers.MarkerSet;
import de.bluecolored.bluemap.api.markers.POIMarker;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Explosion;

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
import static com.github.electroluxv2.bluemapcommunityadvisor.creeperholes.CreeperHoles.configDirectory;

public class ShameHoleMarkerManager {
    private static final String markerLabel = "Creeper hole by %s";
    private static final String markerDetail = "Creeper hole by %s, convinced at %s, destroying %d blocks total.";
    private static final String markerKey = "creeper-hole-%d-%d";
    private static final String markerSetLabel = "Shame holes";
    private static final String markerSetKey = "creeper-holes-marker-set-%s";
    private static final String markerSetExtension = ".json5";
    private static final String markerSetKeyPrefix = markerSetKey.substring(0, markerSetKey.lastIndexOf("-") + 1);

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

    public static String createShameMarker(final List<String> ignitionCulprits, final List<BlockPos> blocksDestroyedByCreeper, final Explosion explosion) {
        final var api = BlueMapAPI.getInstance().orElseThrow();

        final var blueMapWorld = api
                .getWorld(Objects.requireNonNull(explosion.getDirectSourceEntity()).level())
                .orElseThrow();

        final var authors = String.join(", ", ignitionCulprits);

        final var ex = (ExplosionAccessor) explosion;
        final var markerPosition = new Vector3d(ex.getX(), ex.getY(), ex.getZ());

        final var marker = POIMarker.builder()
                .label(markerLabel.formatted(authors))
                .detail(markerDetail.formatted(authors, Instant.now().toString(), blocksDestroyedByCreeper.size()))
                .position(markerPosition)
                .maxDistance(200)
                .build();

        final var markerId = markerKey.formatted(authors.hashCode(), markerPosition.hashCode());

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

    public static void removeShameMarker(String markerId) {
        final var api = BlueMapAPI.getInstance().orElseThrow();

        for (final var world : api.getWorlds()) {
            for (final var map : world.getMaps()) {
                final var markerSet = map.getMarkerSets().get(markerSetKey.formatted(world.getId()));

                if (markerSet == null) continue;

                markerSet.remove(markerId);
            }
        }

        saveMarkerSets(BlueMapAPI.getInstance().orElseThrow());
    }
}
