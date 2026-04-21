package com.dsagamehub.dto;

public class SnakeLadderAnswerRequest {

    private String playerName;
    private String answerText;
    private Long roundId;
    public SnakeLadderAnswerRequest() {
    }

    // Constructor with all fields
    public SnakeLadderAnswerRequest(String playerName, String answerText, Long roundId) {
        this.playerName = playerName;
        this.answerText = answerText;
        this.roundId = roundId;
    }

    // Getters
    public String getPlayerName() {
        return playerName;
    }

    public String getAnswerText() {
        return answerText;
    }

    public Long getRoundId() {
        return roundId;
    }

    // Setters
    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }

    public void setAnswerText(String answerText) {
        this.answerText = answerText;
    }

    public void setRoundId(Long roundId) {
        this.roundId = roundId;
    }

    @Override
    public String toString() {
        return "SnakeLadderAnswerRequest{" +
                "playerName='" + playerName + '\'' +
                ", answerText='" + answerText + '\'' +
                ", roundId=" + roundId +
                '}';
    }
}