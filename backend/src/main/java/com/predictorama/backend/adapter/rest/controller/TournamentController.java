package com.predictorama.backend.adapter.rest.controller;

import com.predictorama.backend.adapter.rest.SessionService;
import com.predictorama.backend.adapter.rest.dto.GroupTournamentResponse;
import com.predictorama.backend.domain.service.CompetitionCatalog;
import com.predictorama.backend.domain.service.TournamentService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;
import java.util.function.Function;

@RestController
@RequestMapping("/api/tournaments")
@RequiredArgsConstructor
public class TournamentController {

    private final TournamentService tournamentService;
    private final SessionService sessionService;
    private final CompetitionCatalog competitionCatalog;

    private <T> ResponseEntity<T> withUser(HttpSession session, Function<UUID, ResponseEntity<T>> action) {
        return sessionService.getUserId(session)
                .map(action)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.UNAUTHORIZED).build());
    }

    @GetMapping
    public ResponseEntity<List<GroupTournamentResponse>> getAllTournaments(HttpSession session) {
        return withUser(session, _userId -> ResponseEntity.ok(
                tournamentService.getAllTournaments().stream()
                        .map(tournament -> new GroupTournamentResponse(
                                tournament.getId(),
                                competitionCatalog.toCompetitionCode(tournament.getName()),
                                tournament.getName(),
                                tournament.getDescription(),
                                tournament.getSport()
                        ))
                        .toList()
        ));
    }
}
