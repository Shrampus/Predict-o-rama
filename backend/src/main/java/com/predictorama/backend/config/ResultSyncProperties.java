package com.predictorama.backend.config;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "result-sync")
public class ResultSyncProperties {
    private boolean enabled = false;
    private Duration initialDelay = Duration.ofSeconds(30);
    private Duration fixedDelay = Duration.ofMinutes(5);
}
