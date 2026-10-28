package com.curriculo.inteligente.skill;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface LanguageRepository extends JpaRepository<LanguageSkill, UUID> {
    List<LanguageSkill> findByUserId(UUID userId);
    Optional<LanguageSkill> findByIdAndUserId(UUID id, UUID userId);
}
