package com.github.electroluxv2.bluemapcommunityadvisor.creeperholes.core;

import com.github.electroluxv2.bluemapcommunityadvisor.creeperholes.utils.Position;
import net.minecraft.server.network.ServerPlayerEntity;

import static com.github.electroluxv2.bluemapcommunityadvisor.creeperholes.core.TextFactory.createAlternatingProgressBar;

public class ShameHoleNotifier {
    public static void notifyPlayerAboutNearbyHoles(final Position position, final ServerPlayerEntity playerEntity) {
        final var markerIdsWithinPosition = ShameHoleDataManager.getMarkersInDistance(position, 2);
        if (markerIdsWithinPosition.isEmpty()) return;

        for (final var markerId : markerIdsWithinPosition) {
            final var size = ShameHoleDataManager.getPositionsForMarkerId(markerId).size();
            playerEntity.sendMessage(createAlternatingProgressBar(size), true);
            break; // Too lazy to remove this loop, it is stupid that set has no first() or root()
        }
    }
}
