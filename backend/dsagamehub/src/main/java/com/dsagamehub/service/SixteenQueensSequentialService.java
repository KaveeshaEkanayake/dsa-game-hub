package com.dsagamehub.service;

import org.springframework.stereotype.Service;

@Service
public class SixteenQueensSequentialService {

    private static final int SIZE = 16;

    public long countQueens() {
        int[] board = new int[SIZE];
        return solveRow(0, board);
    }

    private long solveRow(int row, int[] board) {
        if (row == SIZE) {
            return 1;
        }

        long count = 0;

        for (int col = 0; col < SIZE; col++) {
            if (isSafe(row, col, board)) {
                board[row] = col;
                count += solveRow(row + 1, board);
            }
        }

        return count;
    }

    private boolean isSafe(int row, int col, int[] board) {
        for (int i = 0; i < row; i++) {
            int existingCol = board[i];

            if (existingCol == col) {
                return false;
            }

            if (Math.abs(existingCol - col) == Math.abs(i - row)) {
                return false;
            }
        }

        return true;
    }
}