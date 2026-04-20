package com.dsagamehub.dto;

import java.time.LocalDateTime;
import java.util.List;

public class MinimumCostRoundResponse {

    private Long roundId;
    private int roundNumber;
    private int taskCount;
    private String generatedMode;
    private CostMatrixSummary costMatrixSummary;
    private AlgorithmResult algorithm1;
    private AlgorithmResult algorithm2;
    private boolean sameMinimumCost;
    private String fasterAlgorithm;
    private String comparisonMessage;
    private LocalDateTime createdAt;

    public MinimumCostRoundResponse() {
    }

    public MinimumCostRoundResponse(Long roundId,
                                    int roundNumber,
                                    int taskCount,
                                    String generatedMode,
                                    CostMatrixSummary costMatrixSummary,
                                    AlgorithmResult algorithm1,
                                    AlgorithmResult algorithm2,
                                    boolean sameMinimumCost,
                                    String fasterAlgorithm,
                                    String comparisonMessage,
                                    LocalDateTime createdAt) {
        this.roundId = roundId;
        this.roundNumber = roundNumber;
        this.taskCount = taskCount;
        this.generatedMode = generatedMode;
        this.costMatrixSummary = costMatrixSummary;
        this.algorithm1 = algorithm1;
        this.algorithm2 = algorithm2;
        this.sameMinimumCost = sameMinimumCost;
        this.fasterAlgorithm = fasterAlgorithm;
        this.comparisonMessage = comparisonMessage;
        this.createdAt = createdAt;
    }

    public Long getRoundId() {
        return roundId;
    }

    public void setRoundId(Long roundId) {
        this.roundId = roundId;
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

    public String getGeneratedMode() {
        return generatedMode;
    }

    public void setGeneratedMode(String generatedMode) {
        this.generatedMode = generatedMode;
    }

    public CostMatrixSummary getCostMatrixSummary() {
        return costMatrixSummary;
    }

    public void setCostMatrixSummary(CostMatrixSummary costMatrixSummary) {
        this.costMatrixSummary = costMatrixSummary;
    }

    public AlgorithmResult getAlgorithm1() {
        return algorithm1;
    }

    public void setAlgorithm1(AlgorithmResult algorithm1) {
        this.algorithm1 = algorithm1;
    }

    public AlgorithmResult getAlgorithm2() {
        return algorithm2;
    }

    public void setAlgorithm2(AlgorithmResult algorithm2) {
        this.algorithm2 = algorithm2;
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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public static class CostMatrixSummary {
        private int minimumValue;
        private int maximumValue;
        private double averageValue;

        public CostMatrixSummary() {
        }

        public CostMatrixSummary(int minimumValue, int maximumValue, double averageValue) {
            this.minimumValue = minimumValue;
            this.maximumValue = maximumValue;
            this.averageValue = averageValue;
        }

        public int getMinimumValue() {
            return minimumValue;
        }

        public void setMinimumValue(int minimumValue) {
            this.minimumValue = minimumValue;
        }

        public int getMaximumValue() {
            return maximumValue;
        }

        public void setMaximumValue(int maximumValue) {
            this.maximumValue = maximumValue;
        }

        public double getAverageValue() {
            return averageValue;
        }

        public void setAverageValue(double averageValue) {
            this.averageValue = averageValue;
        }
    }

    public static class AlgorithmResult {
        private String algorithmName;
        private String logicUsed;
        private long minimumTotalCost;
        private long timeTakenMs;
        private boolean validAssignment;
        private List<AssignmentItem> assignments;

        public AlgorithmResult() {
        }

        public AlgorithmResult(String algorithmName,
                               String logicUsed,
                               long minimumTotalCost,
                               long timeTakenMs,
                               boolean validAssignment,
                               List<AssignmentItem> assignments) {
            this.algorithmName = algorithmName;
            this.logicUsed = logicUsed;
            this.minimumTotalCost = minimumTotalCost;
            this.timeTakenMs = timeTakenMs;
            this.validAssignment = validAssignment;
            this.assignments = assignments;
        }

        public String getAlgorithmName() {
            return algorithmName;
        }

        public void setAlgorithmName(String algorithmName) {
            this.algorithmName = algorithmName;
        }

        public String getLogicUsed() {
            return logicUsed;
        }

        public void setLogicUsed(String logicUsed) {
            this.logicUsed = logicUsed;
        }

        public long getMinimumTotalCost() {
            return minimumTotalCost;
        }

        public void setMinimumTotalCost(long minimumTotalCost) {
            this.minimumTotalCost = minimumTotalCost;
        }

        public long getTimeTakenMs() {
            return timeTakenMs;
        }

        public void setTimeTakenMs(long timeTakenMs) {
            this.timeTakenMs = timeTakenMs;
        }

        public boolean isValidAssignment() {
            return validAssignment;
        }

        public void setValidAssignment(boolean validAssignment) {
            this.validAssignment = validAssignment;
        }

        public List<AssignmentItem> getAssignments() {
            return assignments;
        }

        public void setAssignments(List<AssignmentItem> assignments) {
            this.assignments = assignments;
        }
    }

    public static class AssignmentItem {
        private int taskNumber;
        private int employeeNumber;
        private int cost;

        public AssignmentItem() {
        }

        public AssignmentItem(int taskNumber, int employeeNumber, int cost) {
            this.taskNumber = taskNumber;
            this.employeeNumber = employeeNumber;
            this.cost = cost;
        }

        public int getTaskNumber() {
            return taskNumber;
        }

        public void setTaskNumber(int taskNumber) {
            this.taskNumber = taskNumber;
        }

        public int getEmployeeNumber() {
            return employeeNumber;
        }

        public void setEmployeeNumber(int employeeNumber) {
            this.employeeNumber = employeeNumber;
        }

        public int getCost() {
            return cost;
        }

        public void setCost(int cost) {
            this.cost = cost;
        }
    }
}