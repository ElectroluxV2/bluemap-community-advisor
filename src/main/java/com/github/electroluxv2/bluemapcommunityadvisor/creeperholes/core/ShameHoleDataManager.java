package com.github.electroluxv2.bluemapcommunityadvisor.creeperholes.core;

import com.github.electroluxv2.bluemapcommunityadvisor.creeperholes.utils.DebouncedRunnable;
import com.github.electroluxv2.bluemapcommunityadvisor.creeperholes.utils.Position;
import de.bluecolored.bluemap.api.gson.MarkerGson;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.github.electroluxv2.bluemapcommunityadvisor.BlueMapCommunityAdvisor.LOGGER;
import static com.github.electroluxv2.bluemapcommunityadvisor.creeperholes.CreeperHoles.configDirectory;

public class ShameHoleDataManager {
    private static final ConcurrentHashMap<Position, HashSet<String>> positionToMarkerIds = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<String, ShameHole> markerIdsToShameHoles = new ConcurrentHashMap<>();

    private static final HashMap<String, DebouncedRunnable> pendingSaves = new HashMap<>();

    public static void initialize() {
        final var holeFiles = Stream
                .of(Objects.requireNonNull(configDirectory.toFile().listFiles()))
                .filter(x -> !x.isDirectory())
                .filter(x -> x.toPath().getFileName().toString().startsWith("hole-"))
                .toList();

        for (final var holeFile : holeFiles) {
            final var filename = holeFile.toPath().getFileName().toString();
            final var markerId = filename.substring("hole-".length(), filename.lastIndexOf(".json5"));

            try (final var reader = new FileReader(holeFile)) {
                final ShameHole shameHole = MarkerGson.INSTANCE.fromJson(reader, ShameHole.class);

                markerIdsToShameHoles.put(markerId, shameHole);
                for (final var position : shameHole.positions()) {
                    positionToMarkerIds.computeIfAbsent(position, x -> new HashSet<>());
                    positionToMarkerIds.get(position).add(markerId);
                }

                LOGGER.info("Loaded hole for marker: %s".formatted(markerId));
            } catch (IOException ex) {
                LOGGER.error("Failed to load hole for marker: %s".formatted(markerId));
            }
        }
    }

    public static void saveHole(final String markerId, final ShameHole shameHole) {
        final var file = configDirectory
                .resolve("hole-%s.json5".formatted(markerId))
                .toFile();

        markerIdsToShameHoles.put(markerId, shameHole);
        for (final var position : shameHole.positions()) {
            positionToMarkerIds.computeIfAbsent(position, x -> new HashSet<>());
            positionToMarkerIds.get(position).add(markerId);
        }

        pendingSaves.computeIfAbsent(markerId, x -> new DebouncedRunnable(() -> {
            try (final var writer = new FileWriter(file)) {
                MarkerGson.INSTANCE.toJson(shameHole, writer);
                LOGGER.info("Saved hole for marker: %s".formatted(markerId));
            } catch (IOException ex) {
                LOGGER.error("Failed to save hole for marker: %s".formatted(markerId), ex);
            }
        }, "Saving %s".formatted(markerId), 5000));

        pendingSaves.get(markerId).run();
    }

    public static Optional<Set<String>> getMarkerIdsForPosition(final Position position) {
        return positionToMarkerIds.containsKey(position)
                ? Optional.of(positionToMarkerIds.get(position))
                : Optional.empty();
    }

    public static Set<String> getMarkersInDistance(final Position position, final double distance) {
        return positionToMarkerIds
                .entrySet()
                .stream()
                .filter(x -> x.getKey().calculateEuclideanDistance(position) < distance)
                .map(Map.Entry::getValue)
                .flatMap(Collection::stream)
                .collect(Collectors.toSet());
    }

    public static ShameHole getShameHoleForMarkerId(final String markerId) {
        return markerIdsToShameHoles.get(markerId);
    }

    public static void deleteHole(String markerId) {
        markerIdsToShameHoles.remove(markerId);

        positionToMarkerIds.entrySet().removeIf(entry -> {
            entry.getValue().remove(markerId);

            return entry.getValue().isEmpty();
        });

        final var file = configDirectory
                .resolve("hole-%s.json5".formatted(markerId))
                .toFile();

        if (!file.delete()) {
            LOGGER.warn("Failed to remove %s, please remove it manually".formatted(file.toString()));
        }
    }
}
