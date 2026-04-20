package com.dsagamehub.dto;

public class SnakeLadderAnswerRequest {

    private String playerName;    // Name of the player
    private String answerText;    // The answer selected by player (as string)
    private Long roundId;         // The current game round ID

    // Default constructor (required for JSON deserialization)
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