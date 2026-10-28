package com.curriculo.inteligente.candidate;

import com.curriculo.inteligente.security.CurrentUser;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;

@RestController
@RequestMapping("/api/v1/profile")
public class ProfileController {

    private final ProfileService profiles;
    private final CurrentUser currentUser;

    public ProfileController(ProfileService profiles, CurrentUser currentUser) {
        this.profiles = profiles;
        this.currentUser = currentUser;
    }

    @GetMapping
    public ProfileResponse get() {
        return profiles.get(currentUser.requireId());
    }

    @PutMapping
    public ProfileResponse update(@RequestBody ProfileResponse request) {
        return profiles.update(currentUser.requireId(), request);
    }

    @PostMapping("/consent")
    public ProfileResponse consent() {
        return profiles.consent(currentUser.requireId());
    }

    @PostMapping(value = "/photo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ProfileResponse photo(@RequestPart("file") MultipartFile file) {
        return profiles.savePhoto(currentUser.requireId(), file);
    }

    @DeleteMapping("/photo")
    public ProfileResponse deletePhoto() {
        return profiles.deletePhoto(currentUser.requireId());
    }

    @PostMapping("/photo/approve")
    public ProfileResponse approvePhoto() {
        return profiles.approvePhoto(currentUser.requireId());
    }

    @GetMapping("/photo")
    public ResponseEntity<byte[]> download() {
        ProfileService.StoredPhoto photo = profiles.readPhoto(currentUser.requireId());
        return ResponseEntity.ok().contentType(MediaType.IMAGE_JPEG).body(photo.bytes());
    }

    @PostMapping("/photo/edit")
    public ProfileResponse edit() {
        return profiles.editPhoto(currentUser.requireId());
    }

    public record ProfileResponse(
            String fullName,
            String targetRole,
            String currentProfession,
            String city,
            String state,
            String professionalEmail,
            String phone,
            String linkedinUrl,
            String githubUrl,
            String portfolioUrl,
            String otherProfessionalLinks,
            String workMode,
            String targetOpportunity,
            boolean includeAge,
            Integer age,
            boolean careerChange,
            String desiredProfession,
            String careerChangeReason,
            boolean photoApproved,
            boolean hasPhoto,
            Instant aiConsentAt) {
    }
}
