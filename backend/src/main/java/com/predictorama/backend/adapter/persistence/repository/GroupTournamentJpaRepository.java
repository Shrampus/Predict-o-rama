package com.predictorama.backend.adapter.persistence.repository;

import com.predictorama.backend.adapter.persistence.entity.GroupTournamentEntity;
import com.predictorama.backend.adapter.persistence.entity.GroupTournamentId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface GroupTournamentJpaRepository extends JpaRepository<GroupTournamentEntity, GroupTournamentId> {
    @Query("select gt.tournamentId from GroupTournamentEntity gt where gt.groupId = :groupId")
    List<UUID> findTournamentIdsByGroupId(@Param("groupId") UUID groupId);

    boolean existsByGroupIdAndTournamentId(UUID groupId, UUID tournamentId);
}
