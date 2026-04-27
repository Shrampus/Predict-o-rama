package com.predictorama.backend.adapter.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "group_tournaments", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"group_id", "tournament_id"})
})
@IdClass(GroupTournamentId.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GroupTournamentEntity {

    @Id
    @Column(name = "group_id", nullable = false)
    private UUID groupId;

    @Id
    @Column(name = "tournament_id", nullable = false)
    private UUID tournamentId;

    @Column(name = "ruleset_id")
    private UUID rulesetId;
}
