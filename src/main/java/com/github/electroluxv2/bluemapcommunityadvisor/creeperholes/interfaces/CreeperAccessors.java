package com.github.electroluxv2.bluemapcommunityadvisor.creeperholes.interfaces;

import java.util.Optional;

public interface CreeperAccessors {
    default Optional<Double> bluemap_community_advisor$getEplosionRadius() {
        return Optional.empty();
    }
}
