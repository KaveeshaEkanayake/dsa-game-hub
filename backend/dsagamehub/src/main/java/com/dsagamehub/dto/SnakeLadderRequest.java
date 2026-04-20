/*package com.dsagamehub.dto;

public class SnakeLadderRequest {
    private int boardSize;

    public int getBoardSize() { return boardSize; }
    public void setBoardSize(int boardSize) { this.boardSize = boardSize; }
}*/

package com.dsagamehub.dto;

public class SnakeLadderRequest {
    private int boardSize;
    private String playerName;  // Added for frontend

    public int getBoardSize() { return boardSize; }
    public void setBoardSize(int boardSize) { this.boardSize = boardSize; }

    public String getPlayerName() { return playerName; }
    public void setPlayerName(String playerName) { this.playerName = playerName; }
}

