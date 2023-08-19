package com.github.electroluxv2.bluemapcommunityadvisor.interfaces;

import com.flowpowered.math.vector.Vector3d;

import java.util.Optional;

public interface ExplosionAccessors {
    default Optional<Vector3d> bluemap_community_advisor$getVector3d() {
        return Optional.empty();
    }
}
