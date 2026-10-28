package com.curriculo.inteligente.interview;

import com.curriculo.inteligente.ai.InventionGuard;
import com.curriculo.inteligente.ai.LanguageModel;
import com.curriculo.inteligente.candidate.CandidateProfile;
import com.curriculo.inteligente.candidate.ProfileRepository;
import com.curriculo.inteligente.common.ApiException;
import com.curriculo.inteligente.common.PageResponse;
import com.curriculo.inteligente.common.Text;
import com.curriculo.inteligente.setup.ApiKeyStore;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;

@Service
public class InterviewService {

    private static final Pattern YEAR = Pattern.compile("(19|20)\\d{2}");

    private final InterviewRepository interviews;
    private final MessageRepository messages;
    private final ProfileRepository profiles;
    private final LanguageModel model;
    private final ApiKeyStore keys;

    public InterviewService(InterviewRepository interviews, MessageRepository messages, ProfileRepository profiles,
                            LanguageModel model, ApiKeyStore keys) {
        this.interviews = interviews;
        this.messages = messages;
        this.profiles = profiles;
        this.model = model;
        this.keys = keys;
    }

    @Transactional
    public InterviewView start(UUID userId) {
        Interview interview = new Interview();
        interview.setUserId(userId);
        QuestionBank.Item first = QuestionBank.first();
        interview.setStage(first.stage());
        interview.setQuestionKey(first.key());
        interview.setStatus("IN_PROGRESS");
        interviews.save(interview);
        addAssistant(interview, first, null);
        return view(interview);
    }

    public PageResponse<InterviewView> list(UUID userId, Pageable pageable) {
        return PageResponse.of(interviews.findByUserId(userId, pageable), interview -> summary(interview, List.of()));
    }

    public InterviewView get(UUID userId, UUID id) {
        return view(load(userId, id));
    }

    @Transactional
    public InterviewView answer(UUID userId, UUID id, String content, boolean skip) {
        Interview interview = load(userId, id);
        if ("PAUSED".equals(interview.getStatus())) {
            throw new ApiException(409, "CONFLICT", "A entrevista está pausada.");
        }
        if ("COMPLETED".equals(interview.getStatus())) {
            throw new ApiException(409, "CONFLICT", "A entrevista já foi concluída.");
        }
        QuestionBank.Item current = QuestionBank.describe(interview.getQuestionKey());
        if (current == null) {
            throw new ApiException(409, "CONFLICT", "Não há pergunta em aberto.");
        }
        if (skip && current.required()) {
            throw new ApiException(400, "VALIDATION", "Esta pergunta é obrigatória.", Map.of("content", "Responda para continuar."));
        }
        String text = content == null ? "" : content.trim();
        if (!skip && current.required() && text.isBlank()) {
            throw new ApiException(400, "VALIDATION", "Esta pergunta é obrigatória.", Map.of("content", "Escreva uma resposta."));
        }
        if (!skip && interview.getQuestionKey().endsWith(":period") && !YEAR.matcher(text).find()) {
            throw new ApiException(400, "VALIDATION", "Informe ao menos o ano. Uma data ausente não será inventada.",
                    Map.of("content", "Exemplo: 03/2022 a 10/2024, ou 03/2022 até o momento."));
        }
        InterviewMessage user = new InterviewMessage();
        user.setInterviewId(interview.getId());
        user.setRole("USER");
        user.setStage(current.stage());
        user.setQuestionKey(current.key());
        user.setContent(skip ? "" : text);
        user.setSkipped(skip);
        messages.save(user);
        String nextKey = QuestionBank.next(current.key(), text, followups(interview.getId(), current.key()));
        String spoken = null;
        if (!skip && nextKey != null && nextKey.contains(".followup.") && keys.resolve() != null && consented(userId)) {
            spoken = adaptFollowup(userId, current.text(), text);
        }
        move(interview, nextKey, spoken);
        return view(interview);
    }

    @Transactional
    public InterviewView correct(UUID userId, UUID interviewId, UUID messageId, String content) {
        Interview interview = load(userId, interviewId);
        InterviewMessage message = messages.findByIdAndInterviewId(messageId, interviewId)
                .orElseThrow(() -> new ApiException(404, "NOT_FOUND", "Resposta não encontrada."));
        if (!"USER".equals(message.getRole())) {
            throw new ApiException(400, "VALIDATION", "Só uma resposta sua pode ser corrigida.");
        }
        message.setContent(content == null ? "" : content.trim());
        message.setSkipped(false);
        messages.save(message);
        messages.deleteByInterviewIdAndCreatedAtAfter(interviewId, message.getCreatedAt());
        String next = QuestionBank.next(message.getQuestionKey(), message.getContent(), 0);
        move(interview, next, null);
        return view(interview);
    }

