package com.curriculo.inteligente.ai;

import com.curriculo.inteligente.common.ApiException;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class InventionGuard {

    private static final Pattern NUMBER = Pattern.compile("\\d{2,}");
    private static final Pattern PERCENT = Pattern.compile("\\d+\\s*%");

    private InventionGuard() {
    }

    public static void assertFaithful(String source, String revised) {
        if (revised == null || revised.isBlank()) {
            throw new ApiException(422, "AI_INVALID_OUTPUT", "A revisão veio vazia.");
        }
        String base = source == null ? "" : source;
        Matcher percent = PERCENT.matcher(revised);
        while (percent.find()) {
            if (!base.contains(percent.group().replace(" ", ""))) {
                throw new ApiException(422, "AI_INVALID_OUTPUT",
                        "A revisão trouxe um percentual que não estava no texto original.");
            }
        }
        Matcher numbers = NUMBER.matcher(revised);
        while (numbers.find()) {
            if (!base.contains(numbers.group())) {
                throw new ApiException(422, "AI_INVALID_OUTPUT",
                        "A revisão trouxe um número que não estava no texto original.");
            }
        }
    }

    public static String keepFaithful(String source, String revised) {
        if (revised == null || revised.isBlank()) {
            return "";
        }
        String[] sentences = revised.trim().split("(?<=[.!?])\\s+");
        StringBuilder kept = new StringBuilder();
        for (String sentence : sentences) {
            String piece = sentence.trim();
            if (piece.isBlank()) {
                continue;
            }
            try {
                assertFaithful(source, piece);
            } catch (ApiException ex) {
                continue;
            }
            if (!kept.isEmpty()) {
                kept.append(' ');
            }
            kept.append(piece);
        }
        return kept.toString();
    }
}
