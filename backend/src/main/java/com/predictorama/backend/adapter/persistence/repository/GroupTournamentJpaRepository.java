package com.predictorama.backend.adapter.persistence.repository;

import com.predictorama.backend.adapter.persistence.entity.GroupTournamentEntity;
import com.predictorama.backend.adapter.persistence.entity.GroupTournamentId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface GroupTournamentJpaRepository extends JpaRepository<GroupTournamentEntity, GroupTournamentId> {
    @Query("select gt.tournamentId from GroupTournamentEntity gt where gt.groupId = :groupId")
    List<UUID> findTournamentIdsByGroupId(@Param("groupId") UUID groupId);

    boolean existsByGroupIdAndTournamentId(UUID groupId, UUID tournamentId);

    void deleteByGroupIdAndTournamentId(UUID groupId, UUID tournamentId);

    @Query("select gt.rulesetId from GroupTournamentEntity gt where gt.groupId = :groupId and gt.tournamentId = :tournamentId")
    Optional<UUID> findRulesetIdByGroupIdAndTournamentId(@Param("groupId") UUID groupId, @Param("tournamentId") UUID tournamentId);

    @Transactional
    @Modifying
    @Query("UPDATE GroupTournamentEntity g SET g.rulesetId = :rulesetId WHERE g.groupId = :groupId AND g.tournamentId = :tournamentId")
    void updateRulesetId(@Param("groupId") UUID groupId, @Param("tournamentId") UUID tournamentId, @Param("rulesetId") UUID rulesetId);
}
