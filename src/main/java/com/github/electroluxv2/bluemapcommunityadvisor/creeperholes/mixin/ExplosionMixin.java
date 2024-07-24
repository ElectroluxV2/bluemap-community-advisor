package com.github.electroluxv2.bluemapcommunityadvisor.creeperholes.mixin;

import com.github.electroluxv2.bluemapcommunityadvisor.creeperholes.core.ShameHoleCreator;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Explosion.class)
public abstract class ExplosionMixin {
    @Shadow @Final private Level level;

    @Inject(at = @At("TAIL"), method = "explode")
    private void afterCollectBlocksAndDamageEntities(final CallbackInfo ci) {
        ShameHoleCreator.afterExplosionCollectBlocks(level, (Explosion) (Object) this);
    }

    @Inject(at = @At("TAIL"), method = "finalizeExplosion")
    private void afterAffectWorld(final CallbackInfo ci) {
        ShameHoleCreator.afterExplosionAffectWorld((Explosion) (Object) this);
    }
}
