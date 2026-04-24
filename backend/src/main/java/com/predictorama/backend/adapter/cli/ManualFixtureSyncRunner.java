package com.predictorama.backend.adapter.cli;

import com.predictorama.backend.config.ManualSyncProperties;
import com.predictorama.backend.domain.service.FixtureSyncResult;
import com.predictorama.backend.domain.service.FixtureSyncService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ManualFixtureSyncRunner implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(ManualFixtureSyncRunner.class);

    private final ManualSyncProperties manualSyncProperties;
    private final FixtureSyncService fixtureSyncService;
    private final ConfigurableApplicationContext applicationContext;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        if (!manualSyncProperties.isRequested()) {
            return;
        }

        try {
            validateManualSyncProperties();

            log.info(
                    "Starting manual fixture sync for competition={} dateFrom={} dateTo={}",
                    manualSyncProperties.getCompetition(),
                    manualSyncProperties.getDateFrom(),
                    manualSyncProperties.getDateTo()
            );

            FixtureSyncResult result = fixtureSyncService.syncCompetition(
                    manualSyncProperties.getCompetition(),
                    manualSyncProperties.getDateFrom(),
                    manualSyncProperties.getDateTo()
            );

            log.info(
                    "Finished manual fixture sync for competition={} importedCount={} scoredCount={} dateFrom={} dateTo={}",
                    result.competition(),
                    result.importedCount(),
                    result.scoredCount(),
                    result.dateFrom(),
                    result.dateTo()
            );

            exitIfConfigured(0);
        } catch (Exception e) {
            log.error("Manual fixture sync failed", e);
            exitIfConfigured(1);
            throw e;
        }
    }

    private void validateManualSyncProperties() {
        if (manualSyncProperties.getCompetition() == null || manualSyncProperties.getCompetition().isBlank()) {
            throw new IllegalArgumentException("manual-sync.competition is required");
        }

        if (manualSyncProperties.getDateFrom() == null) {
            throw new IllegalArgumentException("manual-sync.date-from is required");
        }

        if (manualSyncProperties.getDateTo() == null) {
            throw new IllegalArgumentException("manual-sync.date-to is required");
        }
    }

    private void exitIfConfigured(int exitCode) {
        if (!manualSyncProperties.isExitAfterRun()) {
            return;
        }

        SpringApplication.exit(applicationContext, () -> exitCode);
    }
}
