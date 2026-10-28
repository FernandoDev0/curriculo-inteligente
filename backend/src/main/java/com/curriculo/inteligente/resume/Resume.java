package com.curriculo.inteligente.resume;

import com.curriculo.inteligente.common.Timestamps;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "resume")
public class Resume extends Timestamps {
    @Id @GeneratedValue(strategy = GenerationType.UUID) private UUID id;
    @Column(nullable = false) private UUID userId;
    @Column(nullable = false, length = 160) private String title;
    @Column(nullable = false, length = 20) private String status = "DRAFT";
    private String targetRole;
    @Column(columnDefinition = "text") private String summaryDraft;
    @Column(columnDefinition = "text") private String summaryApproved;
    @Column(nullable = false, length = 40) private String templateCode = "REFERENCIA_A4";
    @Column(nullable = false) private boolean includePhoto;
    private Instant approvedAt;
    public UUID getId() { return id; }
    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getTargetRole() { return targetRole; }
    public void setTargetRole(String targetRole) { this.targetRole = targetRole; }
    public String getSummaryDraft() { return summaryDraft; }
    public void setSummaryDraft(String summaryDraft) { this.summaryDraft = summaryDraft; }
    public String getSummaryApproved() { return summaryApproved; }
    public void setSummaryApproved(String summaryApproved) { this.summaryApproved = summaryApproved; }
    public String getTemplateCode() { return templateCode; }
    public void setTemplateCode(String templateCode) { this.templateCode = templateCode; }
    public boolean isIncludePhoto() { return includePhoto; }
    public void setIncludePhoto(boolean includePhoto) { this.includePhoto = includePhoto; }
    public Instant getApprovedAt() { return approvedAt; }
    public void setApprovedAt(Instant approvedAt) { this.approvedAt = approvedAt; }
}
