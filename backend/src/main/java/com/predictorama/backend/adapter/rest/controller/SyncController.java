package com.predictorama.backend.adapter.rest.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.predictorama.backend.domain.service.FixtureSyncResult;
import com.predictorama.backend.domain.service.FixtureSyncService;

import lombok.RequiredArgsConstructor;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class SyncController {
    
    private final FixtureSyncService fixtureSyncService;

    @PostMapping("/sync-results")
    public ResponseEntity<FixtureSyncResult> syncResults(
            @RequestParam String competition,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo
    ) {
        return ResponseEntity.ok(fixtureSyncService.syncCompetition(competition, dateFrom, dateTo));
    }
}
