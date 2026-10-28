package com.curriculo.inteligente.interview;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "interview_message")
public class InterviewMessage {
    @Id @GeneratedValue(strategy = GenerationType.UUID) private UUID id;
    @Column(nullable = false) private UUID interviewId;
    @Column(nullable = false, length = 20) private String role;
    @Column(nullable = false, length = 40) private String stage;
    @Column(length = 80) private String questionKey;
    @Column(nullable = false, columnDefinition = "text") private String content;
    @Column(nullable = false) private boolean skipped;
    @Column(nullable = false) private Instant createdAt = Instant.now();
    public UUID getId() { return id; }
    public UUID getInterviewId() { return interviewId; }
    public void setInterviewId(UUID interviewId) { this.interviewId = interviewId; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public String getStage() { return stage; }
    public void setStage(String stage) { this.stage = stage; }
    public String getQuestionKey() { return questionKey; }
    public void setQuestionKey(String questionKey) { this.questionKey = questionKey; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public boolean isSkipped() { return skipped; }
    public void setSkipped(boolean skipped) { this.skipped = skipped; }
    public Instant getCreatedAt() { return createdAt; }
}
