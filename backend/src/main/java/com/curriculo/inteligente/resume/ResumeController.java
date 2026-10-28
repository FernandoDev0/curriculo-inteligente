package com.curriculo.inteligente.resume;

import com.curriculo.inteligente.common.PageResponse;
import com.curriculo.inteligente.job.JobPosting;
import com.curriculo.inteligente.security.CurrentUser;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
public class ResumeController {

    private final ResumeService resumes;
    private final CurrentUser currentUser;

    public ResumeController(ResumeService resumes, CurrentUser currentUser) {
        this.resumes = resumes;
        this.currentUser = currentUser;
    }

    @PostMapping("/resumes")
    public ResumeService.ResumeDetail create(@RequestBody CreateRequest request) {
        return resumes.create(currentUser.requireId(), request.title());
    }

    @GetMapping("/resumes")
    public PageResponse<ResumeService.ResumeDetail> list(@RequestParam(defaultValue = "0") int page,
                                                         @RequestParam(defaultValue = "20") int size) {
        return resumes.list(currentUser.requireId(), PageRequest.of(page, Math.min(size, 50)));
    }

    @GetMapping("/resumes/{id}")
    public ResumeService.ResumeDetail get(@PathVariable UUID id) {
        return resumes.get(currentUser.requireId(), id);
    }

    @PutMapping("/resumes/{id}")
    public ResumeService.ResumeDetail update(@PathVariable UUID id, @RequestBody ResumeService.ResumeWrite write) {
        return resumes.update(currentUser.requireId(), id, write);
    }

    @PostMapping("/resumes/{id}/summary")
    public ResumeService.TextRevision summary(@PathVariable UUID id) {
        return resumes.regenerateSummary(currentUser.requireId(), id);
    }

    @PostMapping("/resumes/{id}/review")
    public ResumeService.TextRevision review(@PathVariable UUID id, @RequestBody ReviewRequest request) {
        return resumes.review(currentUser.requireId(), id, request.section(), request.experienceId(), request.text());
    }

    @PostMapping("/resumes/{id}/approve")
    public ResumeService.ResumeDetail approve(@PathVariable UUID id) {
        return resumes.approve(currentUser.requireId(), id);
    }

    @PostMapping("/resumes/{id}/preview/pdf")
    public ResponseEntity<byte[]> preview(@PathVariable UUID id) {
        return resumes.preview(currentUser.requireId(), id);
    }

    @PostMapping("/resumes/{id}/export/pdf")
    public ResponseEntity<byte[]> export(@PathVariable UUID id) {
        return resumes.export(currentUser.requireId(), id);
    }

    @PostMapping("/jobs")
    public JobPosting createJob(@Valid @RequestBody JobRequest request) {
        return resumes.createJob(currentUser.requireId(), request.title(), request.company(), request.description());
    }

    @GetMapping("/jobs/{id}")
    public JobPosting job(@PathVariable UUID id) {
        return resumes.job(currentUser.requireId(), id);
    }

    @PostMapping("/resumes/{id}/job-match")
    public ResumeService.JobMatchResponse match(@PathVariable UUID id, @RequestBody MatchRequest request) {
        return resumes.match(currentUser.requireId(), id, request.jobId());
    }

    public record CreateRequest(String title) {
    }

    public record ReviewRequest(String section, UUID experienceId, String text) {
    }

    public record JobRequest(@NotBlank String title, String company, @NotBlank String description) {
    }

    public record MatchRequest(UUID jobId) {
    }
}
