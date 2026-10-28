package com.curriculo.inteligente.user;

import com.curriculo.inteligente.candidate.CandidateProfile;
import com.curriculo.inteligente.candidate.ProfileRepository;
import com.curriculo.inteligente.common.ApiException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final AppUserRepository users;
    private final ProfileRepository profiles;
    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;
    private final SecurityContextRepository securityContexts = new HttpSessionSecurityContextRepository();

    public AuthService(AppUserRepository users, ProfileRepository profiles, AuthenticationManager authenticationManager,
                       PasswordEncoder passwordEncoder) {
        this.users = users;
        this.profiles = profiles;
        this.authenticationManager = authenticationManager;
        this.passwordEncoder = passwordEncoder;
    }

    public AuthController.SessionResponse session() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        boolean authenticated = authentication != null
                && authentication.isAuthenticated()
                && authentication.getName() != null
                && users.findByEmailIgnoreCase(authentication.getName()).isPresent();
        String email = authenticated ? authentication.getName() : null;
        return new AuthController.SessionResponse(authenticated, users.count() == 0, email);
    }

    @Transactional
    public AuthController.SessionResponse bootstrap(AuthController.BootstrapRequest request, HttpServletRequest http, HttpServletResponse response) {
        if (users.count() > 0) {
            throw new ApiException(409, "CONFLICT", "A conta local já foi criada.");
        }
        AppUser user = new AppUser();
        user.setEmail(request.email().trim());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        users.save(user);
        CandidateProfile profile = new CandidateProfile();
        profile.setUserId(user.getId());
        profile.setFullName(request.fullName().trim());
        profiles.save(profile);
        return login(new AuthController.LoginRequest(request.email(), request.password()), http, response);
    }

    public AuthController.SessionResponse login(AuthController.LoginRequest request, HttpServletRequest http, HttpServletResponse response) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email().trim(), request.password()));
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);
        securityContexts.saveContext(context, http, response);
        return new AuthController.SessionResponse(true, false, authentication.getName());
    }

    public void logout(HttpServletRequest request) {
        SecurityContextHolder.clearContext();
        if (request.getSession(false) != null) {
            request.getSession(false).invalidate();
        }
    }
}
