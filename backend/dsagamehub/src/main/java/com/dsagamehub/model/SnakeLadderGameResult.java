package com.dsagamehub.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "snake_ladder_game_result")
public class SnakeLadderGameResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String playerName;
    private int boardSize;
    private int correctAnswer;
    private boolean isWin;
    private int userAnswer;

    // Algorithm performance - BFS and DP (two different algorithms)
    private long bfsTimeMs;
    private long dpTimeMs;
    private int minimumMoves;

    // Game round tracking
    private Long gameRoundId;

    @Column(name = "round_number")
    private int roundNumber;

    // Board configuration as JSON
    @Column(columnDefinition = "TEXT")
    private String snakesJson;

    @Column(columnDefinition = "TEXT")
    private String laddersJson;

    private LocalDateTime playedAt;

    @PrePersist
    protected void onCreate() {
        playedAt = LocalDateTime.now();
    }

    //GETTERS
    public Long getId() { return id; }
    public String getPlayerName() { return playerName; }
    public int getBoardSize() { return boardSize; }
    public int getCorrectAnswer() { return correctAnswer; }
    public boolean isWin() { return isWin; }
    public int getUserAnswer() { return userAnswer; }
    public long getBfsTimeMs() { return bfsTimeMs; }
    public long getDpTimeMs() { return dpTimeMs; }
    public int getMinimumMoves() { return minimumMoves; }
    public Long getGameRoundId() { return gameRoundId; }
    public int getRoundNumber() { return roundNumber; }
    public String getSnakesJson() { return snakesJson; }
    public String getLaddersJson() { return laddersJson; }
    public LocalDateTime getPlayedAt() { return playedAt; }

    // SETTERS
    public void setId(Long id) { this.id = id; }
    public void setPlayerName(String playerName) { this.playerName = playerName; }
    public void setBoardSize(int boardSize) { this.boardSize = boardSize; }
    public void setCorrectAnswer(int correctAnswer) { this.correctAnswer = correctAnswer; }
    public void setWin(boolean win) { isWin = win; }
    public void setUserAnswer(int userAnswer) { this.userAnswer = userAnswer; }
    public void setBfsTimeMs(long bfsTimeMs) { this.bfsTimeMs = bfsTimeMs; }
    public void setDpTimeMs(long dpTimeMs) { this.dpTimeMs = dpTimeMs; }
    public void setMinimumMoves(int minimumMoves) { this.minimumMoves = minimumMoves; }
    public void setGameRoundId(Long gameRoundId) { this.gameRoundId = gameRoundId; }
    public void setRoundNumber(int roundNumber) { this.roundNumber = roundNumber; }
    public void setSnakesJson(String snakesJson) { this.snakesJson = snakesJson; }
    public void setLaddersJson(String laddersJson) { this.laddersJson = laddersJson; }
    public void setPlayedAt(LocalDateTime playedAt) { this.playedAt = playedAt; }
}