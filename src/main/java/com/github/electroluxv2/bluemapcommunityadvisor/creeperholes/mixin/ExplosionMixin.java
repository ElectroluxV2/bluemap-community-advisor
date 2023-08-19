package com.github.electroluxv2.bluemapcommunityadvisor.creeperholes.mixin;

import com.flowpowered.math.vector.Vector3d;
import com.github.electroluxv2.bluemapcommunityadvisor.creeperholes.core.CreeperShameHoles;
import com.github.electroluxv2.bluemapcommunityadvisor.creeperholes.interfaces.ExplosionAccessors;
import net.minecraft.world.World;
import net.minecraft.world.explosion.Explosion;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Optional;

@Mixin(Explosion.class)
public abstract class ExplosionMixin implements ExplosionAccessors {
    @Shadow @Final private World world;

    @Shadow @Final private double x;

    @Shadow @Final private double z;

    @Shadow @Final private double y;

    @Inject(at = @At("TAIL"), method = "collectBlocksAndDamageEntities")
    private void afterCollectBlocksAndDamageEntities(final CallbackInfo ci) {
        CreeperShameHoles.afterExplosionCollectBlocks(world, (Explosion) (Object) this);
    }

    @Inject(at = @At("TAIL"), method = "affectWorld")
    private void afterAffectWorld(final CallbackInfo ci) {
        CreeperShameHoles.afterExplosionAffectWorld((Explosion) (Object) this);
    }

    @Override
    public Optional<Vector3d> bluemap_community_advisor$getVector3d() {
        return Optional.of(new Vector3d(x, y, z));
    }
}
