package com.dsagamehub.dto;

public class ApiResponse {

    private boolean success;
    private String message;
    private boolean correct;
    private boolean alreadyFound;

    private long sequentialCheckTimeNs;
    private long threadedCheckTimeNs;
    private long totalCheckTimeNs;

    private String bestAlgorithm;
    private String comparisonMessage;

    private int roundNumber;
    private long recognizedSolutionCount;
    private long targetSolutionCount;
    private boolean allSolutionsIdentified;
    private boolean roundResetForNextCycle;

    public ApiResponse() {
    }

    public ApiResponse(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    public ApiResponse(boolean success,
                       String message,
                       boolean correct,
                       boolean alreadyFound,
                       long sequentialCheckTimeNs,
                       long threadedCheckTimeNs,
                       long totalCheckTimeNs,
                       String bestAlgorithm,
                       String comparisonMessage,
                       int roundNumber,
                       long recognizedSolutionCount,
                       long targetSolutionCount,
                       boolean allSolutionsIdentified,
                       boolean roundResetForNextCycle) {
        this.success = success;
        this.message = message;
        this.correct = correct;
        this.alreadyFound = alreadyFound;
        this.sequentialCheckTimeNs = sequentialCheckTimeNs;
        this.threadedCheckTimeNs = threadedCheckTimeNs;
        this.totalCheckTimeNs = totalCheckTimeNs;
        this.bestAlgorithm = bestAlgorithm;
        this.comparisonMessage = comparisonMessage;
        this.roundNumber = roundNumber;
        this.recognizedSolutionCount = recognizedSolutionCount;
        this.targetSolutionCount = targetSolutionCount;
        this.allSolutionsIdentified = allSolutionsIdentified;
        this.roundResetForNextCycle = roundResetForNextCycle;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public boolean isCorrect() {
        return correct;
    }

    public boolean isAlreadyFound() {
        return alreadyFound;
    }

    public long getSequentialCheckTimeNs() {
        return sequentialCheckTimeNs;
    }

    public long getThreadedCheckTimeNs() {
        return threadedCheckTimeNs;
    }

    public long getTotalCheckTimeNs() {
        return totalCheckTimeNs;
    }

    public String getBestAlgorithm() {
        return bestAlgorithm;
    }

    public String getComparisonMessage() {
        return comparisonMessage;
    }

    public int getRoundNumber() {
        return roundNumber;
    }

    public long getRecognizedSolutionCount() {
        return recognizedSolutionCount;
    }

    public long getTargetSolutionCount() {
        return targetSolutionCount;
    }

    public boolean isAllSolutionsIdentified() {
        return allSolutionsIdentified;
    }

    public boolean isRoundResetForNextCycle() {
        return roundResetForNextCycle;
    }
}