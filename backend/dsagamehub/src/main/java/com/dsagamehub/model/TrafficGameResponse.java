package com.dsagamehub.model;

import java.util.List;
import java.util.Map;

public class TrafficGameResponse {

    private Long roundId;
    private List<TrafficEdge> edges;
    private Map<String, Integer> capacities;
    private int correctMaxFlow;
    private long fordFulkersonTimeMs;
    private long edmondsKarpTimeMs;
    private boolean isCorrect;
    private String message;
    private String playerName;

    public TrafficGameResponse() {}

    public TrafficGameResponse(Long roundId, List<TrafficEdge> edges,
                               Map<String, Integer> capacities, int correctMaxFlow,
                               long fordFulkersonTimeMs, long edmondsKarpTimeMs) {
        this.roundId = roundId;
        this.edges = edges;
        this.capacities = capacities;
        this.correctMaxFlow = correctMaxFlow;
        this.fordFulkersonTimeMs = fordFulkersonTimeMs;
        this.edmondsKarpTimeMs = edmondsKarpTimeMs;
    }

    public Long getRoundId() { return roundId; }
    public void setRoundId(Long roundId) { this.roundId = roundId; }

    public List<TrafficEdge> getEdges() { return edges; }
    public void setEdges(List<TrafficEdge> edges) { this.edges = edges; }

    public Map<String, Integer> getCapacities() { return capacities; }
    public void setCapacities(Map<String, Integer> capacities) { this.capacities = capacities; }

    public int getCorrectMaxFlow() { return correctMaxFlow; }
    public void setCorrectMaxFlow(int correctMaxFlow) { this.correctMaxFlow = correctMaxFlow; }

    public long getFordFulkersonTimeMs() { return fordFulkersonTimeMs; }
    public void setFordFulkersonTimeMs(long fordFulkersonTimeMs) { this.fordFulkersonTimeMs = fordFulkersonTimeMs; }

    public long getEdmondsKarpTimeMs() { return edmondsKarpTimeMs; }
    public void setEdmondsKarpTimeMs(long edmondsKarpTimeMs) { this.edmondsKarpTimeMs = edmondsKarpTimeMs; }

    public boolean isCorrect() { return isCorrect; }
    public void setCorrect(boolean correct) { isCorrect = correct; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getPlayerName() { return playerName; }
    public void setPlayerName(String playerName) { this.playerName = playerName; }
}