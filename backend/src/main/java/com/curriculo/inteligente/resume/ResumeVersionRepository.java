package com.curriculo.inteligente.resume;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ResumeVersionRepository extends JpaRepository<ResumeVersion, UUID> {
    Optional<ResumeVersion> findFirstByResumeIdOrderByVersionNumberDesc(UUID resumeId);
    int countByResumeId(UUID resumeId);
}
