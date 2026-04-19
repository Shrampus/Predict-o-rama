package com.predictorama.backend.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "fixture-sync")
public class FixtureSyncProperties {

    /**
     * Enables or disables scheduled fixture sync entirely.
     */
    private boolean enabled = false;

    /**
     * Delay before the first scheduled sync after application startup.
     */
    private Duration initialDelay = Duration.ofSeconds(15);

    /**
     * Delay between scheduled sync executions.
     * In this design, each execution syncs exactly one competition.
     */
    private Duration fixedDelay = Duration.ofMinutes(1);

    /**
     * Configured competition codes eligible for scheduled sync.
     * Example: WC, EC, CL, PL, BL1, ...
     */
    private List<String> competitions = new ArrayList<>();
}