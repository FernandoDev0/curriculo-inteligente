package com.curriculo.inteligente.ai;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Component
public class AiRetentionJob {

    private final AiInvocationRepository invocations;

    public AiRetentionJob(AiInvocationRepository invocations) {
        this.invocations = invocations;
    }

    @Scheduled(cron = "0 30 3 * * *")
    @Transactional
    public void purge() {
        invocations.deleteByCreatedAtBefore(Instant.now().minus(90, ChronoUnit.DAYS));
    }
}
