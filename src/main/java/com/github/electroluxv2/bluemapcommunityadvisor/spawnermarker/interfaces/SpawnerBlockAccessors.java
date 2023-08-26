package com.github.electroluxv2.bluemapcommunityadvisor.spawnermarker.interfaces;

import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;

import java.util.Optional;

public interface SpawnerBlockAccessors {
    default Optional<Text> bluemap_community_advisor$getEntityNameForTooltip(final ItemStack stack) {
        return Optional.empty();
    }
}
