package com.predictorama.backend.adapter.persistence.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "teams")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TeamEntity extends BaseEntity {

    @Id
    private UUID id;

    @Column(nullable = false, unique = true)
    private String name;
}
