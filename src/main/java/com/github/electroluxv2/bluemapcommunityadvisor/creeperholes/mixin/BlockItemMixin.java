package com.github.electroluxv2.bluemapcommunityadvisor.creeperholes.mixin;

import com.github.electroluxv2.bluemapcommunityadvisor.creeperholes.core.ShameHoleRemover;
import com.github.electroluxv2.bluemapcommunityadvisor.creeperholes.utils.Position;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.util.ActionResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockItem.class)
public class BlockItemMixin {
    @Inject(at = @At("TAIL"), method = "place(Lnet/minecraft/item/ItemPlacementContext;)Lnet/minecraft/util/ActionResult;")
    private void onPlace(ItemPlacementContext context, CallbackInfoReturnable<ActionResult> cir) {
        final Runnable logic = () -> ShameHoleRemover.onBlockPlaced(new Position(context.getBlockPos()));
        final var thread = new Thread(logic);

        thread.start();
    }
}
