package com.predictorama.backend.adapter.scheduler;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.predictorama.backend.domain.service.MatchResultSyncService;
import com.predictorama.backend.config.ResultSyncProperties;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class MatchResultSyncScheduler {

    private final ResultSyncProperties resultSyncProperties;
    private final MatchResultSyncService matchResultSyncService;

    @Scheduled(
            initialDelayString = "#{@resultSyncProperties.initialDelay.toMillis()}",
            fixedDelayString = "#{@resultSyncProperties.fixedDelay.toMillis()}"
    )
    public void syncMatchResults() {
        if (!resultSyncProperties.isEnabled()) {
            log.debug("Scheduled result sync is disabled");
            return;
        }

        log.info("Starting match result synchronization...");
        matchResultSyncService.syncAllCompetitions();
        log.info("Finished match result synchronization.");
    }
}
