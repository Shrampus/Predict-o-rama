package com.predictorama.backend.adapter.external.footballdata.mapper;

import com.predictorama.backend.adapter.external.footballdata.FootballDataMatchResponse;
import com.predictorama.backend.adapter.external.footballdata.FootballDataSeasonResponse;
import com.predictorama.backend.adapter.external.footballdata.FootballDataScoreDetailedResponse;
import com.predictorama.backend.adapter.external.footballdata.FootballDataScoreResponse;
import com.predictorama.backend.adapter.external.footballdata.FootballDataTeamResponse;
import com.predictorama.backend.domain.entity.Match;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class FootballDataMatchMapperTest {

    @Test
    void toDomainMatch_ignoresIncompleteFullTimeScore() {
        FootballDataMatchResponse response = new FootballDataMatchResponse(
                new FootballDataSeasonResponse(777, "2025-09-01", "2026-05-31"),
                123,
                "2026-04-20T20:00:00Z",
                "FINISHED",
                4,
                "REGULAR_SEASON",
                "GROUP_A",
                new FootballDataTeamResponse(1, "Home FC", "home.png"),
                new FootballDataTeamResponse(2, "Away FC", "away.png"),
                new FootballDataScoreResponse("HOME_TEAM", new FootballDataScoreDetailedResponse(null, null))
        );

        Match match = FootballDataMatchMapper.toDomainMatch(response);

        assertThat(match.getMatchStatus()).isEqualTo(Match.MatchStatus.COMPLETED);
        assertThat(match.getSeasonIdentifier()).isEqualTo("777");
        assertThat(match.getSeasonLabel()).isEqualTo("2025/26");
        assertThat(match.getRoundIdentifier()).isEqualTo("GROUP A - Matchday 4");
        assertThat(match.getGroupIdentifier()).isEqualTo("GROUP A");
        assertThat(match.getMatchdayIdentifier()).isEqualTo(4);
        assertThat(match.getScores()).isEmpty();
        assertThat(match.getWinner()).isNull();
    }

    @Test
    void toDomainMatch_mapsCompleteFullTimeScore() {
        FootballDataMatchResponse response = new FootballDataMatchResponse(
                new FootballDataSeasonResponse(777, "2025-09-01", "2026-05-31"),
                123,
                "2026-04-20T20:00:00Z",
                "FINISHED",
                4,
                "REGULAR_SEASON",
                "GROUP_A",
                new FootballDataTeamResponse(1, "Home FC", "home.png"),
                new FootballDataTeamResponse(2, "Away FC", "away.png"),
                new FootballDataScoreResponse("HOME_TEAM", new FootballDataScoreDetailedResponse(2, 1))
        );

        Match match = FootballDataMatchMapper.toDomainMatch(response);

        assertThat(match.getSeasonIdentifier()).isEqualTo("777");
        assertThat(match.getSeasonLabel()).isEqualTo("2025/26");
        assertThat(match.getRoundIdentifier()).isEqualTo("GROUP A - Matchday 4");
        assertThat(match.getGroupIdentifier()).isEqualTo("GROUP A");
        assertThat(match.getMatchdayIdentifier()).isEqualTo(4);
        assertThat(match.getScores()).hasSize(1);
        assertThat(match.getScores().getFirst().getHomeScore()).isEqualTo(2);
        assertThat(match.getScores().getFirst().getAwayScore()).isEqualTo(1);
        assertThat(match.getWinner()).isEqualTo(com.predictorama.backend.domain.entity.Winner.HOME);
    }
}
