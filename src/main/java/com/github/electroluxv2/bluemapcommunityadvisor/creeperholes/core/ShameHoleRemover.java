package com.github.electroluxv2.bluemapcommunityadvisor.creeperholes.core;

import com.github.electroluxv2.bluemapcommunityadvisor.creeperholes.utils.Position;
import net.minecraft.entity.player.PlayerEntity;

import static com.github.electroluxv2.bluemapcommunityadvisor.creeperholes.core.TextFactory.createAlternatingProgressBar;
import static com.github.electroluxv2.bluemapcommunityadvisor.creeperholes.core.TextFactory.createConfirm;

public class ShameHoleRemover {
    public static void onBlockPlaced(final Position position, final PlayerEntity player) {
        final var markerIdsWithinPosition = ShameHoleDataManager.getMarkerIdsForPosition(position);
        if (markerIdsWithinPosition.isEmpty()) return;


        for (final var markerId : markerIdsWithinPosition.get()) {
            final var positions = ShameHoleDataManager.getPositionsForMarkerId(markerId);

            if (!positions.contains(position)) {
                continue;
            }

            positions.remove(position);

            player.sendMessage(createAlternatingProgressBar(positions.size()), true);

            if (positions.size() > 3) {
                ShameHoleDataManager.saveHole(markerId, positions);
                continue;
            }

            player.sendMessage(createConfirm("Fixed hole, marker removed"), true);

            ShameHoleDataManager.deleteHole(markerId);
            ShameHoleMarkerManager.removeShameMarker(markerId);
        }
    }
}
