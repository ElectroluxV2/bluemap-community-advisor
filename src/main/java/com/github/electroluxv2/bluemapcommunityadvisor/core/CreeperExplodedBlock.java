package com.github.electroluxv2.bluemapcommunityadvisor.core;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;

public class CreeperExplodedBlock extends BlockPos {
    private final BlockState blockState;

    public CreeperExplodedBlock(BlockPos pos, BlockState state) {
        super(pos);
        blockState = state;
    }

    public BlockState getBlockState() {
        return blockState;
    }

    public BlockPos asBlockPos() {
        return this;
    }
}
