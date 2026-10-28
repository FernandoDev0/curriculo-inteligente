package com.curriculo.inteligente.education;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CertificationRepository extends JpaRepository<Certification, UUID> {
    List<Certification> findByUserIdOrderBySortOrderAsc(UUID userId);
    Optional<Certification> findByIdAndUserId(UUID id, UUID userId);
}
