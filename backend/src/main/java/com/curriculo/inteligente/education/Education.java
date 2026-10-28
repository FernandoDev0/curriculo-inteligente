package com.curriculo.inteligente.education;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.UUID;

@Entity
@Table(name = "education")
public class Education {
    @Id @GeneratedValue(strategy = GenerationType.UUID) private UUID id;
    @Column(nullable = false) private UUID userId;
    @Column(nullable = false, length = 40) private String level;
    private String course;
    private String institution;
    @Column(nullable = false, length = 20) private String status;
    private Integer startYear;
    private Integer endYear;
    @Column(nullable = false) private int sortOrder;
    public UUID getId() { return id; }
    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }
    public String getLevel() { return level; }
    public void setLevel(String level) { this.level = level; }
    public String getCourse() { return course; }
    public void setCourse(String course) { this.course = course; }
    public String getInstitution() { return institution; }
    public void setInstitution(String institution) { this.institution = institution; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Integer getStartYear() { return startYear; }
    public void setStartYear(Integer startYear) { this.startYear = startYear; }
    public Integer getEndYear() { return endYear; }
    public void setEndYear(Integer endYear) { this.endYear = endYear; }
    public int getSortOrder() { return sortOrder; }
    public void setSortOrder(int sortOrder) { this.sortOrder = sortOrder; }
}
