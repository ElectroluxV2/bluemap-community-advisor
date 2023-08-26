package com.github.electroluxv2.bluemapcommunityadvisor.spawnermarker.mixin;

import com.github.electroluxv2.bluemapcommunityadvisor.spawnermarker.core.SpawnerMarkerCreator;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.concurrent.atomic.AtomicLong;

@Mixin(PlayerEntity.class)
public class PlayerEntityMixin {
    @Unique
    private static final int threshold = 5;
    @Unique
    private static final AtomicLong tickCounter = new AtomicLong(0);

    @Inject(at=@At("HEAD"), method = "tick")
    public void onTick(CallbackInfo ci) {
        if(tickCounter.incrementAndGet() % threshold != 0) return;

        // noinspection ConstantValue
        if (!((Object) this instanceof ServerPlayerEntity playerEntity)) return;

        SpawnerMarkerCreator.onPlayerTick(playerEntity, threshold);
    }
}
