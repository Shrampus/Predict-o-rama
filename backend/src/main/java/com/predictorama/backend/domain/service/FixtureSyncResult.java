package com.predictorama.backend.domain.service;

import java.time.LocalDate;

public record FixtureSyncResult(
        String competition,
        LocalDate dateFrom,
        LocalDate dateTo,
        int importedCount,
        int scoredCount
) {
}
