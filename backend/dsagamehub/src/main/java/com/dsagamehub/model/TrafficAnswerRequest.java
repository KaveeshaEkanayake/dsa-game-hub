package com.dsagamehub.model;

public class TrafficAnswerRequest {

    private Long roundId;
    private String playerName;
    private int playerAnswer;

    public TrafficAnswerRequest() {}

    public TrafficAnswerRequest(Long roundId, String playerName, int playerAnswer) {
        this.roundId = roundId;
        this.playerName = playerName;
        this.playerAnswer = playerAnswer;
    }

    public Long getRoundId() { return roundId; }
    public void setRoundId(Long roundId) { this.roundId = roundId; }

    public String getPlayerName() { return playerName; }
    public void setPlayerName(String playerName) { this.playerName = playerName; }

    public int getPlayerAnswer() { return playerAnswer; }
    public void setPlayerAnswer(int playerAnswer) { this.playerAnswer = playerAnswer; }
}