package com.curriculo.inteligente.user;

import com.curriculo.inteligente.ai.AiInvocationRepository;
import com.curriculo.inteligente.audit.AuditRepository;
import com.curriculo.inteligente.candidate.CandidateProfile;
import com.curriculo.inteligente.candidate.ProfileRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Service
public class AccountService {

    private final AppUserRepository users;
    private final ProfileRepository profiles;
    private final AiInvocationRepository invocations;
    private final AuditRepository audit;

    public AccountService(AppUserRepository users, ProfileRepository profiles, AiInvocationRepository invocations, AuditRepository audit) {
        this.users = users;
        this.profiles = profiles;
        this.invocations = invocations;
        this.audit = audit;
    }

    @Transactional
    public void delete(AppUser user) {
        profiles.findByUserId(user.getId()).map(CandidateProfile::getPhotoPath).ifPresent(this::deleteFile);
        invocations.deleteByUserId(user.getId());
        audit.deleteByUserId(user.getId());
        users.delete(user);
    }

    private void deleteFile(String path) {
        try {
            if (path != null) {
                Files.deleteIfExists(Path.of(path));
            }
        } catch (IOException ignored) {
            // A conta segue sendo apagada mesmo se o arquivo já não existir.
        }
    }
}
