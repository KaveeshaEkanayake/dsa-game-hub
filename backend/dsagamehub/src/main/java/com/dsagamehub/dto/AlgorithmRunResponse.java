package com.dsagamehub.dto;

public class AlgorithmRunResponse {

    private String gameName;
    private String algorithmType;
    private int solutionCount;
    private long timeTakenMs;

    public AlgorithmRunResponse() {
    }

    public AlgorithmRunResponse(String gameName, String algorithmType, int solutionCount, long timeTakenMs) {
        this.gameName = gameName;
        this.algorithmType = algorithmType;
        this.solutionCount = solutionCount;
        this.timeTakenMs = timeTakenMs;
    }

    public String getGameName() {
        return gameName;
    }

    public String getAlgorithmType() {
        return algorithmType;
    }

    public int getSolutionCount() {
        return solutionCount;
    }

    public long getTimeTakenMs() {
        return timeTakenMs;
    }
}