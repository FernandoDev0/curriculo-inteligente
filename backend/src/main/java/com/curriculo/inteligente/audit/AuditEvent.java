package com.curriculo.inteligente.audit;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "audit_event")
public class AuditEvent {
    @Id @GeneratedValue(strategy = GenerationType.UUID) private UUID id;
    private UUID userId;
    @Column(nullable = false, length = 60) private String action;
    private String resourceType;
    private UUID resourceId;
    @JdbcTypeCode(SqlTypes.JSON) @Column(columnDefinition = "jsonb") private String metadata;
    @Column(nullable = false) private Instant createdAt = Instant.now();
    public void setUserId(UUID userId) { this.userId = userId; }
    public void setAction(String action) { this.action = action; }
    public void setResourceType(String resourceType) { this.resourceType = resourceType; }
    public void setResourceId(UUID resourceId) { this.resourceId = resourceId; }
}
