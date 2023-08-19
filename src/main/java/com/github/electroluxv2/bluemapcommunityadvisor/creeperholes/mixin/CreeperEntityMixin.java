package com.github.electroluxv2.bluemapcommunityadvisor.creeperholes.mixin;

import com.github.electroluxv2.bluemapcommunityadvisor.creeperholes.core.ShameHoleCreator;
import com.github.electroluxv2.bluemapcommunityadvisor.creeperholes.interfaces.CreeperAccessors;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.CreeperEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;


@Mixin(CreeperEntity.class)
public abstract class CreeperEntityMixin extends MobEntity implements CreeperAccessors {
    @Shadow public abstract void setTarget(@Nullable LivingEntity target);

    @Shadow private int explosionRadius;

    protected CreeperEntityMixin(EntityType<? extends CreeperEntity> entityType, World world) {
        super(entityType, world);
    }

    /** Attach player as target when igniting, so inside {@link #onExplode} we may specify culprit correctly */
    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/mob/CreeperEntity;ignite()V"), method = "interactMob")
    private void onIgnite(PlayerEntity player, Hand hand, CallbackInfoReturnable<ActionResult> cir) {
        setTarget(player);
    }

    @Inject(at = @At("HEAD"), method = "explode")
    private void onExplode(final CallbackInfo info) {
        ShameHoleCreator.onCreeperExploded((CreeperEntity) (Object) this);
    }

    @Override
    public Optional<Double> bluemap_community_advisor$getEplosionRadius() {
        return Optional.of((double) explosionRadius);
    }
}
