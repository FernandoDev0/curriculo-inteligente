package com.curriculo.inteligente.job;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface JobMatchRepository extends JpaRepository<JobMatch, UUID> {
}
