package com.curriculo.inteligente.skill;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProjectRepository extends JpaRepository<ProjectItem, UUID> {
    List<ProjectItem> findByUserIdOrderBySortOrderAsc(UUID userId);
    Optional<ProjectItem> findByIdAndUserId(UUID id, UUID userId);
}
