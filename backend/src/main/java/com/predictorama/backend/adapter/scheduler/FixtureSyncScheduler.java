package com.predictorama.backend.adapter.scheduler;

import com.predictorama.backend.config.FixtureSyncProperties;
import com.predictorama.backend.domain.service.FixtureSyncService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class FixtureSyncScheduler {

    private final FixtureSyncProperties fixtureSyncProperties;
    private final FixtureSyncService fixtureSyncService;

    @Scheduled(
            initialDelayString = "#{@fixtureSyncProperties.initialDelay.toMillis()}",
            fixedDelayString = "#{@fixtureSyncProperties.fixedDelay.toMillis()}"
    )
    public void syncNextCompetition() {
        if (!fixtureSyncProperties.isEnabled()) {
            log.debug("Scheduled fixture sync is disabled");
            return;
        }

        fixtureSyncService.syncNextCompetition();
    }
}