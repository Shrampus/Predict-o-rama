package com.predictorama.backend.adapter.rest.dto;

import java.util.UUID;

public class CreatePredictionRequest {

    public UUID userId;
    public UUID matchId;
    public UUID groupId;
    public int homeScore;
    public int awayScore;



    public CreatePredictionRequest() {}
}