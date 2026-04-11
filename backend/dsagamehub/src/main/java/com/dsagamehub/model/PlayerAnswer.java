package com.dsagamehub.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "player_answers")
public class PlayerAnswer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "answer_data", columnDefinition = "TEXT", nullable = false)
    private String answerData;

    @Column(name = "is_correct")
    private Boolean isCorrect = false;

    @Column(name = "is_duplicate")
    private Boolean isDuplicate = false;

    @Column(name = "submitted_at")
    private LocalDateTime submittedAt = LocalDateTime.now();

    @ManyToOne
    @JoinColumn(name = "player_id", nullable = false)
    private Player player;

    @ManyToOne
    @JoinColumn(name = "game_round_id", nullable = false)
    private GameRound gameRound;

    public PlayerAnswer() {
    }

    public Long getId() {
        return id;
    }

    public String getAnswerData() {
        return answerData;
    }

    public Boolean getIsCorrect() {
        return isCorrect;
    }

    public Boolean getIsDuplicate() {
        return isDuplicate;
    }

    public LocalDateTime getSubmittedAt() {
        return submittedAt;
    }

    public Player getPlayer() {
        return player;
    }

    public GameRound getGameRound() {
        return gameRound;
    }

    public void setAnswerData(String answerData) {
        this.answerData = answerData;
    }

    public void setIsCorrect(Boolean correct) {
        isCorrect = correct;
    }

    public void setIsDuplicate(Boolean duplicate) {
        isDuplicate = duplicate;
    }

    public void setPlayer(Player player) {
        this.player = player;
    }

    public void setGameRound(GameRound gameRound) {
        this.gameRound = gameRound;
    }
}