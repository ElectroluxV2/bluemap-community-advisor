package com.github.electroluxv2.bluemapcommunityadvisor.creeperholes.utils;

import net.minecraft.util.math.BlockPos;

public record Position(double x, double y, double z) {
    public Position(BlockPos blockPos) {
        this(blockPos.getX(), blockPos.getY(), blockPos.getZ());
    }

    public double calculateEuclideanDistance(final Position other) {
        return Math.sqrt(Math.pow(other.x - this.x, 2) + Math.pow(other.y - this.y, 2) + Math.pow(other.z - this.z, 2));
    }
}