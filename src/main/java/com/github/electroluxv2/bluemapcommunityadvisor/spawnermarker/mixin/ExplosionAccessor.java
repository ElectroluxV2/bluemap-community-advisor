package com.github.electroluxv2.bluemapcommunityadvisor.spawnermarker.mixin;

import net.minecraft.world.level.Explosion;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Explosion.class)
public interface ExplosionAccessor {
    @Accessor
    double getX();

    @Accessor
    double getY();

    @Accessor
    double getZ();
}
