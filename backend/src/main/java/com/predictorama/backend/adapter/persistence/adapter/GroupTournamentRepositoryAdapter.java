package com.predictorama.backend.adapter.persistence.adapter;

import com.predictorama.backend.adapter.persistence.entity.GroupTournamentEntity;
import com.predictorama.backend.adapter.persistence.repository.GroupTournamentJpaRepository;
import com.predictorama.backend.domain.port.persistence.GroupTournamentRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class GroupTournamentRepositoryAdapter implements GroupTournamentRepositoryPort {

    private final GroupTournamentJpaRepository groupTournamentJpaRepository;

    @Override
    public List<UUID> findTournamentIdsByGroupId(UUID groupId) {
        return groupTournamentJpaRepository.findByGroupId(groupId)
                .stream()
                .map(GroupTournamentEntity::getTournamentId)
                .toList();
    }

    @Override
    public boolean existsByGroupIdAndTournamentId(UUID groupId, UUID tournamentId) {
        return groupTournamentJpaRepository.existsByGroupIdAndTournamentId(groupId, tournamentId);
    }

    @Override
    public void save(UUID groupId, UUID tournamentId) {
        groupTournamentJpaRepository.save(GroupTournamentEntity.builder()
                .groupId(groupId)
                .tournamentId(tournamentId)
                .build());
    }
}
