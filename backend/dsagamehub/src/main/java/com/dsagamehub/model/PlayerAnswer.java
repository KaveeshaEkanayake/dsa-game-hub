package com.dsagamehub.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
public class PlayerAnswer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String playerName;

    @Column(columnDefinition = "TEXT")
    private String answerText;

    private int roundNumber;

    private long sequentialCheckTimeNs;
    private long threadedCheckTimeNs;
    private long totalCheckTimeNs;

    private String bestAlgorithm;
    private String comparisonMessage;

    private LocalDateTime submittedAt;

    public PlayerAnswer() {
    }

    public PlayerAnswer(String playerName,
                        String answerText,
                        int roundNumber,
                        long sequentialCheckTimeNs,
                        long threadedCheckTimeNs,
                        long totalCheckTimeNs,
                        String bestAlgorithm,
                        String comparisonMessage) {
        this.playerName = playerName;
        this.answerText = answerText;
        this.roundNumber = roundNumber;
        this.sequentialCheckTimeNs = sequentialCheckTimeNs;
        this.threadedCheckTimeNs = threadedCheckTimeNs;
        this.totalCheckTimeNs = totalCheckTimeNs;
        this.bestAlgorithm = bestAlgorithm;
        this.comparisonMessage = comparisonMessage;
        this.submittedAt = LocalDateTime.now();
    }

    @PrePersist
    public void onCreate() {
        if (submittedAt == null) {
            submittedAt = LocalDateTime.now();
        }
    }

    public Long getId() {
        return id;
    }

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

    public int getRoundNumber() {
        return roundNumber;
    }

    public void setRoundNumber(int roundNumber) {
        this.roundNumber = roundNumber;
    }

    public long getSequentialCheckTimeNs() {
        return sequentialCheckTimeNs;
    }

    public void setSequentialCheckTimeNs(long sequentialCheckTimeNs) {
        this.sequentialCheckTimeNs = sequentialCheckTimeNs;
    }

    public long getThreadedCheckTimeNs() {
        return threadedCheckTimeNs;
    }

    public void setThreadedCheckTimeNs(long threadedCheckTimeNs) {
        this.threadedCheckTimeNs = threadedCheckTimeNs;
    }

    public long getTotalCheckTimeNs() {
        return totalCheckTimeNs;
    }

    public void setTotalCheckTimeNs(long totalCheckTimeNs) {
        this.totalCheckTimeNs = totalCheckTimeNs;
    }

    public String getBestAlgorithm() {
        return bestAlgorithm;
    }

    public void setBestAlgorithm(String bestAlgorithm) {
        this.bestAlgorithm = bestAlgorithm;
    }

    public String getComparisonMessage() {
        return comparisonMessage;
    }

    public void setComparisonMessage(String comparisonMessage) {
        this.comparisonMessage = comparisonMessage;
    }

    public LocalDateTime getSubmittedAt() {
        return submittedAt;
    }

    public void setSubmittedAt(LocalDateTime submittedAt) {
        this.submittedAt = submittedAt;
    }
}