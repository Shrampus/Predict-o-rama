package com.predictorama.backend.adapter.persistence.entity;

import com.predictorama.backend.domain.entity.Score;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "match_scores", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"match_id", "score_type"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MatchScoreEntity {

    @Id
    private UUID id;

    @Column(name = "match_id", nullable = false)
    private UUID matchId;

    @Enumerated(EnumType.STRING)
    @Column(name = "score_type", nullable = false)
    private Score.ScoreType scoreType;

    @Column(name = "home_score", nullable = false)
    private Integer homeScore;

    @Column(name = "away_score", nullable = false)
    private Integer awayScore;
}
