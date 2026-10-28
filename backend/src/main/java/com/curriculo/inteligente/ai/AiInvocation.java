package com.curriculo.inteligente.ai;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "ai_invocation")
public class AiInvocation {
    @Id @GeneratedValue(strategy = GenerationType.UUID) private UUID id;
    private UUID userId;
    @Column(nullable = false, length = 40) private String purpose;
    @Column(nullable = false, length = 80) private String model;
    @Column(nullable = false, length = 20) private String status;
    private Integer latencyMs;
    private Integer inputChars;
    private Integer outputChars;
    @Column(nullable = false) private Instant createdAt = Instant.now();

    public void setUserId(UUID userId) { this.userId = userId; }
    public void setPurpose(String purpose) { this.purpose = purpose; }
    public void setModel(String model) { this.model = model; }
    public void setStatus(String status) { this.status = status; }
    public void setLatencyMs(Integer latencyMs) { this.latencyMs = latencyMs; }
    public void setInputChars(Integer inputChars) { this.inputChars = inputChars; }
    public void setOutputChars(Integer outputChars) { this.outputChars = outputChars; }
}
