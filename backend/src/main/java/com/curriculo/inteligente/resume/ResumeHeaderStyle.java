package com.curriculo.inteligente.resume;

import com.curriculo.inteligente.common.Text;

import java.util.Locale;
import java.util.Set;

public final class ResumeHeaderStyle {

    private static final Locale PT = Locale.forLanguageTag("pt-BR");
    private static final Set<String> SMALL = Set.of("de", "da", "do", "das", "dos", "e", "di", "du");

    private ResumeHeaderStyle() {
    }

    public static String personName(String value) {
        if (value == null || value.isBlank()) {
            return value;
        }
        String[] words = value.trim().toLowerCase(PT).split("\\s+");
        StringBuilder name = new StringBuilder();
        for (int i = 0; i < words.length; i++) {
            if (i > 0) {
                name.append(' ');
            }
            name.append(word(words[i], i == 0));
        }
        return name.toString();
    }

    public static String placeName(String value) {
        return personName(value);
    }

    public static String state(String value) {
        if (value == null || value.isBlank()) {
            return value;
        }
        String trimmed = value.trim();
        if (trimmed.length() == 2) {
            return trimmed.toUpperCase(PT);
        }
        return personName(trimmed);
    }

    public static String phone(String value) {
        if (value == null || value.isBlank()) {
            return value;
        }
        String digits = value.replaceAll("\\D", "");
        if (digits.startsWith("55") && digits.length() > 11) {
            digits = digits.substring(2);
        }
        if (digits.length() == 11) {
            return "(" + digits.substring(0, 2) + ") " + digits.substring(2, 7) + "-" + digits.substring(7);
        }
        if (digits.length() == 10) {
            return "(" + digits.substring(0, 2) + ") " + digits.substring(2, 6) + "-" + digits.substring(6);
        }
        return value.trim();
    }

    public static boolean sameWords(String original, String polished) {
        if (original == null || polished == null || original.isBlank() || polished.isBlank()) {
            return false;
        }
        String main = Text.plain(original).replaceAll("[^a-z0-9 ]", " ").trim().split("\\s+")[0];
        return main.length() >= 3 && Text.plain(polished).contains(main);
    }

    public static boolean sameDigits(String original, String polished) {
        if (original == null || polished == null) {
            return false;
        }
        String left = original.replaceAll("\\D", "");
        String right = polished.replaceAll("\\D", "");
        if (left.startsWith("55") && left.length() > 11) {
            left = left.substring(2);
        }
        if (right.startsWith("55") && right.length() > 11) {
            right = right.substring(2);
        }
        return !left.isBlank() && left.equals(right);
    }

    private static String word(String token, boolean first) {
        String lower = token.toLowerCase(PT);
        if (!first && SMALL.contains(lower)) {
            return lower;
        }
        if (token.contains("-")) {
            String[] parts = token.split("-");
            StringBuilder joined = new StringBuilder();
            for (int i = 0; i < parts.length; i++) {
                if (i > 0) {
                    joined.append('-');
                }
                joined.append(capital(parts[i]));
            }
            return joined.toString();
        }
        return capital(token);
    }

    private static String capital(String token) {
        if (token.isBlank()) {
            return token;
        }
        String lower = token.toLowerCase(PT);
        return lower.substring(0, 1).toUpperCase(PT) + lower.substring(1);
    }
}
