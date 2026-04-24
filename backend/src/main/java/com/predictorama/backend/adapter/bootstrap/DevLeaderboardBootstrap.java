package com.predictorama.backend.adapter.bootstrap;

import com.predictorama.backend.config.DevLeaderboardBootstrapProperties;
import com.predictorama.backend.domain.entity.GroupMember;
import com.predictorama.backend.domain.entity.Match;
import com.predictorama.backend.domain.entity.Role;
import com.predictorama.backend.domain.entity.Score;
import com.predictorama.backend.domain.entity.User;
import com.predictorama.backend.domain.entity.Winner;
import com.predictorama.backend.domain.port.persistence.GroupMemberRepositoryPort;
import com.predictorama.backend.domain.port.persistence.GroupTournamentRepositoryPort;
import com.predictorama.backend.domain.port.persistence.UserRepositoryPort;
import com.predictorama.backend.domain.service.PredictionFixtureImportService;
import com.predictorama.backend.domain.service.PredictionScoringService;
import com.predictorama.backend.domain.service.PredictionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
@Component
@RequiredArgsConstructor
public class DevLeaderboardBootstrap {

    private static final String ALICE = "alice";
    private static final String BOB = "bob";
    private static final String CAROL = "carol";

    private final DevLeaderboardBootstrapProperties properties;
    private final PredictionFixtureImportService predictionFixtureImportService;
    private final PredictionService predictionService;
    private final PredictionScoringService predictionScoringService;
    private final UserRepositoryPort userRepository;
    private final GroupMemberRepositoryPort groupMemberRepository;
    private final GroupTournamentRepositoryPort groupTournamentRepository;

    private final AtomicBoolean hasRun = new AtomicBoolean(false);

    @Scheduled(
            initialDelayString = "#{@devLeaderboardBootstrapProperties.initialDelay.toMillis()}",
            fixedDelay = Long.MAX_VALUE
    )
    public void bootstrap() {
        if (!properties.isEnabled()) {
            log.debug("Dev leaderboard bootstrap is disabled");
            return;
        }

        if (!hasRun.compareAndSet(false, true)) {
            return;
        }

        try {
            runBootstrap();
        } catch (Exception e) {
            log.error("Dev leaderboard bootstrap failed", e);
        }
    }

    private void runBootstrap() {
        log.info(
                "Starting dev leaderboard bootstrap competition={} season={} groupId={}",
                properties.getCompetition(),
                properties.getSeason(),
                properties.getGroupId()
        );

        List<Match> importedMatches = predictionFixtureImportService
                .importFinishedMatchesForSeason(properties.getCompetition(), properties.getSeason()).stream()
                .sorted(Comparator
                        .comparing(Match::getKickoffTime, Comparator.nullsLast(Comparator.naturalOrder()))
                        .thenComparing(Match::getExternalId, Comparator.nullsLast(String::compareTo)))
                .toList();

        if (importedMatches.isEmpty()) {
            log.warn(
                    "Dev leaderboard bootstrap skipped prediction seeding because no finished matches were imported"
            );
            return;
        }

        ensureGroupMembers();
        ensureTournamentLink(importedMatches.get(0).getTournamentId());

        seedPredictions(importedMatches);
        importedMatches.forEach(match -> predictionScoringService.distributePredictionScores(match.getId()));

        log.info(
                "Finished dev leaderboard bootstrap seededMatches={} competition={} season={}",
                importedMatches.size(),
                properties.getCompetition(),
                properties.getSeason()
        );
    }

    private void ensureGroupMembers() {
        ensureMembership(requireUser(ALICE).getId(), Role.USER);
        ensureMembership(requireUser(BOB).getId(), Role.ADMIN);
        ensureMembership(requireUser(CAROL).getId(), Role.USER);
    }

    private void ensureMembership(UUID userId, Role role) {
        UUID groupId = properties.getGroupId();
        Optional<GroupMember> existingMembership = groupMemberRepository.findByGroupIdAndUserId(groupId, userId);
        if (existingMembership.isPresent()) {
            return;
        }

        groupMemberRepository.save(GroupMember.builder()
                .id(UUID.randomUUID())
                .userId(userId)
                .groupId(groupId)
                .memberRole(role)
                .status(GroupMember.MemberStatus.ACTIVE)
                .build());
    }

    private void ensureTournamentLink(UUID tournamentId) {
        if (!groupTournamentRepository.existsByGroupIdAndTournamentId(properties.getGroupId(), tournamentId)) {
            groupTournamentRepository.save(properties.getGroupId(), tournamentId);
        }
    }

    private void seedPredictions(List<Match> matches) {
        User bob = requireUser(BOB);
        User alice = requireUser(ALICE);
        User carol = requireUser(CAROL);

        matches.forEach(match -> {
            savePrediction(bob.getId(), match);
            savePrediction(alice.getId(), match);
            savePrediction(carol.getId(), match);
        });
    }

    private void savePrediction(UUID userId, Match match) {
        Score predictedScore = randomScore();

        predictionService.savePrediction(
                userId,
                properties.getGroupId(),
                match.getId(),
                predictedScore.getHomeScore(),
                predictedScore.getAwayScore(),
                deriveWinner(predictedScore)
        );
    }

    private Score score(int homeScore, int awayScore) {
        return Score.builder()
                .homeScore(homeScore)
                .awayScore(awayScore)
                .scoreType(Score.ScoreType.NORMAL_TIME)
                .build();
    }

    private Score randomScore() {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        return score(random.nextInt(6), random.nextInt(6));
    }

    private Winner deriveWinner(Score score) {
        if (score.getHomeScore() > score.getAwayScore()) {
            return Winner.HOME;
        }
        if (score.getAwayScore() > score.getHomeScore()) {
            return Winner.AWAY;
        }
        return Winner.DRAW;
    }

    private User requireUser(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalStateException("Missing seeded dev user: " + username));
    }
}
