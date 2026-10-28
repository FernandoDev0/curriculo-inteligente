package com.curriculo.inteligente.skill;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SkillRepository extends JpaRepository<Skill, UUID> {
    List<Skill> findByUserIdOrderBySortOrderAsc(UUID userId);
    Optional<Skill> findByIdAndUserId(UUID id, UUID userId);
    Optional<Skill> findByUserIdAndCategoryAndNameIgnoreCase(UUID userId, String category, String name);
}
