package com.github.electroluxv2.bluemapcommunityadvisor.creeperholes.mixin;

import com.github.electroluxv2.bluemapcommunityadvisor.creeperholes.core.ShameHoleNotifier;
import com.github.electroluxv2.bluemapcommunityadvisor.creeperholes.utils.Position;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.concurrent.atomic.AtomicLong;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin extends Entity {
    @Unique
    private static final int threshold = 50;
    @Unique
    private static final AtomicLong tickCounter = new AtomicLong(0);

    public PlayerEntityMixin(EntityType<?> type, World world) {
        super(type, world);
    }

    @Inject(method = "tickMovement", at = @At("HEAD"))
    public void tickMovement(CallbackInfo callbackInfo) {
        if (tickCounter.incrementAndGet() % threshold != 0) return;
        // noinspection ConstantConditions
        if (!((Object) this instanceof ServerPlayerEntity playerEntity)) return;

        final var thread = new Thread(() -> ShameHoleNotifier.notifyPlayerAboutNearbyHoles(new Position(this.getBlockPos()), playerEntity));
        thread.start(); // I don't give a fuck if it fails
    }
}
