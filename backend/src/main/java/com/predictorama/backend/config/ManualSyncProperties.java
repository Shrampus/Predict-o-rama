package com.predictorama.backend.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "manual-sync")
public class ManualSyncProperties {

    private String competition;
    private LocalDate dateFrom;
    private LocalDate dateTo;
    private boolean exitAfterRun = true;

    public boolean isRequested() {
        return competition != null || dateFrom != null || dateTo != null;
    }
}