    @Transactional
    public InterviewView pause(UUID userId, UUID id) {
        Interview interview = load(userId, id);
        interview.setStatus("PAUSED");
        return view(interviews.save(interview));
    }

    @Transactional
    public InterviewView resume(UUID userId, UUID id) {
        Interview interview = load(userId, id);
        if (!"COMPLETED".equals(interview.getStatus())) {
            interview.setStatus("IN_PROGRESS");
        }
        return view(interviews.save(interview));
    }

    @Transactional
    public void delete(UUID userId, UUID id) {
        Interview interview = load(userId, id);
        interviews.delete(interview);
    }

    public Interview load(UUID userId, UUID id) {
        return interviews.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ApiException(404, "NOT_FOUND", "Entrevista não encontrada."));
    }

    public List<InterviewMessage> history(UUID interviewId) {
        return messages.findByInterviewIdOrderByCreatedAtAsc(interviewId);
    }

    private void move(Interview interview, String nextKey, String spoken) {
        if (nextKey == null) {
            interview.setStatus("COMPLETED");
            interview.setQuestionKey(null);
            interview.setStage("DONE");
            interviews.save(interview);
            return;
        }
        QuestionBank.Item next = QuestionBank.describe(nextKey);
        interview.setQuestionKey(next.key());
        interview.setStage(next.stage());
        interview.setStatus("IN_PROGRESS");
        interviews.save(interview);
        addAssistant(interview, next, spoken);
    }

    private String adaptFollowup(UUID userId, String previousQuestion, String text) {
        try {
            String question = model.complete(userId, "FOLLOWUP",
                    "Faça uma única pergunta curta, em português, para concretizar o que a pessoa realmente fazia. Não invente fatos, cargos, empresas ou números. Responda só a pergunta.",
                    "Pergunta anterior: " + previousQuestion + "\nResposta: " + text,
                    false).trim();
            InventionGuard.assertFaithful(text, question);
            if (!question.contains("?") || question.length() > 400) {
                return null;
            }
            return question;
        } catch (RuntimeException ex) {
            return null;
        }
    }

    private int followups(UUID interviewId, String key) {
        String base = key.contains(".followup.") ? key.substring(0, key.indexOf(".followup.")) : key;
        return (int) messages.findByInterviewIdOrderByCreatedAtAsc(interviewId).stream()
                .filter(message -> message.getQuestionKey() != null && message.getQuestionKey().startsWith(base + ".followup."))
                .count();
    }

    private boolean consented(UUID userId) {
        return profiles.findByUserId(userId).map(CandidateProfile::getAiConsentAt).isPresent();
    }

    private void addAssistant(Interview interview, QuestionBank.Item item, String spoken) {
        InterviewMessage message = new InterviewMessage();
        message.setInterviewId(interview.getId());
        message.setRole("ASSISTANT");
        message.setStage(item.stage());
        message.setQuestionKey(item.key());
        message.setContent(spoken == null || spoken.isBlank() ? item.text() : spoken);
        message.setSkipped(false);
        messages.save(message);
    }

    private InterviewView view(Interview interview) {
        return summary(interview, messages.findByInterviewIdOrderByCreatedAtAsc(interview.getId()));
    }

    private InterviewView summary(Interview interview, List<InterviewMessage> history) {
        QuestionBank.Item current = QuestionBank.describe(interview.getQuestionKey());
        int progress = "COMPLETED".equals(interview.getStatus()) ? 100 : QuestionBank.progress(interview.getQuestionKey());
        String questionText = current == null ? null : current.text();
        if (current != null) {
            for (int i = history.size() - 1; i >= 0; i--) {
                InterviewMessage message = history.get(i);
                if ("ASSISTANT".equals(message.getRole()) && current.key().equals(message.getQuestionKey())) {
                    questionText = message.getContent();
                    break;
                }
            }
        }
        return new InterviewView(
                interview.getId(),
                interview.getStatus(),
                interview.getStage(),
                interview.getQuestionKey(),
                progress,
                current == null ? null : new InterviewView.Question(current.key(), questionText, current.required(), current.canSkip()),
                history.stream().map(message -> new InterviewView.Message(
                        message.getId(), message.getRole(), message.getStage(), message.getQuestionKey(),
                        message.getContent(), message.isSkipped(), message.getCreatedAt())).toList(),
                interview.getCreatedAt(),
                interview.getUpdatedAt());
    }

    public record InterviewView(
            UUID id,
            String status,
            String stage,
            String questionKey,
            int progressPercent,
            Question currentQuestion,
            List<Message> messages,
            Instant createdAt,
            Instant updatedAt) {
        public record Question(String key, String text, boolean required, boolean canSkip) {
        }
        public record Message(UUID id, String role, String stage, String questionKey, String content, boolean skipped, Instant createdAt) {
        }
    }
}
