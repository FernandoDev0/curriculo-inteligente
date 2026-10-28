package com.curriculo.inteligente.experience;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ExperienceRepository extends JpaRepository<Experience, UUID> {
    List<Experience> findByUserIdOrderBySortOrderAsc(UUID userId);
    Optional<Experience> findByIdAndUserId(UUID id, UUID userId);
    void deleteByUserIdAndNotes(UUID userId, String notes);
}
