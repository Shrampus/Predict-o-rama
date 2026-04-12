package com.predictorama.backend.adapter.rest.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.predictorama.backend.domain.service.MatchResultSyncService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class SyncController {
    
    private final MatchResultSyncService matchResultSyncService;

    @PostMapping("/sync-results")
    public ResponseEntity<Void> syncResults(){
        matchResultSyncService.syncAllCompetitions();
        return ResponseEntity.ok().build();
    }
}
