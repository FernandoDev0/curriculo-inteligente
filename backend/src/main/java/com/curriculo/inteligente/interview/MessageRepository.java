package com.curriculo.inteligente.interview;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MessageRepository extends JpaRepository<InterviewMessage, UUID> {
    List<InterviewMessage> findByInterviewIdOrderByCreatedAtAsc(UUID interviewId);
    Optional<InterviewMessage> findByIdAndInterviewId(UUID id, UUID interviewId);
    void deleteByInterviewIdAndCreatedAtAfter(UUID interviewId, java.time.Instant createdAt);
}
