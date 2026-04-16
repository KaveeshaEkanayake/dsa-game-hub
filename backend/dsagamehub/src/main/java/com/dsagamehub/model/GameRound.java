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
    private boolean allSolutionsFound;
    private LocalDateTime createdAt;

    public GameRound() {
    }

    public GameRound(String gameName, int roundNumber, boolean allSolutionsFound) {
        this.gameName = gameName;
        this.roundNumber = roundNumber;
        this.allSolutionsFound = allSolutionsFound;
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

    public boolean isAllSolutionsFound() {
        return allSolutionsFound;
    }

    public void setAllSolutionsFound(boolean allSolutionsFound) {
        this.allSolutionsFound = allSolutionsFound;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}