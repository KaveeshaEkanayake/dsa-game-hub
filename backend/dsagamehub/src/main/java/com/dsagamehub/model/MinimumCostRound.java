package com.dsagamehub.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "minimum_cost_rounds")
public class MinimumCostRound {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private int roundNumber;
    private int taskCount;

    private long hungarianMinimumCost;
    private long hungarianTimeMs;

    @Lob
    @Column(columnDefinition = "LONGTEXT")
    private String hungarianAssignmentSummary;

    private long minCostFlowMinimumCost;
    private long minCostFlowTimeMs;

    @Lob
    @Column(columnDefinition = "LONGTEXT")
    private String minCostFlowAssignmentSummary;

    private boolean sameMinimumCost;
    private String fasterAlgorithm;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String comparisonMessage;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String costMatrixSummary;

    private LocalDateTime createdAt;

    public MinimumCostRound() {
    }

    public MinimumCostRound(int roundNumber,
                            int taskCount,
                            long hungarianMinimumCost,
                            long hungarianTimeMs,
                            String hungarianAssignmentSummary,
                            long minCostFlowMinimumCost,
                            long minCostFlowTimeMs,
                            String minCostFlowAssignmentSummary,
                            boolean sameMinimumCost,
                            String fasterAlgorithm,
                            String comparisonMessage,
                            String costMatrixSummary) {
        this.roundNumber = roundNumber;
        this.taskCount = taskCount;
        this.hungarianMinimumCost = hungarianMinimumCost;
        this.hungarianTimeMs = hungarianTimeMs;
        this.hungarianAssignmentSummary = hungarianAssignmentSummary;
        this.minCostFlowMinimumCost = minCostFlowMinimumCost;
        this.minCostFlowTimeMs = minCostFlowTimeMs;
        this.minCostFlowAssignmentSummary = minCostFlowAssignmentSummary;
        this.sameMinimumCost = sameMinimumCost;
        this.fasterAlgorithm = fasterAlgorithm;
        this.comparisonMessage = comparisonMessage;
        this.costMatrixSummary = costMatrixSummary;
        this.createdAt = LocalDateTime.now();
    }

    @PrePersist
    public void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public int getRoundNumber() {
        return roundNumber;
    }

    public void setRoundNumber(int roundNumber) {
        this.roundNumber = roundNumber;
    }

    public int getTaskCount() {
        return taskCount;
    }

    public void setTaskCount(int taskCount) {
        this.taskCount = taskCount;
    }

    public long getHungarianMinimumCost() {
        return hungarianMinimumCost;
    }

    public void setHungarianMinimumCost(long hungarianMinimumCost) {
        this.hungarianMinimumCost = hungarianMinimumCost;
    }

    public long getHungarianTimeMs() {
        return hungarianTimeMs;
    }

    public void setHungarianTimeMs(long hungarianTimeMs) {
        this.hungarianTimeMs = hungarianTimeMs;
    }

    public String getHungarianAssignmentSummary() {
        return hungarianAssignmentSummary;
    }

    public void setHungarianAssignmentSummary(String hungarianAssignmentSummary) {
        this.hungarianAssignmentSummary = hungarianAssignmentSummary;
    }

    public long getMinCostFlowMinimumCost() {
        return minCostFlowMinimumCost;
    }

    public void setMinCostFlowMinimumCost(long minCostFlowMinimumCost) {
        this.minCostFlowMinimumCost = minCostFlowMinimumCost;
    }

    public long getMinCostFlowTimeMs() {
        return minCostFlowTimeMs;
    }

    public void setMinCostFlowTimeMs(long minCostFlowTimeMs) {
        this.minCostFlowTimeMs = minCostFlowTimeMs;
    }

    public String getMinCostFlowAssignmentSummary() {
        return minCostFlowAssignmentSummary;
    }

    public void setMinCostFlowAssignmentSummary(String minCostFlowAssignmentSummary) {
        this.minCostFlowAssignmentSummary = minCostFlowAssignmentSummary;
    }

    public boolean isSameMinimumCost() {
        return sameMinimumCost;
    }

    public void setSameMinimumCost(boolean sameMinimumCost) {
        this.sameMinimumCost = sameMinimumCost;
    }

    public String getFasterAlgorithm() {
        return fasterAlgorithm;
    }

    public void setFasterAlgorithm(String fasterAlgorithm) {
        this.fasterAlgorithm = fasterAlgorithm;
    }

    public String getComparisonMessage() {
        return comparisonMessage;
    }

    public void setComparisonMessage(String comparisonMessage) {
        this.comparisonMessage = comparisonMessage;
    }

    public String getCostMatrixSummary() {
        return costMatrixSummary;
    }

    public void setCostMatrixSummary(String costMatrixSummary) {
        this.costMatrixSummary = costMatrixSummary;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}