package com.predictorama.backend.domain.service;

import java.util.Objects;
import java.util.List;
import java.util.UUID;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

import org.springframework.stereotype.Service;

import com.predictorama.backend.domain.entity.Group;
import com.predictorama.backend.domain.port.persistence.GroupMemberRepositoryPort;
import com.predictorama.backend.domain.port.persistence.GroupRepositoryPort;
import com.predictorama.backend.domain.port.persistence.MatchRepositoryPort;
import com.predictorama.backend.domain.port.persistence.TournamentRepositoryPort;
import com.predictorama.backend.domain.service.result.UpcomingMatchResult;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UpcomingMatchQueryService {

    private final GroupMemberRepositoryPort groupMemberRepositoryPort;
    private final GroupRepositoryPort groupRepositoryPort;
    private final MatchRepositoryPort matchRepositoryPort;
    private final TournamentRepositoryPort tournamentRepositoryPort;
    private final CompetitionCatalog competitionCatalog;

    public List<UpcomingMatchResult> getGenericUpcomingMatches() {
        Instant now = Instant.now();
        Instant end = now.plus(28, ChronoUnit.DAYS);

        return matchRepositoryPort.findByKickoffTimeBetween(now, end).stream()
                .map(match -> UpcomingMatchResult.builder()
                        .match(match)
                        .userGroups(List.of())
                        .build())
                .toList();
    }

    public List<UpcomingMatchResult> getUpcomingMatches(UUID userId) {
        List<Group> userGroups = groupMemberRepositoryPort.findByUserId(userId).stream()
                .map(member -> groupRepositoryPort.findById(member.getGroupId()).orElse(null))
                .filter(Objects::nonNull)
                .toList();

        Instant now = Instant.now();
        Instant end = now.plus(28, ChronoUnit.DAYS);

        return matchRepositoryPort.findByKickoffTimeBetween(now, end).stream()
                .map(match -> UpcomingMatchResult.builder()
                        .match(match)
                        .userGroups(userGroups)
                        .build())
                .toList();
    }
    
}
