package com.curriculo.inteligente.common;

import java.text.Normalizer;
import java.util.Locale;

public final class Text {

    private Text() {
    }

    public static String plain(String value) {
        if (value == null) {
            return "";
        }
        String normalized = Normalizer.normalize(value, Normalizer.Form.NFD).replaceAll("\\p{M}", "");
        return normalized.toLowerCase(Locale.ROOT).trim();
    }

    public static boolean blank(String value) {
        return value == null || value.isBlank();
    }
}
