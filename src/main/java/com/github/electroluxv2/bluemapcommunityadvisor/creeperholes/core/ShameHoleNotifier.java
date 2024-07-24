package com.github.electroluxv2.bluemapcommunityadvisor.creeperholes.core;

import com.github.electroluxv2.bluemapcommunityadvisor.creeperholes.utils.Position;
import net.minecraft.server.level.ServerPlayer;

import static com.github.electroluxv2.bluemapcommunityadvisor.BlueMapCommunityAdvisor.EXECUTOR;
import static com.github.electroluxv2.bluemapcommunityadvisor.creeperholes.core.TextFactory.createAlternatingProgressBar;

public class ShameHoleNotifier {
    public static void notifyPlayerAboutNearbyHoles(final Position position, final ServerPlayer playerEntity) {
        EXECUTOR.execute(() -> logic(position, playerEntity));
    }

    private static void logic(final Position position, final ServerPlayer playerEntity) {
        final var markerIdsWithinPosition = ShameHoleDataManager.getMarkersInDistance(position, 2);
        if (markerIdsWithinPosition.isEmpty()) return;

        for (final var markerId : markerIdsWithinPosition) {
            final var size = ShameHoleDataManager.getShameHoleForMarkerId(markerId).positions().size();
            playerEntity.displayClientMessage(createAlternatingProgressBar(size), true);

            break; // Too lazy to remove this loop, it is stupid that set has no first() or root()
        }
    }
}
