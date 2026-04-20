package com.dsagamehub.dto;

import jakarta.validation.constraints.NotBlank;

public class PlayerAnswerRequest {

    @NotBlank(message = "Player name is required")
    private String playerName;
    private Long roundId;  // ADD THIS FIELD

    @NotBlank(message = "Answer is required")
    private String answerText;

    public PlayerAnswerRequest() {
    }

    // ADD THIS GETTER AND SETTER
    public Long getRoundId() { return roundId; }
    public void setRoundId(Long roundId) { this.roundId = roundId; }

    public String getPlayerName() {
        return playerName;
    }

    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }

    public String getAnswerText() {
        return answerText;
    }

    public void setAnswerText(String answerText) {
        this.answerText = answerText;
    }
}