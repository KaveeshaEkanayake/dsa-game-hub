package com.dsagamehub.dto;

public class PlayerAnswerRequest {

    private String playerName;
    private String answerData;

    public PlayerAnswerRequest() {
    }

    public String getPlayerName() {
        return playerName;
    }

    public String getAnswerData() {
        return answerData;
    }

    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }

    public void setAnswerData(String answerData) {
        this.answerData = answerData;
    }
}