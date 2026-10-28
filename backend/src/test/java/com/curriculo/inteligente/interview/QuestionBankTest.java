package com.curriculo.inteligente.interview;

import com.curriculo.inteligente.common.ApiException;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class QuestionBankTest {

    @Test
    void nameAnswerAdvancesToTheTargetRole() {
        assertEquals("target_role", QuestionBank.next("full_name", "Ana Souza", 0));
    }

    @Test
    void requiredQuestionCannotBeSkipped() {
        InterviewRepository interviews = mock(InterviewRepository.class);
        MessageRepository messages = mock(MessageRepository.class);
        var profiles = mock(com.curriculo.inteligente.candidate.ProfileRepository.class);
        var model = mock(com.curriculo.inteligente.ai.LanguageModel.class);
        var keys = mock(com.curriculo.inteligente.setup.ApiKeyStore.class);
        InterviewService service = new InterviewService(interviews, messages, profiles, model, keys);
        UUID userId = UUID.randomUUID();
        UUID interviewId = UUID.randomUUID();
        Interview interview = new Interview();
        interview.setUserId(userId);
        interview.setQuestionKey("full_name");
        interview.setStage("IDENTIFICATION");
        interview.setStatus("IN_PROGRESS");
        when(interviews.findByIdAndUserId(interviewId, userId)).thenReturn(Optional.of(interview));
        ApiException error = assertThrows(ApiException.class, () -> service.answer(userId, interviewId, "", true));
        assertEquals("VALIDATION", error.code());
    }

    @Test
    void lookupUsesTheSessionUser() {
        InterviewRepository interviews = mock(InterviewRepository.class);
        InterviewService service = new InterviewService(interviews, mock(MessageRepository.class),
                mock(com.curriculo.inteligente.candidate.ProfileRepository.class),
                mock(com.curriculo.inteligente.ai.LanguageModel.class),
                mock(com.curriculo.inteligente.setup.ApiKeyStore.class));
        UUID userId = UUID.randomUUID();
        UUID interviewId = UUID.randomUUID();
        when(interviews.findByIdAndUserId(interviewId, userId)).thenReturn(Optional.empty());
        assertThrows(ApiException.class, () -> service.get(userId, interviewId));
        verify(interviews).findByIdAndUserId(interviewId, userId);
    }

    @Test
    void optionalQuestionCanBeDescribedAsSkippable() {
        QuestionBank.Item email = QuestionBank.describe("email");
        assertEquals(false, email.required());
        assertEquals(true, email.canSkip());
        assertEquals(List.of("full_name"), List.of(QuestionBank.first().key()));
    }
}
