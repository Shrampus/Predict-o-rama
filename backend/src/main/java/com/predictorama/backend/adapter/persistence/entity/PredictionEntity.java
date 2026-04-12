
package com.predictorama.backend.adapter.persistence.entity;

import com.predictorama.backend.domain.entity.Winner;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "predictions", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_id", "match_id", "group_id"})
})

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PredictionEntity extends BaseEntity {

    @Id
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "match_id", nullable = false)
    private UUID matchId;

    @Column(name = "group_id", nullable = false)
    private UUID groupId;

    @Enumerated(EnumType.STRING)
    @Column(name = "predicted_winner")
    private Winner predictedWinner;

    @Column(name = "submitted_at")
    private Instant submittedAt;

    // Nullable — set after the match is scored
    @Column
    private Integer result;
}
