package com.predictorama.backend.domain.entity.aggregate;

import com.predictorama.backend.domain.entity.Tournament;

import java.util.List;
import java.util.UUID;

public record GroupLeaderboardView(
        Tournament tournament,
        List<Entry> entries
) {
    public record Entry(
            UUID userId,
            String username,
            int totalScore,
            int scoredPredictions,
            int totalPredictions
    ) {
    }
}
