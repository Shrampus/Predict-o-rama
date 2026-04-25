package com.predictorama.backend.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.UUID;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "dev-leaderboard-bootstrap")
public class DevLeaderboardBootstrapProperties {

    private boolean enabled = true;
    private Duration initialDelay = Duration.ofSeconds(5);
    private String competition = "EC";
    private int season = 2024;
    private UUID groupId = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
}
