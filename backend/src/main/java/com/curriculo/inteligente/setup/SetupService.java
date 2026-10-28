package com.curriculo.inteligente.setup;

import com.curriculo.inteligente.ai.LanguageModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
public class SetupService {

    private final ApiKeyStore keys;
    private final LanguageModel model;
    private final String modelName;
    private volatile Instant lastTestAt;
    private volatile String lastState;
    private volatile String lastMessage;

    public SetupService(ApiKeyStore keys, LanguageModel model, @Value("${app.gemini.model}") String modelName) {
        this.keys = keys;
        this.model = model;
        this.modelName = modelName;
    }

    public SetupController.Status status() {
        boolean configured = keys.resolve() != null;
        String state = lastState;
        if (!configured) {
            state = "KEY_MISSING";
        } else if (state == null) {
            state = "UNTESTED";
        }
        return new SetupController.Status(
                configured,
                keys.source(),
                modelName,
                model.imageEditingAvailable(),
                state,
                lastTestAt,
                configured ? lastMessage : "Nenhuma chave foi configurada.");
    }

    public SetupController.Status saveKey(String apiKey) {
        keys.write(apiKey);
        lastState = "UNTESTED";
        lastMessage = "Chave gravada fora do código. Rode o teste de conexão.";
        lastTestAt = null;
        return status();
    }

    public SetupController.Status deleteKey() {
        keys.deleteFile();
        lastState = null;
        lastMessage = null;
        lastTestAt = null;
        return status();
    }

    public SetupController.Status test(UUID userId) {
        LanguageModel.ConnectionResult result = model.testConnection(userId);
        lastState = result.state();
        lastMessage = result.message();
        lastTestAt = Instant.now();
        return status();
    }
}
