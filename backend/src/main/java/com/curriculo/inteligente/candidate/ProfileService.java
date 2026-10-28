package com.curriculo.inteligente.candidate;

import com.curriculo.inteligente.ai.LanguageModel;
import com.curriculo.inteligente.common.ApiException;
import com.curriculo.inteligente.user.AppUser;
import com.curriculo.inteligente.user.AppUserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.Locale;
import java.util.UUID;

@Service
public class ProfileService {

    private final ProfileRepository profiles;
    private final AppUserRepository users;
    private final LanguageModel model;

    public ProfileService(ProfileRepository profiles, AppUserRepository users, LanguageModel model) {
        this.profiles = profiles;
        this.users = users;
        this.model = model;
    }

    public ProfileController.ProfileResponse get(UUID userId) {
        return toResponse(load(userId));
    }

    @Transactional
    public ProfileController.ProfileResponse update(UUID userId, ProfileController.ProfileResponse request) {
        CandidateProfile profile = load(userId);
        apply(profile, request);
        profile.touch();
        return toResponse(profiles.save(profile));
    }

    @Transactional
    public ProfileController.ProfileResponse consent(UUID userId) {
        CandidateProfile profile = load(userId);
        profile.setAiConsentAt(Instant.now());
        profile.touch();
        return toResponse(profiles.save(profile));
    }

