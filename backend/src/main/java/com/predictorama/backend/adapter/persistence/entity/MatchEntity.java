package com.predictorama.backend.adapter.persistence.entity;

import com.predictorama.backend.domain.entity.Match;
import com.predictorama.backend.domain.entity.Winner;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "matches")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MatchEntity extends BaseEntity {

    @Id
    private UUID id;

    @Column(name = "tournament_id", nullable = false)
    private UUID tournamentId;

    @Column
    private String name;

    @Column(length = 500)
    private String description;

    @Column(name = "home_team_id", nullable = false)
    private UUID homeTeamId;

    @Column(name = "away_team_id", nullable = false)
    private UUID awayTeamId;

    @Enumerated(EnumType.STRING)
    @Column(name = "match_status", nullable = false)
    private Match.MatchStatus matchStatus;

    @Column(name = "kickoff_time")
    private Instant kickoffTime;

    // Nullable — only set once match is COMPLETED
    @Enumerated(EnumType.STRING)
    @Column
    private Winner winner;

    @Column(name = "external_id")
    private String externalId;
}
