package com.curriculo.inteligente.interview;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface InterviewRepository extends JpaRepository<Interview, UUID> {
    Page<Interview> findByUserId(UUID userId, Pageable pageable);
    Optional<Interview> findByIdAndUserId(UUID id, UUID userId);
}
