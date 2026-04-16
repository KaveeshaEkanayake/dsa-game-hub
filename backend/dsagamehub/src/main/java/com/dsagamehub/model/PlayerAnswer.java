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

    private boolean correct;
    private boolean recognized;
    private String message;
    private LocalDateTime submittedAt;

    public PlayerAnswer() {
    }

    public PlayerAnswer(String playerName, String answerText, boolean correct, boolean recognized, String message) {
        this.playerName = playerName;
        this.answerText = answerText;
        this.correct = correct;
        this.recognized = recognized;
        this.message = message;
        this.submittedAt = LocalDateTime.now();
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

    public boolean isCorrect() {
        return correct;
    }

    public void setCorrect(boolean correct) {
        this.correct = correct;
    }

    public boolean isRecognized() {
        return recognized;
    }

    public void setRecognized(boolean recognized) {
        this.recognized = recognized;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public LocalDateTime getSubmittedAt() {
        return submittedAt;
    }
}