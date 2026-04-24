package com.predictorama.backend.domain.port.external;

import com.predictorama.backend.domain.entity.CompetitionSeasonMetadata;
import com.predictorama.backend.domain.entity.Match;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface FootballDataPort {
    List<Match> getMatches(String competition, LocalDate dateFrom, LocalDate dateTo);

    Optional<CompetitionSeasonMetadata> getCurrentSeasonMetadata(String competition);
}
