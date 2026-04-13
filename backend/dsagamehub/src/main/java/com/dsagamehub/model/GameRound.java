package com.dsagamehub.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "game_rounds")
public class GameRound {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "game_name", nullable = false)
    private String gameName;

    @Column(name = "round_number", nullable = false)
    private Integer roundNumber;

    @Column(name = "input_data", columnDefinition = "TEXT")
    private String inputData;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    public GameRound() {
    }

    public GameRound(String gameName, Integer roundNumber, String inputData) {
        this.gameName = gameName;
        this.roundNumber = roundNumber;
        this.inputData = inputData;
    }

    public Long getId() {
        return id;
    }

    public String getGameName() {
        return gameName;
    }

    public Integer getRoundNumber() {
        return roundNumber;
    }

    public String getInputData() {
        return inputData;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setGameName(String gameName) {
        this.gameName = gameName;
    }

    public void setRoundNumber(Integer roundNumber) {
        this.roundNumber = roundNumber;
    }

    public void setInputData(String inputData) {
        this.inputData = inputData;
    }
}