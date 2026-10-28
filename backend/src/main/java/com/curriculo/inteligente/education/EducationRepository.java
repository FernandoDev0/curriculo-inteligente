package com.curriculo.inteligente.education;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface EducationRepository extends JpaRepository<Education, UUID> {
    List<Education> findByUserIdOrderBySortOrderAsc(UUID userId);
    Optional<Education> findByIdAndUserId(UUID id, UUID userId);
}