    @Transactional
    public ProfileController.ProfileResponse savePhoto(UUID userId, MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ApiException(400, "VALIDATION", "Envie uma foto.");
        }
        if (file.getSize() > 5_000_000) {
            throw new ApiException(413, "PAYLOAD_TOO_LARGE", "A foto passa de 5 MB.");
        }
        byte[] jpeg = reencode(file);
        CandidateProfile profile = load(userId);
        deleteQuiet(profile.getPhotoPath());
        Path path = Path.of(System.getProperty("user.home"), ".curriculo-inteligente", "photos", userId.toString(), UUID.randomUUID() + ".jpg");
        try {
            Files.createDirectories(path.getParent());
            Files.write(path, jpeg);
        } catch (IOException ex) {
            throw new ApiException(500, "UNAVAILABLE", "Não foi possível guardar a foto.");
        }
        profile.setPhotoPath(path.toString());
        profile.setPhotoApproved(false);
        profile.touch();
        return toResponse(profiles.save(profile));
    }

    @Transactional
    public ProfileController.ProfileResponse deletePhoto(UUID userId) {
        CandidateProfile profile = load(userId);
        deleteQuiet(profile.getPhotoPath());
        profile.setPhotoPath(null);
        profile.setPhotoApproved(false);
        profile.touch();
        return toResponse(profiles.save(profile));
    }

    @Transactional
    public ProfileController.ProfileResponse approvePhoto(UUID userId) {
        CandidateProfile profile = load(userId);
        if (profile.getPhotoPath() == null) {
            throw new ApiException(409, "CONFLICT", "Não há foto para aprovar.");
        }
        profile.setPhotoApproved(true);
        profile.touch();
        return toResponse(profiles.save(profile));
    }

    public StoredPhoto readPhoto(UUID userId) {
        CandidateProfile profile = load(userId);
        if (profile.getPhotoPath() == null) {
            throw new ApiException(404, "NOT_FOUND", "Não há foto.");
        }
        try {
            return new StoredPhoto(Files.readAllBytes(Path.of(profile.getPhotoPath())));
        } catch (IOException ex) {
            throw new ApiException(404, "NOT_FOUND", "A foto não está mais no disco.");
        }
    }

    public ProfileController.ProfileResponse editPhoto(UUID userId) {
        if (!model.imageEditingAvailable()) {
            throw new ApiException(409, "AI_UNAVAILABLE",
                    "O modelo configurado não oferece edição de imagem confirmada. A foto original foi mantida.");
        }
        throw new ApiException(409, "AI_UNAVAILABLE",
                "A edição automática permanece desligada para não alterar a identidade da pessoa. Use a foto original.");
    }

    public CandidateProfile load(UUID userId) {
        return profiles.findByUserId(userId).orElseGet(() -> {
            AppUser user = users.findById(userId).orElseThrow(() -> new ApiException(401, "UNAUTHENTICATED", "Entre para continuar."));
            CandidateProfile created = new CandidateProfile();
            created.setUserId(user.getId());
            return profiles.save(created);
        });
    }

    public void apply(CandidateProfile profile, ProfileController.ProfileResponse request) {
        profile.setFullName(clean(request.fullName()));
        profile.setTargetRole(clean(request.targetRole()));
        profile.setCurrentProfession(emptyToNull(request.currentProfession()));
        profile.setCity(emptyToNull(request.city()));
        profile.setState(emptyToNull(request.state()));
        profile.setProfessionalEmail(emptyToNull(request.professionalEmail()));
        profile.setPhone(emptyToNull(request.phone()));
        profile.setLinkedinUrl(emptyToNull(request.linkedinUrl()));
        profile.setGithubUrl(emptyToNull(request.githubUrl()));
        profile.setPortfolioUrl(emptyToNull(request.portfolioUrl()));
        profile.setOtherProfessionalLinks(emptyToNull(request.otherProfessionalLinks()));
        profile.setWorkMode(emptyToNull(request.workMode()));
        profile.setTargetOpportunity(emptyToNull(request.targetOpportunity()));
        profile.setIncludeAge(request.includeAge());
        profile.setAge(request.includeAge() ? request.age() : null);
        profile.setCareerChange(request.careerChange());
        profile.setDesiredProfession(emptyToNull(request.desiredProfession()));
        profile.setCareerChangeReason(emptyToNull(request.careerChangeReason()));
        if (request.age() != null && (request.age() < 14 || request.age() > 100)) {
            throw new ApiException(400, "VALIDATION", "A idade precisa estar entre 14 e 100.");
        }
    }

    public ProfileController.ProfileResponse toResponse(CandidateProfile profile) {
        return new ProfileController.ProfileResponse(
                profile.getFullName(), profile.getTargetRole(), profile.getCurrentProfession(), profile.getCity(),
                profile.getState(), profile.getProfessionalEmail(), profile.getPhone(), profile.getLinkedinUrl(),
                profile.getGithubUrl(), profile.getPortfolioUrl(), profile.getOtherProfessionalLinks(),
                profile.getWorkMode(), profile.getTargetOpportunity(), profile.isIncludeAge(), profile.getAge(),
                profile.isCareerChange(), profile.getDesiredProfession(), profile.getCareerChangeReason(),
                profile.isPhotoApproved(), profile.getPhotoPath() != null, profile.getAiConsentAt());
    }

    private byte[] reencode(MultipartFile file) {
        String name = file.getOriginalFilename() == null ? "" : file.getOriginalFilename().toLowerCase(Locale.ROOT);
        String type = file.getContentType() == null ? "" : file.getContentType();
        boolean allowed = type.contains("jpeg") || type.contains("png") || type.contains("webp")
                || name.endsWith(".jpg") || name.endsWith(".jpeg") || name.endsWith(".png") || name.endsWith(".webp");
        if (!allowed) {
            throw new ApiException(400, "VALIDATION", "Envie JPEG, PNG ou WebP.");
        }
        try {
            BufferedImage image = ImageIO.read(new ByteArrayInputStream(file.getBytes()));
            if (image == null) {
                throw new ApiException(400, "VALIDATION", "Este Java não conseguiu ler a imagem. Envie JPEG ou PNG.");
            }
            BufferedImage rgb = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_RGB);
            rgb.getGraphics().drawImage(image, 0, 0, java.awt.Color.WHITE, null);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            ImageIO.write(rgb, "jpg", out);
            return out.toByteArray();
        } catch (IOException ex) {
            throw new ApiException(400, "VALIDATION", "Não foi possível processar a imagem.");
        }
    }

    private void deleteQuiet(String path) {
        try {
            if (path != null) {
                Files.deleteIfExists(Path.of(path));
            }
        } catch (IOException ignored) {
            // A troca da foto não depende do arquivo antigo.
        }
    }

    private String clean(String value) {
        return value == null ? "" : value.trim();
    }

    private String emptyToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }

    public record StoredPhoto(byte[] bytes) {
    }
}
