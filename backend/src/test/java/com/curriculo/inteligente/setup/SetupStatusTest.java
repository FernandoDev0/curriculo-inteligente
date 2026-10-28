package com.curriculo.inteligente.setup;

import tools.jackson.databind.json.JsonMapper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;

class SetupStatusTest {

    @Test
    void statusPayloadDoesNotCarryAKey() {
        SetupController.Status status = new SetupController.Status(
                false, "NONE", "gemini-3.5-flash", false, "KEY_MISSING", null, "Nenhuma chave foi configurada.");
        String json = JsonMapper.builder().build().writeValueAsString(status);
        assertFalse(json.contains("apiKey"));
        assertFalse(json.contains("AIza"));
        assertFalse(json.toLowerCase().contains("secret"));
    }
}
