package com.github.electroluxv2.bluemapcommunityadvisor.creeperholes.core;

import com.github.electroluxv2.bluemapcommunityadvisor.creeperholes.utils.Position;

import static com.github.electroluxv2.bluemapcommunityadvisor.BlueMapCommunityAdvisor.LOGGER;

public class ShameHoleRemover {
    public static void onBlockPlaced(final Position position) {
        final var markerIdsWithinPosition = ShameHoleDataManager.getMarkerIdsForPosition(position);
        if (markerIdsWithinPosition.isEmpty()) return;


        for (final var markerId : markerIdsWithinPosition.get()) {
            final var positions = ShameHoleDataManager.getPositionsForMarkerId(markerId);

            if (!positions.contains(position)) {
                continue;
            }

            positions.remove(position);

            if (positions.size() > 3) {
                ShameHoleDataManager.saveHole(markerId, positions);
                continue;
            }

            LOGGER.info("Hole: %s is fixed, removing marker.".formatted(markerId));

            ShameHoleDataManager.deleteHole(markerId);
            ShameHoleMarkerManager.removeShameMarker(markerId);
        }
    }
}
