package com.github.electroluxv2.bluemapcommunityadvisor.spawnermarker.mixin;

import com.github.electroluxv2.bluemapcommunityadvisor.spawnermarker.interfaces.SpawnerBlockAccessors;
import net.minecraft.block.SpawnerBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Optional;

@Mixin(SpawnerBlock.class)
public abstract class SpawnerBlockMixin implements SpawnerBlockAccessors {
    @Shadow protected abstract Optional<Text> getEntityNameForTooltip(ItemStack stack);

    @Override
    public Optional<Text> bluemap_community_advisor$getEntityNameForTooltip(final ItemStack stack) {
        return this.getEntityNameForTooltip(stack);
    }
}
