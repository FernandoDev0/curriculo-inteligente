package com.curriculo.inteligente.ai;

import com.curriculo.inteligente.common.ApiException;
import com.curriculo.inteligente.setup.ApiKeyStore;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

@Service
public class GeminiLanguageModel implements LanguageModel {

    private static final Logger log = LoggerFactory.getLogger(GeminiLanguageModel.class);
    private static final int HOURLY_LIMIT = 30;
    private static final List<String> FALLBACK_MODELS = List.of(
            "gemini-3.5-flash-lite",
            "gemini-3.6-flash",
            "gemini-3.8-flash",
            "gemini-2.5-flash",
            "gemini-2.5-flash-lite");

    private final ApiKeyStore keys;
    private final AiInvocationRepository invocations;
    private final JsonMapper jsonMapper;
    private final RestClient http;
    private final String model;
    private final String imageModel;
    private final Map<UUID, Deque<Instant>> calls = new ConcurrentHashMap<>();
    private final AtomicBoolean imageEditing = new AtomicBoolean(false);
    private volatile String activeModel;

    public GeminiLanguageModel(ApiKeyStore keys, AiInvocationRepository invocations, JsonMapper jsonMapper,
                               @Value("${app.gemini.base-url}") String baseUrl,
                               @Value("${app.gemini.model}") String model,
                               @Value("${app.gemini.image-model:}") String imageModel) {
        this.keys = keys;
        this.invocations = invocations;
        this.jsonMapper = jsonMapper;
        this.model = model;
        this.imageModel = imageModel == null ? "" : imageModel.trim();
        JdkClientHttpRequestFactory factory = new JdkClientHttpRequestFactory();
        factory.setReadTimeout(Duration.ofSeconds(25));
        this.http = RestClient.builder().baseUrl(baseUrl).requestFactory(factory).build();
    }

    @Override
    public boolean imageEditingAvailable() {
        return imageEditing.get();
    }

    @Override
    public ConnectionResult testConnection(UUID userId) {
        String key = keys.resolve();
        if (key == null) {
            return new ConnectionResult("KEY_MISSING", "Nenhuma chave foi configurada.", false);
        }
        limit(userId);
        long started = System.nanoTime();
        try {
            String generated = post(key, Map.of(
                    "systemInstruction", Map.of("parts", new Object[] {Map.of("text", "Responda somente com a palavra ok.")}),
                    "contents", new Object[] {Map.of("role", "user", "parts", new Object[] {Map.of("text", "ping")})},
                    "generationConfig", generationConfig(64)));
            String text = textOf(generated);
            if (text.isBlank()) {
                record(userId, "TEST", "UNAVAILABLE", started, 4, 0);
                return new ConnectionResult("UNAVAILABLE", emptyTextMessage(generated), false);
            }
            boolean images = false;
            if (!imageModel.isBlank()) {
                String imageBody = get(key, "/v1beta/models/" + imageModel);
                images = imageBody != null && imageBody.toUpperCase().contains("IMAGE");
            }
            imageEditing.set(images);
            record(userId, "TEST", "OK", started, 4, text.length());
            String message = images
                    ? "Conexão validada com " + activeModel + ". O modelo de imagem anunciou saída de imagem."
                    : "Conexão validada com " + activeModel + ". Edição de foto não está disponível neste modelo.";
            return new ConnectionResult("VALIDATED", message, images);
        } catch (ApiException ex) {
            record(userId, "TEST", ex.code(), started, 0, 0);
            return new ConnectionResult(ex.code(), ex.getMessage(), false);
        }
    }

    @Override
    public String complete(UUID userId, String purpose, String systemInstruction, String userContent, boolean json) {
        String key = keys.resolve();
        if (key == null) {
            throw new ApiException(409, "KEY_MISSING", "Configure a chave do Gemini para usar este recurso.");
        }
        limit(userId);
        long started = System.nanoTime();
        Map<String, Object> generation = json
                ? generationConfig(2048, "application/json")
                : generationConfig(2048);
        try {
            String body = post(key, Map.of(
                    "systemInstruction", Map.of("parts", new Object[] {Map.of("text", systemInstruction)}),
                    "contents", new Object[] {Map.of("role", "user", "parts", new Object[] {Map.of("text", userContent)})},
                    "generationConfig", generation));
            String text = textOf(body);
            if (text.isBlank()) {
                record(userId, purpose, "UNAVAILABLE", started, userContent.length(), 0);
                throw new ApiException(503, "AI_UNAVAILABLE", emptyTextMessage(body));
            }
            record(userId, purpose, "OK", started, userContent.length(), text.length());
            return text;
        } catch (ApiException ex) {
            if (!"OK".equals(ex.code())) {
                record(userId, purpose, ex.code(), started, userContent.length(), 0);
            }
            throw ex;
        }
    }

    private String get(String key, String path) {
        try {
            return http.get().uri(path).header("x-goog-api-key", key).retrieve().body(String.class);
        } catch (RestClientResponseException ex) {
            throw mapped(ex);
        } catch (ResourceAccessException ex) {
            throw new ApiException(503, "CONNECTIVITY", "Não foi possível alcançar o serviço de IA.");
        }
    }

