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

        // Ensure at least 1ms recorded
        if (algo1Time == 0) algo1Time = 1;

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
            if (algo2Time == 0) algo2Time = 1;
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
        int boardSize = ((Number) request.get("boardSize")).intValue();
        int startRow = ((Number) request.get("startRow")).intValue();
        int startCol = ((Number) request.get("startCol")).intValue();
        String playerAnswer = request.get("playerAnswer").toString();

        long algo1TimeMs = request.get("algo1TimeMs") != null ?
                ((Number) request.get("algo1TimeMs")).longValue() : 0L;
        long algo2TimeMs = request.get("algo2TimeMs") != null ?
                ((Number) request.get("algo2TimeMs")).longValue() : 0L;

        int correctAnswer = boardSize * boardSize;
        boolean isCorrect = playerAnswer.trim().equals(String.valueOf(correctAnswer));

        // Save to DB
        KnightTourResult result = new KnightTourResult();
        result.setPlayerName(playerName.trim());
        result.setBoardSize(boardSize);
        result.setStartRow(startRow);
        result.setStartCol(startCol);
        result.setCorrect(isCorrect);
        result.setGameResult(isCorrect ? "win" : "lose");
        result.setAlgorithm1TimeMs(algo1TimeMs);
        result.setAlgorithm2TimeMs(algo2TimeMs);
        knightTourRepository.save(result);

        Map<String, Object> response = new HashMap<>();
        response.put("correct", isCorrect);
        response.put("correctAnswer", correctAnswer);
        response.put("playerName", playerName);
        response.put("gameResult", isCorrect ? "win" : "lose");
        response.put("algo1TimeMs", algo1TimeMs);
        response.put("algo2TimeMs", algo2TimeMs);
        return response;
    }

    public Map<String, Object> recordDraw(Map<String, Object> request) {
        String playerName = (String) request.get("playerName");
        int boardSize = request.get("boardSize") != null ?
                ((Number) request.get("boardSize")).intValue() : 8;
        int startRow = request.get("startRow") != null ?
                ((Number) request.get("startRow")).intValue() : 0;
        int startCol = request.get("startCol") != null ?
                ((Number) request.get("startCol")).intValue() : 0;

        long algo1TimeMs = request.get("algo1TimeMs") != null ?
                ((Number) request.get("algo1TimeMs")).longValue() : 0L;
        long algo2TimeMs = request.get("algo2TimeMs") != null ?
                ((Number) request.get("algo2TimeMs")).longValue() : 0L;

        KnightTourResult result = new KnightTourResult();
        result.setPlayerName(playerName.trim());
        result.setBoardSize(boardSize);
        result.setStartRow(startRow);
        result.setStartCol(startCol);
        result.setCorrect(false);
        result.setGameResult("draw");
        result.setAlgorithm1TimeMs(algo1TimeMs);
        result.setAlgorithm2TimeMs(algo2TimeMs);
        knightTourRepository.save(result);

        Map<String, Object> response = new HashMap<>();
        response.put("gameResult", "draw");
        response.put("playerName", playerName);
        response.put("correctAnswer", boardSize * boardSize);
        return response;
    }

    public List<Map<String, Object>> getLeaderboard() {
        List<KnightTourResult> correctResults = knightTourRepository.findTopCorrectResults();
        List<Map<String, Object>> leaderboard = new ArrayList<>();

        for (KnightTourResult r : correctResults) {
            Map<String, Object> entry = new HashMap<>();
            entry.put("playerName", r.getPlayerName());
            entry.put("boardSize", r.getBoardSize());
            entry.put("algo1TimeMs", r.getAlgorithm1TimeMs());
            entry.put("algo2TimeMs", r.getAlgorithm2TimeMs());
            entry.put("createdAt", r.getCreatedAt());
            leaderboard.add(entry);
        }
        return leaderboard;
    }

    public List<Map<String, Object>> getAllResults() {
        List<KnightTourResult> results = knightTourRepository.findAllOrderByCreatedAtDesc();
        List<Map<String, Object>> response = new ArrayList<>();

        for (KnightTourResult r : results) {
            Map<String, Object> entry = new HashMap<>();
            entry.put("id", r.getId());
            entry.put("playerName", r.getPlayerName());
            entry.put("boardSize", r.getBoardSize());
            entry.put("startRow", r.getStartRow());
            entry.put("startCol", r.getStartCol());
            entry.put("correct", r.isCorrect());
            entry.put("gameResult", r.getGameResult());
            entry.put("algo1TimeMs", r.getAlgorithm1TimeMs());
            entry.put("algo2TimeMs", r.getAlgorithm2TimeMs());
            entry.put("createdAt", r.getCreatedAt());
            response.add(entry);
        }
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

    private boolean backtrack(int[][] board, int row, int col, int moveNum,
                              int size, List<int[]> path) {
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