package com.github.electroluxv2.bluemapcommunityadvisor.creeperholes.mixin;

import com.github.electroluxv2.bluemapcommunityadvisor.creeperholes.core.ShameHoleRemover;
import com.github.electroluxv2.bluemapcommunityadvisor.creeperholes.utils.Position;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockItem.class)
public class BlockItemMixin {
    @Inject(at = @At("TAIL"), method = "place")
    private void onPlace(BlockPlaceContext context, CallbackInfoReturnable<InteractionResult> cir) {
        ShameHoleRemover.onBlockPlaced(new Position(context.getClickedPos()), context.getPlayer());
    }
}
