package com.predictorama.backend.domain.service.result;

import com.predictorama.backend.domain.entity.Group;
import com.predictorama.backend.domain.entity.Match;
import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Getter
@Builder
public class UpcomingMatchResult {
    private Match match;
    private String competitionCode;
    private String tournamentName;
    private List<Group> userGroups;
    private Set<UUID> groupIdsWithPrediction;
}
