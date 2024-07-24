package com.github.electroluxv2.bluemapcommunityadvisor.creeperholes.mixin;

import com.github.electroluxv2.bluemapcommunityadvisor.creeperholes.core.ShameHoleNotifier;
import com.github.electroluxv2.bluemapcommunityadvisor.creeperholes.utils.Position;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.concurrent.atomic.AtomicLong;

@Mixin(Player.class)
public abstract class PlayerEntityMixin extends Entity {
    @Unique
    private static final int threshold = 50;
    @Unique
    private final AtomicLong tickCounter = new AtomicLong(0);

    public PlayerEntityMixin(EntityType<?> type, Level world) {
        super(type, world);
    }

    @Inject(method = "tick", at = @At("HEAD"))
    public void tickMovement(CallbackInfo callbackInfo) {
        if (tickCounter.incrementAndGet() % threshold != 0) return;
        // noinspection ConstantConditions
        if (!((Object) this instanceof ServerPlayer playerEntity)) return;

        ShameHoleNotifier.notifyPlayerAboutNearbyHoles(new Position(this.blockPosition()), playerEntity);
    }
}
