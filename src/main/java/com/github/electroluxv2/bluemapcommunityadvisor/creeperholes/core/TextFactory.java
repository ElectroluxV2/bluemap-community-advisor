package com.github.electroluxv2.bluemapcommunityadvisor.creeperholes.core;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

@SuppressWarnings("DataFlowIssue")
public class TextFactory {
    public static Component createAlternatingProgressBar(final int total) {
        final var text = Component.literal("[ ").withColor(ChatFormatting.RED.getColor());

        for (int i = 0; i < total; i++) {
            final var color = i % 2 == 0
                ? ChatFormatting.DARK_GREEN.getColor()
                : ChatFormatting.GREEN.getColor();

            text.append(Component.literal("|").withColor(color));
        }

        text.append(Component.literal(" ]").withColor(ChatFormatting.RED.getColor()));

        return text;
    }

    public static Component createConfirm(final String message) {
        return Component.literal(message).withColor(ChatFormatting.GOLD.getColor());
    }
}
