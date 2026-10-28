package com.curriculo.inteligente.interview;

import com.curriculo.inteligente.common.PageResponse;
import com.curriculo.inteligente.resume.ResumeService;
import com.curriculo.inteligente.security.CurrentUser;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.DeleteMapping;
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
@RequestMapping("/api/v1/interviews")
public class InterviewController {

    private final InterviewService interviews;
    private final ResumeService resumes;
    private final CurrentUser currentUser;

    public InterviewController(InterviewService interviews, ResumeService resumes, CurrentUser currentUser) {
        this.interviews = interviews;
        this.resumes = resumes;
        this.currentUser = currentUser;
    }

    @PostMapping
    public InterviewService.InterviewView start() {
        return interviews.start(currentUser.requireId());
    }

    @GetMapping
    public PageResponse<InterviewService.InterviewView> list(@RequestParam(defaultValue = "0") int page,
                                                             @RequestParam(defaultValue = "20") int size) {
        return interviews.list(currentUser.requireId(), PageRequest.of(page, Math.min(size, 50)));
    }

    @GetMapping("/{id}")
    public InterviewService.InterviewView get(@PathVariable UUID id) {
        return interviews.get(currentUser.requireId(), id);
    }

    @PostMapping("/{id}/messages")
    public InterviewService.InterviewView answer(@PathVariable UUID id, @RequestBody AnswerRequest request) {
        return interviews.answer(currentUser.requireId(), id, request.content(), request.skip());
    }

    @PutMapping("/{id}/messages/{messageId}")
    public InterviewService.InterviewView correct(@PathVariable UUID id, @PathVariable UUID messageId, @RequestBody AnswerRequest request) {
        return interviews.correct(currentUser.requireId(), id, messageId, request.content());
    }

    @PostMapping("/{id}/pause")
    public InterviewService.InterviewView pause(@PathVariable UUID id) {
        return interviews.pause(currentUser.requireId(), id);
    }

    @PostMapping("/{id}/resume")
    public InterviewService.InterviewView resume(@PathVariable UUID id) {
        return interviews.resume(currentUser.requireId(), id);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable UUID id) {
        interviews.delete(currentUser.requireId(), id);
    }

    @PostMapping("/{id}/resume-draft")
    public ResumeService.ResumeDetail draft(@PathVariable UUID id) {
        return resumes.draftFromInterview(currentUser.requireId(), id);
    }

    @PostMapping("/{id}/improve")
    public ResumeService.Improvement improve(@PathVariable UUID id) {
        return resumes.improveFromInterview(currentUser.requireId(), id);
    }

    public record AnswerRequest(String content, boolean skip) {
    }
}
