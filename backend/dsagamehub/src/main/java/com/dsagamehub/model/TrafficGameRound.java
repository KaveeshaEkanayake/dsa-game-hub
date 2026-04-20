package com.dsagamehub.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "traffic_game_rounds")
public class TrafficGameRound {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "correct_max_flow")
    private int correctMaxFlow;

    @Column(name = "ford_fulkerson_time_ms")
    private long fordFulkersonTimeMs;

    @Column(name = "edmonds_karp_time_ms")
    private long edmondsKarpTimeMs;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    public TrafficGameRound() {}

    public TrafficGameRound(int correctMaxFlow, long fordFulkersonTimeMs, long edmondsKarpTimeMs) {
        this.correctMaxFlow = correctMaxFlow;
        this.fordFulkersonTimeMs = fordFulkersonTimeMs;
        this.edmondsKarpTimeMs = edmondsKarpTimeMs;
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public int getCorrectMaxFlow() { return correctMaxFlow; }
    public void setCorrectMaxFlow(int correctMaxFlow) { this.correctMaxFlow = correctMaxFlow; }

    public long getFordFulkersonTimeMs() { return fordFulkersonTimeMs; }
    public void setFordFulkersonTimeMs(long fordFulkersonTimeMs) { this.fordFulkersonTimeMs = fordFulkersonTimeMs; }

    public long getEdmondsKarpTimeMs() { return edmondsKarpTimeMs; }
    public void setEdmondsKarpTimeMs(long edmondsKarpTimeMs) { this.edmondsKarpTimeMs = edmondsKarpTimeMs; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}