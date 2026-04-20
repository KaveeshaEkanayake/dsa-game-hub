package com.dsagamehub.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
public class AlgorithmRun {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String gameName;
    private int roundNumber;
    private String algorithmType;
    private String logicUsed;
    private long solutionCount;
    private long timeTakenMs;
    private LocalDateTime createdAt;

    public AlgorithmRun() {
    }

    public AlgorithmRun(String gameName, int roundNumber, String algorithmType, String logicUsed, long solutionCount, long timeTakenMs) {
        this.gameName = gameName;
        this.roundNumber = roundNumber;
        this.algorithmType = algorithmType;
        this.logicUsed = logicUsed;
        this.solutionCount = solutionCount;
        this.timeTakenMs = timeTakenMs;
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public String getGameName() {
        return gameName;
    }

    public void setGameName(String gameName) {
        this.gameName = gameName;
    }

    public int getRoundNumber() {
        return roundNumber;
    }

    public void setRoundNumber(int roundNumber) {
        this.roundNumber = roundNumber;
    }

    public String getAlgorithmType() {
        return algorithmType;
    }

    public void setAlgorithmType(String algorithmType) {
        this.algorithmType = algorithmType;
    }

    public String getLogicUsed() {
        return logicUsed;
    }

    public void setLogicUsed(String logicUsed) {
        this.logicUsed = logicUsed;
    }

    public long getSolutionCount() {
        return solutionCount;
    }

    public void setSolutionCount(long solutionCount) {
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
