package com.predictorama.backend.adapter.persistence.repository;

import com.predictorama.backend.adapter.persistence.entity.GroupTournamentEntity;
import com.predictorama.backend.adapter.persistence.entity.GroupTournamentId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface GroupTournamentJpaRepository extends JpaRepository<GroupTournamentEntity, GroupTournamentId> {
    List<GroupTournamentEntity> findByGroupId(UUID groupId);

    boolean existsByGroupIdAndTournamentId(UUID groupId, UUID tournamentId);
}
