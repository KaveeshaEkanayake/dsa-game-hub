/*package com.dsagamehub.dto;

import java.util.List;

public class SnakeLadderResponse {

    private Long roundId;
    private int boardSize;
    private List<Integer> options;
    private int correctAnswer;
    private Object snakes;
    private Object ladders;

    public SnakeLadderResponse() {}

    public SnakeLadderResponse(Long roundId, int boardSize,
                               int correctAnswer,
                               List<Integer> options,
                               Object snakes,
                               Object ladders) {
        this.roundId = roundId;
        this.boardSize = boardSize;
        this.correctAnswer = correctAnswer;
        this.options = options;
        this.snakes = snakes;
        this.ladders = ladders;
    }

    public Long getRoundId() { return roundId; }
    public int getBoardSize() { return boardSize; }
    public List<Integer> getOptions() { return options; }
    public int getCorrectAnswer() { return correctAnswer; }
    public Object getSnakes() { return snakes; }
    public Object getLadders() { return ladders; }

    public void setRoundId(Long roundId) { this.roundId = roundId; }
    public void setBoardSize(int boardSize) { this.boardSize = boardSize; }
    public void setOptions(List<Integer> options) { this.options = options; }
    public void setCorrectAnswer(int correctAnswer) { this.correctAnswer = correctAnswer; }
    public void setSnakes(Object snakes) { this.snakes = snakes; }
    public void setLadders(Object ladders) { this.ladders = ladders; }
}*/

package com.dsagamehub.dto;

import java.util.List;
import java.util.Map;

public class SnakeLadderResponse {

    private Long roundId;
    private int boardSize;
    private List<Integer> options;
    private int correctAnswer;
    private Map<Integer, Integer> snakes;
    private Map<Integer, Integer> ladders;
    private long bfsTimeMs;
    private long dpTimeMs;

    public SnakeLadderResponse() {}

    public SnakeLadderResponse(Long roundId, int boardSize,
                               int correctAnswer,
                               List<Integer> options,
                               Map<Integer, Integer> snakes,
                               Map<Integer, Integer> ladders,
                               long bfsTimeMs,
                               long dpTimeMs) {
        this.roundId = roundId;
        this.boardSize = boardSize;
        this.correctAnswer = correctAnswer;
        this.options = options;
        this.snakes = snakes;
        this.ladders = ladders;
        this.bfsTimeMs = bfsTimeMs;
        this.dpTimeMs = dpTimeMs;
    }

    // Getters
    public Long getRoundId() { return roundId; }
    public int getBoardSize() { return boardSize; }
    public List<Integer> getOptions() { return options; }
    public int getCorrectAnswer() { return correctAnswer; }
    public Map<Integer, Integer> getSnakes() { return snakes; }
    public Map<Integer, Integer> getLadders() { return ladders; }
    public long getBfsTimeMs() { return bfsTimeMs; }
    public long getDpTimeMs() { return dpTimeMs; }

    // Setters
    public void setRoundId(Long roundId) { this.roundId = roundId; }
    public void setBoardSize(int boardSize) { this.boardSize = boardSize; }
    public void setOptions(List<Integer> options) { this.options = options; }
    public void setCorrectAnswer(int correctAnswer) { this.correctAnswer = correctAnswer; }
    public void setSnakes(Map<Integer, Integer> snakes) { this.snakes = snakes; }
    public void setLadders(Map<Integer, Integer> ladders) { this.ladders = ladders; }
    public void setBfsTimeMs(long bfsTimeMs) { this.bfsTimeMs = bfsTimeMs; }
    public void setDpTimeMs(long dpTimeMs) { this.dpTimeMs = dpTimeMs; }
}