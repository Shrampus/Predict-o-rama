package com.predictorama.backend.adapter.persistence.repository;

import com.predictorama.backend.adapter.persistence.entity.RulesetEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface RulesetJpaRepository extends JpaRepository<RulesetEntity, UUID> {

    Optional<RulesetEntity> findByName(String name);

}
