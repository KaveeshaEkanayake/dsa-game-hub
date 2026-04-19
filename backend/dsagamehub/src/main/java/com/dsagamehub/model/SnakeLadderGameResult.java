/*package com.dsagamehub.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "snake_ladder_game_result")  // Single table for all results
public class SnakeLadderGameResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Player information
    private String playerName;

    // Game configuration
    private int boardSize;
    private int correctAnswer;

    // Game result
    private boolean isWin;
    private int userAnswer;

    // Algorithm performance
    private long bfsTimeMs;
    private long dfsTimeMs;
    private int minimumMoves;

    // Complete game state (like solutionPath in KnightTour)
    @Column(columnDefinition = "TEXT")
    private String boardConfiguration;  // Store snakes/ladders as JSON

    @Column(columnDefinition = "TEXT")
    private String gameState;  // Store complete game state if needed

    // Timestamp
    private LocalDateTime playedAt;

    @PrePersist
    protected void onCreate() {
        playedAt = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getPlayerName() { return playerName; }
    public void setPlayerName(String playerName) { this.playerName = playerName; }

    public int getBoardSize() { return boardSize; }
    public void setBoardSize(int boardSize) { this.boardSize = boardSize; }

    public int getCorrectAnswer() { return correctAnswer; }
    public void setCorrectAnswer(int correctAnswer) { this.correctAnswer = correctAnswer; }

    public boolean isWin() { return isWin; }
    public void setWin(boolean win) { isWin = win; }

    public int getUserAnswer() { return userAnswer; }
    public void setUserAnswer(int userAnswer) { this.userAnswer = userAnswer; }

    public long getBfsTimeMs() { return bfsTimeMs; }
    public void setBfsTimeMs(long bfsTimeMs) { this.bfsTimeMs = bfsTimeMs; }

    public long getDfsTimeMs() { return dfsTimeMs; }
    public void setDfsTimeMs(long dfsTimeMs) { this.dfsTimeMs = dfsTimeMs; }

    public int getMinimumMoves() { return minimumMoves; }
    public void setMinimumMoves(int minimumMoves) { this.minimumMoves = minimumMoves; }

    public String getBoardConfiguration() { return boardConfiguration; }
    public void setBoardConfiguration(String boardConfiguration) { this.boardConfiguration = boardConfiguration; }

    public String getGameState() { return gameState; }
    public void setGameState(String gameState) { this.gameState = gameState; }

    public LocalDateTime getPlayedAt() { return playedAt; }
    public void setPlayedAt(LocalDateTime playedAt) { this.playedAt = playedAt; }
}*/

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

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getPlayerName() { return playerName; }
    public void setPlayerName(String playerName) { this.playerName = playerName; }

    public int getBoardSize() { return boardSize; }
    public void setBoardSize(int boardSize) { this.boardSize = boardSize; }

    public int getCorrectAnswer() { return correctAnswer; }
    public void setCorrectAnswer(int correctAnswer) { this.correctAnswer = correctAnswer; }

    public boolean isWin() { return isWin; }
    public void setWin(boolean win) { isWin = win; }

    public int getUserAnswer() { return userAnswer; }
    public void setUserAnswer(int userAnswer) { this.userAnswer = userAnswer; }

    public long getBfsTimeMs() { return bfsTimeMs; }
    public void setBfsTimeMs(long bfsTimeMs) { this.bfsTimeMs = bfsTimeMs; }

    public long getDpTimeMs() { return dpTimeMs; }
    public void setDpTimeMs(long dpTimeMs) { this.dpTimeMs = dpTimeMs; }

    public int getMinimumMoves() { return minimumMoves; }
    public void setMinimumMoves(int minimumMoves) { this.minimumMoves = minimumMoves; }

    public Long getGameRoundId() { return gameRoundId; }
    public void setGameRoundId(Long gameRoundId) { this.gameRoundId = gameRoundId; }

    public int getRoundNumber() { return roundNumber; }
    public void setRoundNumber(int roundNumber) { this.roundNumber = roundNumber; }

    public String getSnakesJson() { return snakesJson; }
    public void setSnakesJson(String snakesJson) { this.snakesJson = snakesJson; }

    public String getLaddersJson() { return laddersJson; }
    public void setLaddersJson(String laddersJson) { this.laddersJson = laddersJson; }

    public LocalDateTime getPlayedAt() { return playedAt; }
    public void setPlayedAt(LocalDateTime playedAt) { this.playedAt = playedAt; }
}