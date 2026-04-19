package com.dsagamehub.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
public class AlgorithmRun {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ADD THIS FIELD
    private Long gameRoundId;  // Link to specific game round

    private String gameName;
    private String algorithmType;
    private int solutionCount;
    private long timeTakenMs;
    private LocalDateTime createdAt;

    public AlgorithmRun() {
    }

    public AlgorithmRun(String gameName, String algorithmType, int solutionCount, long timeTakenMs) {
        this.gameName = gameName;
        this.algorithmType = algorithmType;
        this.solutionCount = solutionCount;
        this.timeTakenMs = timeTakenMs;
        this.createdAt = LocalDateTime.now();
    }

    // ADD THIS GETTER AND SETTER
    public Long getGameRoundId() { return gameRoundId; }
    public void setGameRoundId(Long gameRoundId) { this.gameRoundId = gameRoundId; }

    public Long getId() {
        return id;
    }

    public String getGameName() {
        return gameName;
    }

    public void setGameName(String gameName) {
        this.gameName = gameName;
    }

    public String getAlgorithmType() {
        return algorithmType;
    }

    public void setAlgorithmType(String algorithmType) {
        this.algorithmType = algorithmType;
    }

    public int getSolutionCount() {
        return solutionCount;
    }

    public void setSolutionCount(int solutionCount) {
        this.solutionCount = solutionCount;
    }

    public long getTimeTakenMs() {
        return timeTakenMs;
    }

    public void setTimeTakenMs(long timeTakenMs) {
        this.timeTakenMs = timeTakenMs;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}