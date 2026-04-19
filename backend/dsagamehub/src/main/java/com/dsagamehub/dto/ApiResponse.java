package com.dsagamehub.dto;

public class ApiResponse {

    private boolean success;
    private String message;
    private boolean correct;
    private boolean alreadyFound;
    private long sequentialCheckTimeMs;
    private long threadedCheckTimeMs;
    private long totalCheckTimeMs;
    private String bestAlgorithm;
    private String comparisonMessage;
    private boolean allSolutionsIdentified;

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
                       long sequentialCheckTimeMs,
                       long threadedCheckTimeMs,
                       long totalCheckTimeMs,
                       String bestAlgorithm,
                       String comparisonMessage,
                       boolean allSolutionsIdentified) {
        this.success = success;
        this.message = message;
        this.correct = correct;
        this.alreadyFound = alreadyFound;
        this.sequentialCheckTimeMs = sequentialCheckTimeMs;
        this.threadedCheckTimeMs = threadedCheckTimeMs;
        this.totalCheckTimeMs = totalCheckTimeMs;
        this.bestAlgorithm = bestAlgorithm;
        this.comparisonMessage = comparisonMessage;
        this.allSolutionsIdentified = allSolutionsIdentified;
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

    public long getSequentialCheckTimeMs() {
        return sequentialCheckTimeMs;
    }

    public long getThreadedCheckTimeMs() {
        return threadedCheckTimeMs;
    }

    public long getTotalCheckTimeMs() {
        return totalCheckTimeMs;
    }

    public String getBestAlgorithm() {
        return bestAlgorithm;
    }

    public String getComparisonMessage() {
        return comparisonMessage;
    }

    public boolean isAllSolutionsIdentified() {
        return allSolutionsIdentified;
    }
}

