package com.curriculo.inteligente.candidate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "candidate_profile")
public class CandidateProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true)
    private UUID userId;

    @Column(nullable = false, length = 160)
    private String fullName = "";

    @Column(nullable = false, length = 160)
    private String targetRole = "";

    private String currentProfession;
    private String city;
    private String state;
    private String professionalEmail;
    private String phone;
    private String linkedinUrl;
    private String githubUrl;
    private String portfolioUrl;
    private String otherProfessionalLinks;
    private String workMode;
    private String targetOpportunity;

    @Column(nullable = false)
    private boolean includeAge;

    private Integer age;

    @Column(nullable = false)
    private boolean careerChange;

    private String desiredProfession;
    private String careerChangeReason;
    private String photoPath;

    @Column(nullable = false)
    private boolean photoApproved;

    private Instant aiConsentAt;

    @Column(nullable = false)
    private Instant updatedAt = Instant.now();

    public UUID getId() { return id; }
    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getTargetRole() { return targetRole; }
    public void setTargetRole(String targetRole) { this.targetRole = targetRole; }
    public String getCurrentProfession() { return currentProfession; }
    public void setCurrentProfession(String currentProfession) { this.currentProfession = currentProfession; }
    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }
    public String getState() { return state; }
    public void setState(String state) { this.state = state; }
    public String getProfessionalEmail() { return professionalEmail; }
    public void setProfessionalEmail(String professionalEmail) { this.professionalEmail = professionalEmail; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getLinkedinUrl() { return linkedinUrl; }
    public void setLinkedinUrl(String linkedinUrl) { this.linkedinUrl = linkedinUrl; }
    public String getGithubUrl() { return githubUrl; }
    public void setGithubUrl(String githubUrl) { this.githubUrl = githubUrl; }
    public String getPortfolioUrl() { return portfolioUrl; }
    public void setPortfolioUrl(String portfolioUrl) { this.portfolioUrl = portfolioUrl; }
    public String getOtherProfessionalLinks() { return otherProfessionalLinks; }
    public void setOtherProfessionalLinks(String otherProfessionalLinks) { this.otherProfessionalLinks = otherProfessionalLinks; }
    public String getWorkMode() { return workMode; }
    public void setWorkMode(String workMode) { this.workMode = workMode; }
    public String getTargetOpportunity() { return targetOpportunity; }
    public void setTargetOpportunity(String targetOpportunity) { this.targetOpportunity = targetOpportunity; }
    public boolean isIncludeAge() { return includeAge; }
    public void setIncludeAge(boolean includeAge) { this.includeAge = includeAge; }
    public Integer getAge() { return age; }
    public void setAge(Integer age) { this.age = age; }
    public boolean isCareerChange() { return careerChange; }
    public void setCareerChange(boolean careerChange) { this.careerChange = careerChange; }
    public String getDesiredProfession() { return desiredProfession; }
    public void setDesiredProfession(String desiredProfession) { this.desiredProfession = desiredProfession; }
    public String getCareerChangeReason() { return careerChangeReason; }
    public void setCareerChangeReason(String careerChangeReason) { this.careerChangeReason = careerChangeReason; }
    public String getPhotoPath() { return photoPath; }
    public void setPhotoPath(String photoPath) { this.photoPath = photoPath; }
    public boolean isPhotoApproved() { return photoApproved; }
    public void setPhotoApproved(boolean photoApproved) { this.photoApproved = photoApproved; }
    public Instant getAiConsentAt() { return aiConsentAt; }
    public void setAiConsentAt(Instant aiConsentAt) { this.aiConsentAt = aiConsentAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void touch() { this.updatedAt = Instant.now(); }
}
