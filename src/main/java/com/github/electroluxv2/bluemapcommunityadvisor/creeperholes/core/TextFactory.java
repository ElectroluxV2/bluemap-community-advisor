package com.github.electroluxv2.bluemapcommunityadvisor.creeperholes.core;

import net.minecraft.text.Text;

import java.util.StringJoiner;

public class TextFactory {
    private record TextBuilder(StringJoiner segments) {
        public static TextBuilder of(final String text, final String color) {
                return new TextBuilder(new StringJoiner(",")).with(text, color);
        }

        public TextBuilder with(final String text, final String color) {
            segments.add(colored(text, color));
            return this;
        }

        public Text build() {
            return fromJsonSegments(segments.toString());
        }

        private static Text fromJsonSegments(final String segments) {
            return Text.Serializer.fromJson("[%s]".formatted(segments));
        }

        private static String colored(final String text, final String color) {
            return "{\"text\":\"%s\",\"color\":\"%s\"}".formatted(text, color);
        }
    }

    public static Text createAlternatingProgressBar(final int total) {
        final var text = TextBuilder.of("[ ", "red");

        for (int i = 0; i < total; i++) {
            final var color = i % 2 == 0
                    ? "dark_green"
                    : "green";

            text.with("|", color);
        }

        text.with(" ]", "red");

        return text.build();
    }

    public static Text createConfirm(final String message) {
        return TextBuilder.of(message, "gold").build();
    }
}
