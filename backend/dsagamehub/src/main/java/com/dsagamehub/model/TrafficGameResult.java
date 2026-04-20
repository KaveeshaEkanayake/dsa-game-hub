package com.dsagamehub.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "traffic_game_results")
public class TrafficGameResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "player_name", nullable = false)
    private String playerName;

    @Column(name = "player_answer", nullable = false)
    private int playerAnswer;

    @Column(name = "correct_max_flow", nullable = false)
    private int correctMaxFlow;

    @Column(name = "is_correct", nullable = false)
    private boolean isCorrect;

    @Column(name = "result", nullable = false)
    private String result;

    @Column(name = "round_id", nullable = false)
    private Long roundId;

    @Column(name = "ford_fulkerson_time_ms", nullable = false)
    private long fordFulkersonTimeMs;

    @Column(name = "edmonds_karp_time_ms", nullable = false)
    private long edmondsKarpTimeMs;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    public TrafficGameResult() {}

    public TrafficGameResult(String playerName, int playerAnswer, int correctMaxFlow,
                             boolean isCorrect, String result, Long roundId,
                             long fordFulkersonTimeMs, long edmondsKarpTimeMs) {
        this.playerName = playerName;
        this.playerAnswer = playerAnswer;
        this.correctMaxFlow = correctMaxFlow;
        this.isCorrect = isCorrect;
        this.result = result;
        this.roundId = roundId;
        this.fordFulkersonTimeMs = fordFulkersonTimeMs;
        this.edmondsKarpTimeMs = edmondsKarpTimeMs;
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getPlayerName() { return playerName; }
    public void setPlayerName(String playerName) { this.playerName = playerName; }

    public int getPlayerAnswer() { return playerAnswer; }
    public void setPlayerAnswer(int playerAnswer) { this.playerAnswer = playerAnswer; }

    public int getCorrectMaxFlow() { return correctMaxFlow; }
    public void setCorrectMaxFlow(int correctMaxFlow) { this.correctMaxFlow = correctMaxFlow; }

    public boolean isCorrect() { return isCorrect; }
    public void setCorrect(boolean correct) { isCorrect = correct; }

    public String getResult() { return result; }
    public void setResult(String result) { this.result = result; }

    public Long getRoundId() { return roundId; }
    public void setRoundId(Long roundId) { this.roundId = roundId; }

    public long getFordFulkersonTimeMs() { return fordFulkersonTimeMs; }
    public void setFordFulkersonTimeMs(long fordFulkersonTimeMs) { this.fordFulkersonTimeMs = fordFulkersonTimeMs; }

    public long getEdmondsKarpTimeMs() { return edmondsKarpTimeMs; }
    public void setEdmondsKarpTimeMs(long edmondsKarpTimeMs) { this.edmondsKarpTimeMs = edmondsKarpTimeMs; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}