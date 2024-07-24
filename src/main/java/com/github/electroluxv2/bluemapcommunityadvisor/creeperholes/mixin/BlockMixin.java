package com.github.electroluxv2.bluemapcommunityadvisor.creeperholes.mixin;

import com.github.electroluxv2.bluemapcommunityadvisor.creeperholes.core.ShameHoleCreator;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Block.class)
public abstract class BlockMixin {
    @Inject(at = @At("HEAD"), method = "wasExploded")
    private void onDestroyedByExplosion(Level level, BlockPos pos, Explosion explosion, CallbackInfo ci) {
        ShameHoleCreator.onBlockDestroyedByExplosion(explosion, pos);
    }
}
