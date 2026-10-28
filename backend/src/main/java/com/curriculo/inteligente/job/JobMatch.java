package com.curriculo.inteligente.job;

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
@Table(name = "job_match")
public class JobMatch {
    @Id @GeneratedValue(strategy = GenerationType.UUID) private UUID id;
    @Column(nullable = false) private UUID userId;
    @Column(nullable = false) private UUID resumeId;
    @Column(nullable = false) private UUID jobId;
    @JdbcTypeCode(SqlTypes.JSON) @Column(nullable = false, columnDefinition = "jsonb") private String demonstrated;
    @JdbcTypeCode(SqlTypes.JSON) @Column(nullable = false, columnDefinition = "jsonb") private String related;
    @JdbcTypeCode(SqlTypes.JSON) @Column(nullable = false, columnDefinition = "jsonb") private String missing;
    @JdbcTypeCode(SqlTypes.JSON) @Column(nullable = false, columnDefinition = "jsonb") private String keywords;
    @Column(columnDefinition = "text") private String suggestions;
    @Column(nullable = false) private Instant createdAt = Instant.now();
    public void setUserId(UUID userId) { this.userId = userId; }
    public void setResumeId(UUID resumeId) { this.resumeId = resumeId; }
    public void setJobId(UUID jobId) { this.jobId = jobId; }
    public void setDemonstrated(String demonstrated) { this.demonstrated = demonstrated; }
    public void setRelated(String related) { this.related = related; }
    public void setMissing(String missing) { this.missing = missing; }
    public void setKeywords(String keywords) { this.keywords = keywords; }
    public void setSuggestions(String suggestions) { this.suggestions = suggestions; }
}
