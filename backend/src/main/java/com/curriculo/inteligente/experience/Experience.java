package com.curriculo.inteligente.experience;

import com.curriculo.inteligente.common.Timestamps;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.UUID;

@Entity
@Table(name = "experience")
public class Experience extends Timestamps {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID userId;

    @Column(nullable = false, length = 160)
    private String company;

    @Column(nullable = false, length = 160)
    private String roleTitle;

    @Column(nullable = false)
    private int startYear;

    private Integer startMonth;
    private Integer endYear;
    private Integer endMonth;

    @Column(nullable = false)
    private boolean currentJob;

    private String projectLabel;

    @Column(columnDefinition = "text")
    private String responsibilities;

    @Column(columnDefinition = "text")
    private String tools;

    @Column(columnDefinition = "text")
    private String results;

    @Column(columnDefinition = "text")
    private String notes;

    @Column(nullable = false)
    private int sortOrder;

    @Column(nullable = false, length = 20)
    private String detailPreference = "AUTO";

    public UUID getId() { return id; }
    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }
    public String getCompany() { return company; }
    public void setCompany(String company) { this.company = company; }
    public String getRoleTitle() { return roleTitle; }
    public void setRoleTitle(String roleTitle) { this.roleTitle = roleTitle; }
    public int getStartYear() { return startYear; }
    public void setStartYear(int startYear) { this.startYear = startYear; }
    public Integer getStartMonth() { return startMonth; }
    public void setStartMonth(Integer startMonth) { this.startMonth = startMonth; }
    public Integer getEndYear() { return endYear; }
    public void setEndYear(Integer endYear) { this.endYear = endYear; }
    public Integer getEndMonth() { return endMonth; }
    public void setEndMonth(Integer endMonth) { this.endMonth = endMonth; }
    public boolean isCurrentJob() { return currentJob; }
    public void setCurrentJob(boolean currentJob) { this.currentJob = currentJob; }
    public String getProjectLabel() { return projectLabel; }
    public void setProjectLabel(String projectLabel) { this.projectLabel = projectLabel; }
    public String getResponsibilities() { return responsibilities; }
    public void setResponsibilities(String responsibilities) { this.responsibilities = responsibilities; }
    public String getTools() { return tools; }
    public void setTools(String tools) { this.tools = tools; }
    public String getResults() { return results; }
    public void setResults(String results) { this.results = results; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    public int getSortOrder() { return sortOrder; }
    public void setSortOrder(int sortOrder) { this.sortOrder = sortOrder; }
    public String getDetailPreference() { return detailPreference; }
    public void setDetailPreference(String detailPreference) { this.detailPreference = detailPreference; }
}
