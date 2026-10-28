package com.curriculo.inteligente.setup;

import com.curriculo.inteligente.security.CurrentUser;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;

@RestController
@RequestMapping("/api/v1/setup")
public class SetupController {

    private final SetupService setup;
    private final CurrentUser currentUser;

    public SetupController(SetupService setup, CurrentUser currentUser) {
        this.setup = setup;
        this.currentUser = currentUser;
    }

    @GetMapping("/status")
    public Status status() {
        return setup.status();
    }

    @PostMapping("/api-key")
    public Status saveKey(@Valid @RequestBody KeyRequest request) {
        currentUser.requireId();
        return setup.saveKey(request.apiKey());
    }

    @DeleteMapping("/api-key")
    public Status deleteKey() {
        currentUser.requireId();
        return setup.deleteKey();
    }

    @PostMapping("/test-connection")
    public Status test() {
        return setup.test(currentUser.requireId());
    }

    public record KeyRequest(@NotBlank String apiKey) {
    }

    public record Status(
            boolean keyConfigured,
            String keySource,
            String model,
            boolean imageEditingAvailable,
            String state,
            Instant lastTestAt,
            String lastTestMessage) {
    }
}
