package com.github.electroluxv2.bluemapcommunityadvisor.creeperholes.utils;

import net.minecraft.util.math.BlockPos;

public record Position(double x, double y, double z) {
    public Position(BlockPos blockPos) {
        this(blockPos.getX(), blockPos.getY(), blockPos.getZ());
    }
}