/*package com.dsagamehub.dto;

import java.util.List;

public class SnakeLadderResponse {

    private Long roundId;
    private int boardSize;
    private List<Integer> options;
    private int correctAnswer;
    private Object snakes;
    private Object ladders;

    // ✅ DEFAULT CONSTRUCTOR (REQUIRED for Jackson)
    public SnakeLadderResponse() {
    }

    // Parameterized constructor
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

    // ✅ GETTERS (ALL fields need getters)
    public Long getRoundId() {
        return roundId;
    }

    public int getBoardSize() {
        return boardSize;
    }

    public List<Integer> getOptions() {
        return options;
    }

    public int getCorrectAnswer() {
        return correctAnswer;
    }

    public Object getSnakes() {
        return snakes;
    }

    public Object getLadders() {
        return ladders;
    }

    // ✅ SETTERS (ADD THESE - also important)
    public void setRoundId(Long roundId) {
        this.roundId = roundId;
    }

    public void setBoardSize(int boardSize) {
        this.boardSize = boardSize;
    }

    public void setOptions(List<Integer> options) {
        this.options = options;
    }

    public void setCorrectAnswer(int correctAnswer) {
        this.correctAnswer = correctAnswer;
    }

    public void setSnakes(Object snakes) {
        this.snakes = snakes;
    }

    public void setLadders(Object ladders) {
        this.ladders = ladders;
    }
}*/

package com.dsagamehub.dto;

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
}