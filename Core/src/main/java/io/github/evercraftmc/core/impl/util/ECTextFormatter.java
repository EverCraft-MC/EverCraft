package io.github.evercraftmc.core.impl.util;

import java.util.regex.Pattern;
import org.jetbrains.annotations.NotNull;

public final class ECTextFormatter {
    public static final char FROM_COLOR_CHAR = '&';
    public static final char TO_COLOR_CHAR = '§';

    private static final @NotNull String ALL_CODES = "[0123456789AaBbCcDdEeFfKkLlMmNnOoRr]";

    private static final @NotNull Pattern TRANSLATE_COLOR_PATTERN = Pattern.compile(FROM_COLOR_CHAR + "(" + ALL_CODES + ")");
    private static final @NotNull String TRANSLATE_COLOR_REPLACEMENT = TO_COLOR_CHAR + "$1";
    private static final @NotNull Pattern UNTRANSLATE_COLOR_PATTERN = Pattern.compile(TO_COLOR_CHAR + "(" + ALL_CODES + ")");
    private static final @NotNull String UNTRANSLATE_COLOR_REPLACEMENT = FROM_COLOR_CHAR + "$1";
    private static final @NotNull Pattern STRIP_COLOR_PATTERN = Pattern.compile("[" + TO_COLOR_CHAR + FROM_COLOR_CHAR + "]" + ALL_CODES);

    private ECTextFormatter() {
    }

    public static @NotNull String translateColors(@NotNull String input) {
        return TRANSLATE_COLOR_PATTERN.matcher(input).replaceAll(TRANSLATE_COLOR_REPLACEMENT);
    }

    public static @NotNull String untranslateColors(@NotNull String input) {
        return UNTRANSLATE_COLOR_PATTERN.matcher(input).replaceAll(UNTRANSLATE_COLOR_REPLACEMENT);
    }

    public static @NotNull String stripColors(@NotNull String input) {
        return STRIP_COLOR_PATTERN.matcher(input).replaceAll("");
    }
}