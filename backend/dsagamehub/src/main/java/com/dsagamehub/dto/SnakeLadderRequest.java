package com.dsagamehub.dto;

public class SnakeLadderRequest {
    private int boardSize;

    public int getBoardSize() { return boardSize; }
    public void setBoardSize(int boardSize) { this.boardSize = boardSize; }
}

/*package com.dsagamehub.dto;

public class SnakeLadderRequest {
    private int boardSize;

    // ✅ Default constructor
    public SnakeLadderRequest() {
    }

    // Parameterized constructor (optional)
    public SnakeLadderRequest(int boardSize) {
        this.boardSize = boardSize;
    }

    // Getter
    public int getBoardSize() {
        return boardSize;
    }

    // Setter
    public void setBoardSize(int boardSize) {
        this.boardSize = boardSize;
    }
}*/