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
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
@Component
@Profile("dev")
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

        if (importedMatches.size() < properties.getSeededMatchCount()) {
            log.warn(
                    "Dev leaderboard bootstrap skipped prediction seeding because only {} matches were imported (need {})",
                    importedMatches.size(),
                    properties.getSeededMatchCount()
            );
            return;
        }

        ensureGroupMembers();
        ensureTournamentLink(importedMatches.get(0).getTournamentId());

        List<Match> seededMatches = importedMatches.subList(0, properties.getSeededMatchCount());
        seedPredictions(seededMatches);
        seededMatches.forEach(match -> predictionScoringService.distributePredictionScores(match.getId()));

        log.info(
                "Finished dev leaderboard bootstrap seededMatches={} competition={} season={}",
                seededMatches.size(),
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

        Match matchOne = matches.get(0);
        Match matchTwo = matches.get(1);
        Match matchThree = matches.get(2);

        savePrediction(bob.getId(), matchOne, PredictionPattern.EXACT);
        savePrediction(alice.getId(), matchOne, PredictionPattern.WINNER_ONLY);
        savePrediction(carol.getId(), matchOne, PredictionPattern.WRONG);

        savePrediction(bob.getId(), matchTwo, PredictionPattern.EXACT);
        savePrediction(alice.getId(), matchTwo, PredictionPattern.EXACT);
        savePrediction(carol.getId(), matchTwo, PredictionPattern.WINNER_ONLY);

        savePrediction(bob.getId(), matchThree, PredictionPattern.WRONG);
        savePrediction(alice.getId(), matchThree, PredictionPattern.WINNER_ONLY);
        savePrediction(carol.getId(), matchThree, PredictionPattern.EXACT);
    }

    private void savePrediction(UUID userId, Match match, PredictionPattern pattern) {
        Score actualScore = requireFullTimeScore(match);
        Score predictedScore = switch (pattern) {
            case EXACT -> score(actualScore.getHomeScore(), actualScore.getAwayScore());
            case WINNER_ONLY -> winnerOnlyScore(actualScore, match.getWinner());
            case WRONG -> wrongScore(match.getWinner());
        };

        predictionService.savePrediction(
                userId,
                properties.getGroupId(),
                match.getId(),
                predictedScore.getHomeScore(),
                predictedScore.getAwayScore(),
                deriveWinner(predictedScore)
        );
    }

    private Score requireFullTimeScore(Match match) {
        return match.getScores().stream()
                .filter(score -> score.getScoreType() == Score.ScoreType.FULL_TIME)
                .findFirst()
                .orElseGet(() -> match.primaryScore()
                        .orElseThrow(() -> new IllegalStateException("Missing score for match " + match.getId())));
    }

    private Score winnerOnlyScore(Score actualScore, Winner winner) {
        int homeScore = actualScore.getHomeScore();
        int awayScore = actualScore.getAwayScore();

        return switch (winner) {
            case HOME -> (homeScore - awayScore == 1)
                    ? score(homeScore + 1, awayScore)
                    : score(homeScore - 1, awayScore);
            case AWAY -> (awayScore - homeScore == 1)
                    ? score(homeScore, awayScore + 1)
                    : score(homeScore, awayScore - 1);
            case DRAW -> score(homeScore + 1, awayScore + 1);
        };
    }

    private Score wrongScore(Winner winner) {
        return switch (winner) {
            case HOME, DRAW -> score(0, 1);
            case AWAY -> score(1, 0);
        };
    }

    private Score score(int homeScore, int awayScore) {
        return Score.builder()
                .homeScore(homeScore)
                .awayScore(awayScore)
                .scoreType(Score.ScoreType.NORMAL_TIME)
                .build();
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

    private enum PredictionPattern {
        EXACT,
        WINNER_ONLY,
        WRONG
    }
}
