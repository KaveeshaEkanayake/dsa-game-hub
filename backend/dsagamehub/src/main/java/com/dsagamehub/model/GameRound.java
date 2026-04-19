package com.dsagamehub.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
public class GameRound {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String gameName;
    private int roundNumber;
    private String status;
    private boolean allSolutionsFound;

    private long targetSolutionCount;
    private long recognizedSolutionCount;

    private boolean sequentialRunCompleted;
    private boolean threadedRunCompleted;

    private long sequentialTimeTakenNs;
    private long threadedTimeTakenNs;

    private long sequentialSolutionCount;
    private long threadedSolutionCount;

    private LocalDateTime createdAt;
    private LocalDateTime completedAt;

    public GameRound() {
    }

    public GameRound(String gameName, int roundNumber) {
        this.gameName = gameName;
        this.roundNumber = roundNumber;
        this.status = "ACTIVE";
        this.allSolutionsFound = false;
        this.targetSolutionCount = 0;
        this.recognizedSolutionCount = 0;
        this.sequentialRunCompleted = false;
        this.threadedRunCompleted = false;
        this.sequentialTimeTakenNs = 0;
        this.threadedTimeTakenNs = 0;
        this.sequentialSolutionCount = 0;
        this.threadedSolutionCount = 0;
        this.createdAt = LocalDateTime.now();
    }

    @PrePersist
    public void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (status == null || status.isBlank()) {
            status = "ACTIVE";
        }
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public boolean isAllSolutionsFound() {
        return allSolutionsFound;
    }

    public void setAllSolutionsFound(boolean allSolutionsFound) {
        this.allSolutionsFound = allSolutionsFound;
    }

    public long getTargetSolutionCount() {
        return targetSolutionCount;
    }

    public void setTargetSolutionCount(long targetSolutionCount) {
        this.targetSolutionCount = targetSolutionCount;
    }

    public long getRecognizedSolutionCount() {
        return recognizedSolutionCount;
    }

    public void setRecognizedSolutionCount(long recognizedSolutionCount) {
        this.recognizedSolutionCount = recognizedSolutionCount;
    }

    public boolean isSequentialRunCompleted() {
        return sequentialRunCompleted;
    }

    public void setSequentialRunCompleted(boolean sequentialRunCompleted) {
        this.sequentialRunCompleted = sequentialRunCompleted;
    }

    public boolean isThreadedRunCompleted() {
        return threadedRunCompleted;
    }

    public void setThreadedRunCompleted(boolean threadedRunCompleted) {
        this.threadedRunCompleted = threadedRunCompleted;
    }

    public long getSequentialTimeTakenNs() {
        return sequentialTimeTakenNs;
    }

    public void setSequentialTimeTakenNs(long sequentialTimeTakenNs) {
        this.sequentialTimeTakenNs = sequentialTimeTakenNs;
    }

    public long getThreadedTimeTakenNs() {
        return threadedTimeTakenNs;
    }

    public void setThreadedTimeTakenNs(long threadedTimeTakenNs) {
        this.threadedTimeTakenNs = threadedTimeTakenNs;
    }

    public long getSequentialSolutionCount() {
        return sequentialSolutionCount;
    }

    public void setSequentialSolutionCount(long sequentialSolutionCount) {
        this.sequentialSolutionCount = sequentialSolutionCount;
    }

    public long getThreadedSolutionCount() {
        return threadedSolutionCount;
    }

    public void setThreadedSolutionCount(long threadedSolutionCount) {
        this.threadedSolutionCount = threadedSolutionCount;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
    }
}