package com.predictorama.backend.adapter.rest.controller;

import com.predictorama.backend.adapter.rest.dto.GroupTournamentResponse;
import com.predictorama.backend.domain.service.CompetitionCatalog;
import com.predictorama.backend.domain.service.TournamentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping({"/api/tournaments", "/api/v1/tournaments"})
@RequiredArgsConstructor
public class TournamentController {

    private final TournamentService tournamentService;
    private final CompetitionCatalog competitionCatalog;

    @GetMapping
    public ResponseEntity<List<GroupTournamentResponse>> getAllTournaments() {
        return ResponseEntity.ok(
                tournamentService.getAllTournaments().stream()
                        .map(tournament -> new GroupTournamentResponse(
                                tournament.getId(),
                                competitionCatalog.toCompetitionCode(tournament.getName()),
                                tournament.getName(),
                                tournament.getSeasonLabel(),
                                tournament.getSport()
                        ))
                        .toList()
        );
    }
}
