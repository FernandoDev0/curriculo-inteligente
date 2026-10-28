package com.curriculo.inteligente.user;

import com.curriculo.inteligente.security.CurrentUser;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
public class AuthController {

    private final AuthService auth;
    private final AccountService accounts;
    private final CurrentUser currentUser;

    public AuthController(AuthService auth, AccountService accounts, CurrentUser currentUser) {
        this.auth = auth;
        this.accounts = accounts;
        this.currentUser = currentUser;
    }

    @GetMapping("/auth/session")
    public SessionResponse session() {
        return auth.session();
    }

    @PostMapping("/auth/bootstrap")
    public SessionResponse bootstrap(@Valid @RequestBody BootstrapRequest request, HttpServletRequest http, HttpServletResponse response) {
        return auth.bootstrap(request, http, response);
    }

    @PostMapping("/auth/login")
    public SessionResponse login(@Valid @RequestBody LoginRequest request, HttpServletRequest http, HttpServletResponse response) {
        return auth.login(request, http, response);
    }

    @PostMapping("/auth/logout")
    public void logout(HttpServletRequest request) {
        auth.logout(request);
    }

    @DeleteMapping("/account")
    public void deleteAccount() {
        accounts.delete(currentUser.require());
    }

    public record BootstrapRequest(
            @Email @NotBlank String email,
            @NotBlank @Size(min = 10, max = 200) String password,
            @NotBlank @Size(max = 160) String fullName) {
    }

    public record LoginRequest(@Email @NotBlank String email, @NotBlank String password) {
    }

    public record SessionResponse(boolean authenticated, boolean needsBootstrap, String email) {
    }
}