    private String post(String key, Map<String, Object> body) {
        ApiException last = null;
        for (String candidate : modelOrder()) {
            try {
                String generated = exchange(key, "/v1beta/models/" + candidate + ":generateContent", body);
                activeModel = candidate;
                return generated;
            } catch (ApiException ex) {
                if ("MODEL_REJECTED".equals(ex.code())) {
                    try {
                        String generated = exchange(key, "/v1beta/models/" + candidate + ":generateContent", withoutThinking(body));
                        activeModel = candidate;
                        return generated;
                    } catch (ApiException again) {
                        ex = again;
                    }
                }
                if (!skippable(ex)) {
                    throw ex;
                }
                log.info("Modelo {} respondeu {}. Seguindo para o proximo.", candidate, ex.code());
                last = ex;
            }
        }
        throw last == null
                ? new ApiException(503, "UNAVAILABLE", "Nenhum modelo da lista respondeu.")
                : new ApiException(429, "QUOTA", "Todos os modelos da lista atingiram o limite ou estão indisponíveis. Espere um pouco e tente de novo.");
    }

    private List<String> modelOrder() {
        LinkedHashSet<String> order = new LinkedHashSet<>();
        if (activeModel != null && !activeModel.isBlank()) {
            order.add(activeModel);
        }
        if (model != null && !model.isBlank()) {
            order.add(model.trim());
        }
        order.addAll(FALLBACK_MODELS);
        return new ArrayList<>(order);
    }

    private static boolean skippable(ApiException ex) {
        return "QUOTA".equals(ex.code())
                || "UNAVAILABLE".equals(ex.code())
                || "CONNECTIVITY".equals(ex.code())
                || "MODEL_REJECTED".equals(ex.code());
    }

    private static Map<String, Object> withoutThinking(Map<String, Object> body) {
        Map<String, Object> copy = new java.util.LinkedHashMap<>(body);
        Object generation = copy.get("generationConfig");
        if (generation instanceof Map<?, ?> config) {
            Map<String, Object> next = new java.util.LinkedHashMap<>();
            config.forEach((key, value) -> {
                if (!"thinkingConfig".equals(String.valueOf(key))) {
                    next.put(String.valueOf(key), value);
                }
            });
            copy.put("generationConfig", next);
        }
        return copy;
    }

    private String exchange(String key, String path, Map<String, Object> body) {
        try {
            return http.post().uri(path)
                    .header("x-goog-api-key", key)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .body(String.class);
        } catch (RestClientResponseException ex) {
            log.info("Gemini HTTP {}", ex.getStatusCode().value());
            throw mapped(ex);
        } catch (ResourceAccessException ex) {
            throw new ApiException(503, "CONNECTIVITY", "Não foi possível alcançar o serviço de IA.");
        }
    }

    private ApiException mapped(RestClientResponseException ex) {
        int status = ex.getStatusCode().value();
        if (status == 401 || status == 403) {
            return new ApiException(401, "INVALID_CREDENTIALS", "A chave foi recusada pelo provedor.");
        }
        if (status == 400) {
            return new ApiException(400, "MODEL_REJECTED", "O modelo recusou o formato do pedido.");
        }
        if (status == 429) {
            return new ApiException(429, "QUOTA", "A conta atingiu o limite de uso do provedor.");
        }
        if (status == 404) {
            return new ApiException(503, "UNAVAILABLE", "O modelo configurado não está disponível.");
        }
        return new ApiException(503, "UNAVAILABLE", "O Gemini respondeu 503 e está temporariamente indisponível. Espere um pouco e tente de novo.");
    }

    private Map<String, Object> generationConfig(int maxOutputTokens) {
        return generationConfig(maxOutputTokens, null);
    }

    private Map<String, Object> generationConfig(int maxOutputTokens, String responseMimeType) {
        Map<String, Object> config = new java.util.LinkedHashMap<>();
        config.put("maxOutputTokens", maxOutputTokens);
        config.put("thinkingConfig", Map.of("thinkingBudget", 0));
        if (responseMimeType != null) {
            config.put("responseMimeType", responseMimeType);
        }
        return config;
    }

    private String textOf(String body) {
        if (body == null || body.isBlank()) {
            return "";
        }
        JsonNode parts = jsonMapper.readTree(body).path("candidates").path(0).path("content").path("parts");
        if (!parts.isArray()) {
            return "";
        }
        StringBuilder text = new StringBuilder();
        for (JsonNode part : parts) {
            if (part.path("thought").asBoolean(false)) {
                continue;
            }
            String piece = part.path("text").asText("");
            if (!piece.isBlank()) {
                if (!text.isEmpty()) {
                    text.append('\n');
                }
                text.append(piece.trim());
            }
        }
        return text.toString();
    }

    private String emptyTextMessage(String body) {
        String reason = "";
        if (body != null && !body.isBlank()) {
            reason = jsonMapper.readTree(body).path("candidates").path(0).path("finishReason").asText("");
        }
        if ("MAX_TOKENS".equals(reason)) {
            return "O modelo gastou o limite de tokens pensando e não devolveu texto.";
        }
        return "A resposta não veio no formato esperado.";
    }

    private void limit(UUID userId) {
        Instant cutoff = Instant.now().minus(Duration.ofHours(1));
        Deque<Instant> deque = calls.computeIfAbsent(userId, ignored -> new ArrayDeque<>());
        synchronized (deque) {
            while (!deque.isEmpty() && deque.peekFirst().isBefore(cutoff)) {
                deque.removeFirst();
            }
            if (deque.size() >= HOURLY_LIMIT) {
                throw new ApiException(429, "QUOTA", "O limite local de chamadas desta hora foi atingido.");
            }
            deque.addLast(Instant.now());
        }
    }

    private void record(UUID userId, String purpose, String status, long started, int input, int output) {
        AiInvocation row = new AiInvocation();
        row.setUserId(userId);
        row.setPurpose(purpose);
        row.setModel(activeModel == null ? model : activeModel);
        row.setStatus(status);
        row.setLatencyMs((int) ((System.nanoTime() - started) / 1_000_000));
        row.setInputChars(input);
        row.setOutputChars(output);
        invocations.save(row);
    }
}
