package com.curriculo.inteligente.ai;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.UUID;

public interface AiInvocationRepository extends JpaRepository<AiInvocation, UUID> {
    void deleteByCreatedAtBefore(Instant cutoff);
    void deleteByUserId(UUID userId);
}
