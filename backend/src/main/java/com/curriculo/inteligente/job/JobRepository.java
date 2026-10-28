package com.curriculo.inteligente.job;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface JobRepository extends JpaRepository<JobPosting, UUID> {
    Optional<JobPosting> findByIdAndUserId(UUID id, UUID userId);
}
