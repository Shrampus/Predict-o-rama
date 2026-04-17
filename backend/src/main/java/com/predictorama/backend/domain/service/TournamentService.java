package com.predictorama.backend.domain.service;

import com.predictorama.backend.domain.entity.Tournament;
import com.predictorama.backend.domain.port.persistence.TournamentRepositoryPort;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
public class TournamentService {

    private final TournamentRepositoryPort tournamentRepository;

    public List<Tournament> getAllTournaments() {
        return tournamentRepository.findAll();
    }
}
