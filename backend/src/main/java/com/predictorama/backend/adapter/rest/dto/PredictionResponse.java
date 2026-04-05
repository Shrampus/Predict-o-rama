package com.predictorama.backend.adapter.rest.dto;

public class PredictionResponse {

    public String userName;
    public String groupName;
    public String matchResult;
    public int predictedScoreHome;
    public int predictedScoreAway;
    public boolean isWinner;

    public PredictionResponse() {}
}