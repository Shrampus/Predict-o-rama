package com.predictorama.backend.adapter.persistence.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "groups")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GroupEntity extends BaseEntity {

    @Id
    private UUID id;

    @Column(name = "owner_id", nullable = false)
    private UUID ownerId;

    @Column(name = "invite_code", unique = true, length = 50)
    private UUID inviteCode;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 500)
    private String description;

}
