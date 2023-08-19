package com.github.electroluxv2.bluemapcommunityadvisor.interfaces;

import java.util.Optional;

public interface CreeperAccessors {
    default Optional<Double> bluemap_community_advisor$getEplosionRadius() {
        return Optional.empty();
    }
}
