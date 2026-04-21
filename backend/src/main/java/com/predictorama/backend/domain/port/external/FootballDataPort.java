package com.predictorama.backend.domain.port.external;

import com.predictorama.backend.domain.entity.Match;

import java.time.LocalDate;
import java.util.List;

public interface FootballDataPort {
    List<Match> getMatches(String competition, LocalDate dateFrom, LocalDate dateTo);
}
