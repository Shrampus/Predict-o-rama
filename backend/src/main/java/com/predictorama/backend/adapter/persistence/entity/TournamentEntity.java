package com.predictorama.backend.adapter.persistence.entity;

import com.predictorama.backend.domain.entity.Tournament;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "tournaments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TournamentEntity extends BaseEntity {

    @Id
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Column(length = 500)
    private String description;

    @Column(name = "season_identifier", length = 100)
    private String seasonIdentifier;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Tournament.Sport sport;
}
