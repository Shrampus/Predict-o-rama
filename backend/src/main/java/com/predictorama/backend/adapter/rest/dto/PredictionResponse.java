package com.predictorama.backend.adapter.rest.dto;

public class PredictionResponse {

    public String userName;
    public String groupName;
    public String matchResult;
    public int predictedScore;
    public boolean isWinner;

    public PredictionResponse() {}
}