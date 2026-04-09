package com.predictorama.backend.adapter.persistence.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "rulesets")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class RulesetEntity extends BaseEntity {

    @Id
    private UUID id;

    @Column(nullable = false, unique = true, length = 100)
    private String name;

    @ElementCollection
    @CollectionTable(name = "ruleset_rules", joinColumns = @JoinColumn(name = "ruleset_id"))
    @Column(name = "rule_name")
    private Set<String> ruleNames;

}
