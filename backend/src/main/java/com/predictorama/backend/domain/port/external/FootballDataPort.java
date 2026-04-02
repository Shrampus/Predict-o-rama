package com.predictorama.backend.domain.port.external;

import com.predictorama.backend.domain.entity.Match;

import java.util.List;

public interface FootballDataPort {
    List<Match> getUpcomingMatches(String competition);
}