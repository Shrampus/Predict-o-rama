package com.predictorama.backend.domain.service;

import com.predictorama.backend.domain.entity.Match;
import com.predictorama.backend.domain.port.external.FootballDataPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PredictionPageService {

    private final FootballDataPort footballDataPort;

    public List<Match> getPredictionPageMatches(String competition) {
        return footballDataPort.getUpcomingMatches(competition);
    }
}