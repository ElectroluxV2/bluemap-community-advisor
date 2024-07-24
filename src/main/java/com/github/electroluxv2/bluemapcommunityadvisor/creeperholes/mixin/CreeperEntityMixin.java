package com.github.electroluxv2.bluemapcommunityadvisor.creeperholes.mixin;

import com.github.electroluxv2.bluemapcommunityadvisor.creeperholes.core.ShameHoleCreator;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Creeper.class)
public abstract class CreeperEntityMixin extends Mob {
    @Shadow public abstract void setTarget(@Nullable LivingEntity target);

    protected CreeperEntityMixin(EntityType<? extends Mob> entityType, Level level) {
        super(entityType, level);
    }

    /** Attach player as target when igniting, so inside {@link #onExplodeCreeper} we may specify culprit correctly */
    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/monster/Creeper;ignite()V"), method = "mobInteract")
    private void onMobInteract(Player player, InteractionHand hand, CallbackInfoReturnable<InteractionResult> cir) {
        setTarget(player);
    }

    @Inject(at = @At("HEAD"), method = "explodeCreeper")
    private void onExplodeCreeper(final CallbackInfo info) {
        ShameHoleCreator.onCreeperExploded((Creeper) (Object) this);
    }
}
