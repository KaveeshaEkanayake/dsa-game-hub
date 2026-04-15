package com.dsagamehub.service;

import com.dsagamehub.model.KnightTourResult;
import com.dsagamehub.repository.KnightTourRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.*;

@Service
public class KnightTourService {

    @Autowired
    private KnightTourRepository knightTourRepository;

    private static final int[] ROW_MOVES = {-2,-1,1,2,2,1,-1,-2};
    private static final int[] COL_MOVES = {1,2,2,1,-1,-2,-2,-1};

    public Map<String, Object> generateGame(int boardSize) {
        Random rand = new Random();
        int startRow = rand.nextInt(boardSize);
        int startCol = rand.nextInt(boardSize);

        // Algorithm 1 — Warnsdorff's (Iterative)
        long start1 = System.currentTimeMillis();
        List<int[]> solution1 = warnsdorff(boardSize, startRow, startCol);
        long algo1Time = System.currentTimeMillis() - start1;

        // Algorithm 2 — Backtracking (Recursive, 8x8 only, 5 second timeout)
        long algo2Time = 0;
        if (boardSize == 8) {
            int[][] board2 = new int[boardSize][boardSize];
            List<int[]> solution2 = Collections.synchronizedList(new ArrayList<>());
            ExecutorService executor = Executors.newSingleThreadExecutor();
            long start2 = System.currentTimeMillis();
            Future<?> future = executor.submit(() ->
                    backtrack(board2, startRow, startCol, 1, boardSize, solution2)
            );
            try {
                future.get(5, TimeUnit.SECONDS);
            } catch (TimeoutException e) {
                future.cancel(true);
            } catch (Exception e) {
                // ignore
            } finally {
                executor.shutdownNow();
            }
            algo2Time = System.currentTimeMillis() - start2;
        }

        Map<String, Object> response = new HashMap<>();
        response.put("startRow", startRow);
        response.put("startCol", startCol);
        response.put("boardSize", boardSize);
        response.put("totalMoves", boardSize * boardSize);
        response.put("algo1TimeMs", algo1Time);
        response.put("algo2TimeMs", algo2Time);
        response.put("solution", solution1);
        return response;
    }

    public Map<String, Object> submitAnswer(Map<String, Object> request) {
        String playerName = (String) request.get("playerName");
        int boardSize = (Integer) request.get("boardSize");
        int startRow = (Integer) request.get("startRow");
        int startCol = (Integer) request.get("startCol");
        String playerAnswer = (String) request.get("playerAnswer");

        // Get algo times sent from frontend
        long algo1TimeMs = 0L;
        long algo2TimeMs = 0L;
        if (request.get("algo1TimeMs") != null) {
            algo1TimeMs = ((Number) request.get("algo1TimeMs")).longValue();
        }
        if (request.get("algo2TimeMs") != null) {
            algo2TimeMs = ((Number) request.get("algo2TimeMs")).longValue();
        }

        int correctAnswer = boardSize * boardSize;
        boolean isCorrect = playerAnswer.trim().equals(String.valueOf(correctAnswer));

        // Always save to DB (both correct and incorrect) for timing data
        KnightTourResult result = new KnightTourResult();
        result.setPlayerName(playerName);
        result.setBoardSize(boardSize);
        result.setStartRow(startRow);
        result.setStartCol(startCol);
        result.setCorrect(isCorrect);
        result.setAlgorithm1TimeMs(algo1TimeMs);
        result.setAlgorithm2TimeMs(algo2TimeMs);
        knightTourRepository.save(result);

        Map<String, Object> response = new HashMap<>();
        response.put("correct", isCorrect);
        response.put("correctAnswer", correctAnswer);
        response.put("playerName", playerName);
        return response;
    }

    private List<int[]> warnsdorff(int size, int row, int col) {
        int[][] board = new int[size][size];
        List<int[]> path = new ArrayList<>();
        board[row][col] = 1;
        path.add(new int[]{row, col});
        int curRow = row, curCol = col;

        for (int move = 2; move <= size * size; move++) {
            int[] next = getWarnsdorffNext(board, curRow, curCol, size);
            if (next == null) break;
            curRow = next[0]; curCol = next[1];
            board[curRow][curCol] = move;
            path.add(new int[]{curRow, curCol});
        }
        return path;
    }

    private int[] getWarnsdorffNext(int[][] board, int row, int col, int size) {
        int minDegree = Integer.MAX_VALUE;
        int[] bestMove = null;
        for (int i = 0; i < 8; i++) {
            int nr = row + ROW_MOVES[i];
            int nc = col + COL_MOVES[i];
            if (isValid(nr, nc, size, board)) {
                int degree = getDegree(board, nr, nc, size);
                if (degree < minDegree) {
                    minDegree = degree;
                    bestMove = new int[]{nr, nc};
                }
            }
        }
        return bestMove;
    }

    private int getDegree(int[][] board, int row, int col, int size) {
        int count = 0;
        for (int i = 0; i < 8; i++) {
            int nr = row + ROW_MOVES[i];
            int nc = col + COL_MOVES[i];
            if (isValid(nr, nc, size, board)) count++;
        }
        return count;
    }

    private boolean backtrack(int[][] board, int row, int col, int moveNum, int size, List<int[]> path) {
        if (Thread.currentThread().isInterrupted()) return false;
        board[row][col] = moveNum;
        path.add(new int[]{row, col});
        if (moveNum == size * size) return true;
        for (int i = 0; i < 8; i++) {
            int nr = row + ROW_MOVES[i];
            int nc = col + COL_MOVES[i];
            if (isValid(nr, nc, size, board)) {
                if (backtrack(board, nr, nc, moveNum + 1, size, path)) return true;
            }
        }
        board[row][col] = 0;
        path.remove(path.size() - 1);
        return false;
    }

    private boolean isValid(int row, int col, int size, int[][] board) {
        return row >= 0 && col >= 0 && row < size && col < size && board[row][col] == 0;
    }
}