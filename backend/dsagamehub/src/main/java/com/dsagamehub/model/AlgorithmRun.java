package com.dsagamehub.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "algorithm_runs")
public class AlgorithmRun {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "algorithm_name", nullable = false)
    private String algorithmName;

    @Column(name = "result_data", columnDefinition = "TEXT")
    private String resultData;

    @Column(name = "execution_time_ms", nullable = false)
    private Long executionTimeMs;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @ManyToOne
    @JoinColumn(name = "game_round_id", nullable = false)
    private GameRound gameRound;

    public AlgorithmRun() {
    }

    public Long getId() {
        return id;
    }

    public String getAlgorithmName() {
        return algorithmName;
    }

    public String getResultData() {
        return resultData;
    }

    public Long getExecutionTimeMs() {
        return executionTimeMs;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public GameRound getGameRound() {
        return gameRound;
    }

    public void setAlgorithmName(String algorithmName) {
        this.algorithmName = algorithmName;
    }

    public void setResultData(String resultData) {
        this.resultData = resultData;
    }

    public void setExecutionTimeMs(Long executionTimeMs) {
        this.executionTimeMs = executionTimeMs;
    }

    public void setGameRound(GameRound gameRound) {
        this.gameRound = gameRound;
    }
}