
package com.dsagamehub.dto;

import java.time.LocalDateTime;

public class PerformanceRecord {
    private int roundNumber;
    private int boardSize;
    private double bfsTimeMs;
    private double dpTimeMs;
    private String winner;
    private LocalDateTime timestamp;

    public PerformanceRecord() {}

    // Constructor with all fields
    public PerformanceRecord(int roundNumber, int boardSize,
                             double bfsTimeMs, double dpTimeMs,
                             String winner, LocalDateTime timestamp) {
        this.roundNumber = roundNumber;
        this.boardSize = boardSize;
        this.bfsTimeMs = bfsTimeMs;
        this.dpTimeMs = dpTimeMs;
        this.winner = winner;
        this.timestamp = timestamp;
    }

    // Getters
    public int getRoundNumber() { return roundNumber; }
    public int getBoardSize() { return boardSize; }
    public double getBfsTimeMs() { return bfsTimeMs; }
    public double getDpTimeMs() { return dpTimeMs; }
    public String getWinner() { return winner; }
    public LocalDateTime getTimestamp() { return timestamp; }

    // Setters
    public void setRoundNumber(int roundNumber) { this.roundNumber = roundNumber; }
    public void setBoardSize(int boardSize) { this.boardSize = boardSize; }
    public void setBfsTimeMs(double bfsTimeMs) { this.bfsTimeMs = bfsTimeMs; }
    public void setDpTimeMs(double dpTimeMs) { this.dpTimeMs = dpTimeMs; }
    public void setWinner(String winner) { this.winner = winner; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
}