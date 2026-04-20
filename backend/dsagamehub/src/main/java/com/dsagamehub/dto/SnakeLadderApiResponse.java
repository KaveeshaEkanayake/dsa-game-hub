package com.dsagamehub.dto;

public class SnakeLadderApiResponse {

    private boolean success;
    private String message;
    private boolean correct;
    private boolean alreadyFound;
    private long bfsTimeMs;
    private long dpTimeMs;
    private long totalTimeMs;
    private String bestAlgorithm;
    private String comparisonMessage;
    private boolean allSolutionsIdentified;

    // Default constructor
    public SnakeLadderApiResponse() {
    }

    // Simple constructor (for errors)
    public SnakeLadderApiResponse(boolean success, String message) {
        this.success = success;
        this.message = message;
        this.correct = false;
        this.alreadyFound = false;
        this.bfsTimeMs = 0;
        this.dpTimeMs = 0;
        this.totalTimeMs = 0;
        this.bestAlgorithm = null;
        this.comparisonMessage = null;
        this.allSolutionsIdentified = false;
    }

    // Full constructor for correct answers
    public SnakeLadderApiResponse(boolean success, String message, boolean correct,
                                  boolean alreadyFound, long bfsTimeMs, long dpTimeMs,
                                  long totalTimeMs, String bestAlgorithm,
                                  String comparisonMessage, boolean allSolutionsIdentified) {
        this.success = success;
        this.message = message;
        this.correct = correct;
        this.alreadyFound = alreadyFound;
        this.bfsTimeMs = bfsTimeMs;
        this.dpTimeMs = dpTimeMs;
        this.totalTimeMs = totalTimeMs;
        this.bestAlgorithm = bestAlgorithm;
        this.comparisonMessage = comparisonMessage;
        this.allSolutionsIdentified = allSolutionsIdentified;
    }

    // Getters
    public boolean isSuccess() { return success; }
    public String getMessage() { return message; }
    public boolean isCorrect() { return correct; }
    public boolean isAlreadyFound() { return alreadyFound; }
    public long getBfsTimeMs() { return bfsTimeMs; }
    public long getDpTimeMs() { return dpTimeMs; }
    public long getTotalTimeMs() { return totalTimeMs; }
    public String getBestAlgorithm() { return bestAlgorithm; }
    public String getComparisonMessage() { return comparisonMessage; }
    public boolean isAllSolutionsIdentified() { return allSolutionsIdentified; }

    // Setters
    public void setSuccess(boolean success) { this.success = success; }
    public void setMessage(String message) { this.message = message; }
    public void setCorrect(boolean correct) { this.correct = correct; }
    public void setAlreadyFound(boolean alreadyFound) { this.alreadyFound = alreadyFound; }
    public void setBfsTimeMs(long bfsTimeMs) { this.bfsTimeMs = bfsTimeMs; }
    public void setDpTimeMs(long dpTimeMs) { this.dpTimeMs = dpTimeMs; }
    public void setTotalTimeMs(long totalTimeMs) { this.totalTimeMs = totalTimeMs; }
    public void setBestAlgorithm(String bestAlgorithm) { this.bestAlgorithm = bestAlgorithm; }
    public void setComparisonMessage(String comparisonMessage) { this.comparisonMessage = comparisonMessage; }
    public void setAllSolutionsIdentified(boolean allSolutionsIdentified) { this.allSolutionsIdentified = allSolutionsIdentified; }
}