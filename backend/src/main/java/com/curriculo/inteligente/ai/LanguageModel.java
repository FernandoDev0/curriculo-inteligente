package com.curriculo.inteligente.ai;

import java.util.UUID;

public interface LanguageModel {

    ConnectionResult testConnection(UUID userId);

    String complete(UUID userId, String purpose, String systemInstruction, String userContent, boolean json);

    boolean imageEditingAvailable();

    record ConnectionResult(String state, String message, boolean imageEditingAvailable) {
    }
}
