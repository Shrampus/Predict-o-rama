package com.predictorama.backend.domain.service;

import com.predictorama.backend.domain.entity.Group;
import com.predictorama.backend.domain.entity.Match;
import com.predictorama.backend.domain.entity.Tournament;
import com.predictorama.backend.domain.port.persistence.GroupMemberRepositoryPort;
import com.predictorama.backend.domain.port.persistence.GroupRepositoryPort;
import com.predictorama.backend.domain.port.persistence.GroupTournamentRepositoryPort;
import com.predictorama.backend.domain.port.persistence.MatchRepositoryPort;
import com.predictorama.backend.domain.port.persistence.PredictionRepositoryPort;
import com.predictorama.backend.domain.port.persistence.TournamentRepositoryPort;
import com.predictorama.backend.domain.service.result.UpcomingMatchResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserUpcomingMatchQueryService {

    private final GroupMemberRepositoryPort groupMemberRepositoryPort;
    private final GroupRepositoryPort groupRepositoryPort;
    private final GroupTournamentRepositoryPort groupTournamentRepositoryPort;
    private final MatchRepositoryPort matchRepositoryPort;
    private final TournamentRepositoryPort tournamentRepositoryPort;
    private final PredictionRepositoryPort predictionRepositoryPort;
    private final CompetitionCatalog competitionCatalog;

    public List<UpcomingMatchResult> getUpcomingMatches(UUID userId) {
        List<Group> userGroups = groupMemberRepositoryPort.findByUserId(userId).stream()
                .map(member -> groupRepositoryPort.findById(member.getGroupId()).orElse(null))
                .filter(Objects::nonNull)
                .toList();

        Map<UUID, Set<UUID>> tournamentsByGroup = userGroups.stream()
                .collect(Collectors.toMap(
                        Group::getId,
                        group -> new HashSet<>(groupTournamentRepositoryPort.findTournamentIdsByGroupId(group.getId()))
                ));

        Set<String> predictedKeys = predictionRepositoryPort.findByUserId(userId).stream()
                .map(p -> p.getMatchId() + ":" + p.getGroupId())
                .collect(Collectors.toSet());

        Instant now = Instant.now();
        Instant end = now.plus(28, ChronoUnit.DAYS);

        List<Match> matches = matchRepositoryPort.findByKickoffTimeBetween(now, end);

        Set<UUID> tournamentIds = matches.stream().map(Match::getTournamentId).collect(Collectors.toSet());
        Map<UUID, Tournament> tournamentsById = tournamentRepositoryPort.findAllById(tournamentIds).stream()
                .collect(Collectors.toMap(Tournament::getId, t -> t));

        return matches.stream()
                .map(match -> {
                    List<Group> relevantGroups = userGroups.stream()
                            .filter(group -> tournamentsByGroup
                                    .getOrDefault(group.getId(), Set.of())
                                    .contains(match.getTournamentId()))
                            .toList();

                    Set<UUID> groupIdsWithPrediction = relevantGroups.stream()
                            .filter(g -> predictedKeys.contains(match.getId() + ":" + g.getId()))
                            .map(Group::getId)
                            .collect(Collectors.toSet());

                    Tournament tournament = tournamentsById.get(match.getTournamentId());
                    String competitionCode = tournament != null
                            ? competitionCatalog.toCompetitionCode(tournament.getName())
                            : null;
                    String tournamentName = tournament != null ? tournament.getName() : null;

                    return UpcomingMatchResult.builder()
                            .match(match)
                            .competitionCode(competitionCode)
                            .tournamentName(tournamentName)
                            .userGroups(relevantGroups)
                            .groupIdsWithPrediction(groupIdsWithPrediction)
                            .build();
                })
                .toList();
    }
}
