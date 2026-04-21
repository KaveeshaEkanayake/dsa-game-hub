package com.dsagamehub.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "knight_tour_results")
public class KnightTourResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String playerName;
    private int boardSize;
    private int startRow;
    private int startCol;
    private boolean isCorrect;
    private String gameResult; // "win", "lose", "draw"
    private long algorithm1TimeMs;
    private long algorithm2TimeMs;

    @Column(columnDefinition = "TEXT")
    private String solutionPath;

    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getPlayerName() { return playerName; }
    public void setPlayerName(String playerName) { this.playerName = playerName; }
    public int getBoardSize() { return boardSize; }
    public void setBoardSize(int boardSize) { this.boardSize = boardSize; }
    public int getStartRow() { return startRow; }
    public void setStartRow(int startRow) { this.startRow = startRow; }
    public int getStartCol() { return startCol; }
    public void setStartCol(int startCol) { this.startCol = startCol; }
    public boolean isCorrect() { return isCorrect; }
    public void setCorrect(boolean correct) { isCorrect = correct; }
    public String getGameResult() { return gameResult; }
    public void setGameResult(String gameResult) { this.gameResult = gameResult; }
    public long getAlgorithm1TimeMs() { return algorithm1TimeMs; }
    public void setAlgorithm1TimeMs(long algorithm1TimeMs) { this.algorithm1TimeMs = algorithm1TimeMs; }
    public long getAlgorithm2TimeMs() { return algorithm2TimeMs; }
    public void setAlgorithm2TimeMs(long algorithm2TimeMs) { this.algorithm2TimeMs = algorithm2TimeMs; }
    public String getSolutionPath() { return solutionPath; }
    public void setSolutionPath(String solutionPath) { this.solutionPath = solutionPath; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}