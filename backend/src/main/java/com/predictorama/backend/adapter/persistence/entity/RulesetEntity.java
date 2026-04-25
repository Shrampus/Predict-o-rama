package com.predictorama.backend.adapter.persistence.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Map;
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

    @ElementCollection
    @CollectionTable(name = "ruleset_rules", joinColumns = @JoinColumn(name = "ruleset_id"))
    @MapKeyColumn(name = "rule_name")
    @Column(name = "points")
    private Map<String, Integer> rulePoints;

}
