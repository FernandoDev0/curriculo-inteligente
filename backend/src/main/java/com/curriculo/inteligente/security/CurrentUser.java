package com.curriculo.inteligente.security;

import com.curriculo.inteligente.common.ApiException;
import com.curriculo.inteligente.user.AppUser;
import com.curriculo.inteligente.user.AppUserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class CurrentUser {

    private final AppUserRepository users;

    public CurrentUser(AppUserRepository users) {
        this.users = users;
    }

    public AppUser require() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || authentication.getName() == null) {
            throw new ApiException(401, "UNAUTHENTICATED", "Entre para continuar.");
        }
        return users.findByEmailIgnoreCase(authentication.getName())
                .orElseThrow(() -> new ApiException(401, "UNAUTHENTICATED", "Entre para continuar."));
    }

    public UUID requireId() {
        return require().getId();
    }
}
