package com.github.electroluxv2.bluemapcommunityadvisor.mixin;

import com.github.electroluxv2.bluemapcommunityadvisor.core.CreeperShameHoles;
import net.minecraft.block.Block;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.explosion.Explosion;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Block.class)
public abstract class BlockMixin {
    @Inject(at = @At("HEAD"), method = "onDestroyedByExplosion")
    private void onDestroyedByExplosion(World world, BlockPos pos, Explosion explosion, CallbackInfo ci) {
        CreeperShameHoles.onBlockDestroyedByExplosion(explosion, pos);
    }
}
