package com.dsagamehub.service;

import com.dsagamehub.dto.*;
import com.dsagamehub.model.SnakeLadderGameResult;
import com.dsagamehub.repository.SnakeLadderRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class SnakeLadderService {

    private final SnakeLadderRepository snakeLadderResultRepo;
    private final ObjectMapper objectMapper;

    // Store board configurations per round (in-memory only)
    private final Map<Long, BoardConfig> boardConfigStore = new HashMap<>();
    // Store algorithm times per round (in-memory only)
    private final Map<Long, AlgorithmTimes> algorithmTimesStore = new HashMap<>();
    // Store correct answers per round
    private final Map<Long, Integer> answerStore = new HashMap<>();
    // Counter for generating round IDs
    private long roundIdCounter = 1;

    // For sequential round numbers per player
    private final Map<String, Integer> playerRoundCounters = new HashMap<>();

    public SnakeLadderService(SnakeLadderRepository snakeLadderResultRepo) {
        this.snakeLadderResultRepo = snakeLadderResultRepo;
        this.objectMapper = new ObjectMapper();
    }

    private static class BoardConfig {
        Map<Integer, Integer> snakes;
        Map<Integer, Integer> ladders;
        int boardSize;

        BoardConfig(Map<Integer, Integer> snakes, Map<Integer, Integer> ladders, int boardSize) {
            this.snakes = snakes;
            this.ladders = ladders;
            this.boardSize = boardSize;
        }
    }

    private static class AlgorithmTimes {
        long bfsTimeMicro;
        long dpTimeMicro;

        AlgorithmTimes(long bfsTimeMicro, long dpTimeMicro) {
            this.bfsTimeMicro = bfsTimeMicro;
            this.dpTimeMicro = dpTimeMicro;
        }

        double getBfsTimeMs() { return bfsTimeMicro / 1000.0; }
        double getDpTimeMs() { return dpTimeMicro / 1000.0; }
    }


    // START GAME
    public SnakeLadderResponse startGame(SnakeLadderRequest req) {
        //ADD THIS - Player name validation
        if (req.getPlayerName() == null || req.getPlayerName().trim().isEmpty()) {
            throw new IllegalArgumentException("Player name is required");
        }

        int N = req.getBoardSize();

        if (N < 6 || N > 12) {
            throw new IllegalArgumentException("Board size must be between 6 and 12");
        }

        // Generate valid snakes and ladders (N-2 each)
        Map<Integer, Integer> ladders = generateValidLadders(N);
        Map<Integer, Integer> snakes = generateValidSnakes(N, ladders);
        validateNoConflicts(snakes, ladders);

        // Algorithm 1: BFS - Measure in nanoseconds
        long bfsStart = System.nanoTime();
        int bfsResult = bfs(N, ladders, snakes);
        long bfsTimeNano = System.nanoTime() - bfsStart;
        long bfsTimeMicro = bfsTimeNano / 1000;
        double bfsTimeMs = bfsTimeNano / 1_000_000.0;  // Changed to double with .0

        // Algorithm 2: Dynamic Programming
        long dpStart = System.nanoTime();
        int dpResult = dynamicProgramming(N, ladders, snakes);
        long dpTimeNano = System.nanoTime() - dpStart;
        long dpTimeMicro = dpTimeNano / 1000;
        double dpTimeMs = dpTimeNano / 1_000_000.0;  // Changed to double with .0

        System.out.println("=========================================");
        System.out.println("Board Size: " + N);
        System.out.println("BFS - Nanoseconds: " + bfsTimeNano + " ns, Microseconds: " + bfsTimeMicro + " μs, Milliseconds: " + bfsTimeMs + " ms");
        System.out.println("DP  - Nanoseconds: " + dpTimeNano + " ns, Microseconds: " + dpTimeMicro + " μs, Milliseconds: " + dpTimeMs + " ms");
        System.out.println("=========================================");

        int correctAnswer = bfsResult;

        // Generate a unique round ID (in-memory only)
        Long roundId = roundIdCounter++;

        // Store board config and algorithm times in memory
        boardConfigStore.put(roundId, new BoardConfig(snakes, ladders, N));
        algorithmTimesStore.put(roundId, new AlgorithmTimes(bfsTimeMicro, dpTimeMicro));
        answerStore.put(roundId, correctAnswer);

        List<Integer> options = generateOptions(correctAnswer);

        return new SnakeLadderResponse(
                roundId,
                N,
                correctAnswer,
                options,
                snakes,
                ladders,
                bfsTimeMs,
                dpTimeMs
        );
    }

    // SUBMIT ANSWER - USING YOUR OWN SnakeLadderApiResponse
    public SnakeLadderApiResponse submitAnswer(SnakeLadderAnswerRequest req) {
        // Validation
        if (req.getPlayerName() == null || req.getPlayerName().trim().isEmpty()) {
            return new SnakeLadderApiResponse(false, "Player name is required");
        }

        int userAnswer;
        try {
            userAnswer = Integer.parseInt(req.getAnswerText());
        } catch (NumberFormatException e) {
            return new SnakeLadderApiResponse(false, "Invalid answer format. Please enter a number.");
        }

        Long roundId = req.getRoundId();
        if (roundId == null) {
            return new SnakeLadderApiResponse(false, "No active game round. Please start a new game.");
        }

        Integer correctAnswer = answerStore.get(roundId);
        if (correctAnswer == null) {
            return new SnakeLadderApiResponse(false, "Game round expired. Please start a new game.");
        }

        // Get algorithm times from memory store
        AlgorithmTimes times = algorithmTimesStore.get(roundId);
        long bfsTimeMicro = 0;
        long dpTimeMicro = 0;

        if (times != null) {
            bfsTimeMicro = times.bfsTimeMicro;
            dpTimeMicro = times.dpTimeMicro;
            System.out.println("Retrieved times - BFS: " + bfsTimeMicro + " μs, DP: " + dpTimeMicro + " μs");
        }

        // Convert to milliseconds as DOUBLE to preserve decimal values
        double bfsTimeMs = bfsTimeMicro / 1000.0;
        double dpTimeMs = dpTimeMicro / 1000.0;

        // For display - use the actual double values (no rounding to integer)
        double bfsTimeForDisplay = bfsTimeMs;
        double dpTimeForDisplay = dpTimeMs;

        boolean isCorrect = (userAnswer == correctAnswer);

        BoardConfig boardConfig = boardConfigStore.get(roundId);

        // Save to snake_ladder_game_result table only (when correct)
        if (isCorrect) {
            SnakeLadderGameResult result = new SnakeLadderGameResult();
            result.setPlayerName(req.getPlayerName());
            result.setBoardSize(boardConfig != null ? boardConfig.boardSize : 8);
            result.setCorrectAnswer(correctAnswer);
            result.setWin(true);
            result.setUserAnswer(userAnswer);
            result.setBfsTimeMs(bfsTimeMicro);
            result.setDpTimeMs(dpTimeMicro);
            result.setMinimumMoves(correctAnswer);
            result.setGameRoundId(roundId);
            // FIXED: Use sequential round number instead of random
            result.setRoundNumber(getNextRoundNumber(req.getPlayerName()));

            if (boardConfig != null) {
                try {
                    result.setSnakesJson(objectMapper.writeValueAsString(boardConfig.snakes));
                    result.setLaddersJson(objectMapper.writeValueAsString(boardConfig.ladders));
                } catch (JsonProcessingException e) {
                    result.setSnakesJson("{}");
                    result.setLaddersJson("{}");
                }
            }

            result.setPlayedAt(LocalDateTime.now());
            snakeLadderResultRepo.save(result);

            System.out.println("Saved to DB - Player: " + req.getPlayerName() +
                    ", BFS: " + bfsTimeMicro + " μs (" + bfsTimeForDisplay + " ms)" +
                    ", DP: " + dpTimeMicro + " μs (" + dpTimeForDisplay + " ms)");

            String bestAlgorithm = bfsTimeMicro <= dpTimeMicro ? "BFS" : "Dynamic Programming";
            String comparisonMessage = String.format("BFS took %d μs (%.3f ms), DP took %d μs (%.3f ms). %s was faster.",
                    bfsTimeMicro, bfsTimeForDisplay, dpTimeMicro, dpTimeForDisplay, bestAlgorithm);

            // USING YOUR OWN SnakeLadderApiResponse
            return new SnakeLadderApiResponse(
                    true,                    // success
                    "✅ Correct! Well done! The minimum dice throws needed is " + correctAnswer,  // message
                    true,                    // correct
                    bfsTimeForDisplay,       // bfsTimeMs
                    dpTimeForDisplay,        // dpTimeMs
                    bfsTimeForDisplay + dpTimeForDisplay,  // totalTimeMs
                    bestAlgorithm,           // bestAlgorithm
                    comparisonMessage        // comparisonMessage
            );
        } else {
            String bestAlgorithm = bfsTimeMicro <= dpTimeMicro ? "BFS" : "Dynamic Programming";
            String comparisonMessage = String.format("BFS took %.3f ms, DP took %.3f ms.", bfsTimeForDisplay, dpTimeForDisplay);

            // ✅ CORRECTED: 8 parameters
            return new SnakeLadderApiResponse(
                    true,
                    "❌ Incorrect! The correct answer is " + correctAnswer + ". Try again!",  // message
                    false,
                    bfsTimeForDisplay,
                    dpTimeForDisplay,
                    bfsTimeForDisplay + dpTimeForDisplay,
                    bestAlgorithm,
                    comparisonMessage
            );
        }
    }

    //HELPER METHODS

    private Map<Integer, Integer> generateValidLadders(int N) {
        Map<Integer, Integer> ladders = new HashMap<>();
        Random rand = new Random();
        int totalCells = N * N;
        int targetCount = N - 2;

        if (totalCells <= 3) return ladders;

        while (ladders.size() < targetCount) {
            int maxBottom = totalCells - 2;
            if (maxBottom <= 2) break;

            int bottom = rand.nextInt(maxBottom - 1) + 2;
            int maxTop = totalCells - 1;
            if (bottom + 1 > maxTop) continue;

            int top = rand.nextInt(maxTop - bottom) + bottom + 1;

            if (bottom < top && top < totalCells && !ladders.containsKey(bottom)) {
                ladders.put(bottom, top);
            }
        }
        return ladders;
    }

    private Map<Integer, Integer> generateValidSnakes(int N, Map<Integer, Integer> ladders) {
        Map<Integer, Integer> snakes = new HashMap<>();
        Random rand = new Random();
        int totalCells = N * N;
        int targetCount = N - 2;

        if (totalCells <= 3) return snakes;

        while (snakes.size() < targetCount) {
            int maxHead = totalCells - 2;
            if (maxHead <= 2) break;

            int head = rand.nextInt(maxHead - 1) + 2;
            if (head <= 2) continue;

            int tail = rand.nextInt(head - 1) + 1;

            if (head > tail && !snakes.containsKey(head) && !ladders.containsKey(head)) {
                snakes.put(head, tail);
            }
        }
        return snakes;
    }

    private void validateNoConflicts(Map<Integer, Integer> snakes, Map<Integer, Integer> ladders) {
        for (int snakeHead : snakes.keySet()) {
            if (ladders.containsKey(snakeHead)) {
                throw new IllegalStateException("Snake head cannot be at ladder bottom: " + snakeHead);
            }
        }
    }

    // ALGORITHM 1: BFS
    private int bfs(int N, Map<Integer, Integer> ladders, Map<Integer, Integer> snakes) {
        int totalCells = N * N;
        Queue<Integer> queue = new LinkedList<>();
        boolean[] visited = new boolean[totalCells + 1];
        int[] moves = new int[totalCells + 1];

        Arrays.fill(moves, -1);
        queue.add(1);
        visited[1] = true;
        moves[1] = 0;

        while (!queue.isEmpty()) {
            int current = queue.poll();

            if (current == totalCells) {
                return moves[current];
            }

            for (int dice = 1; dice <= 6; dice++) {
                int next = current + dice;

                if (next <= totalCells) {
                    if (ladders.containsKey(next)) {
                        next = ladders.get(next);
                    } else if (snakes.containsKey(next)) {
                        next = snakes.get(next);
                    }

                    if (!visited[next]) {
                        visited[next] = true;
                        moves[next] = moves[current] + 1;
                        queue.add(next);
                    }
                }
            }
        }
        return -1;
    }

    // ALGORITHM 2: DYNAMIC PROGRAMMING
    private int dynamicProgramming(int N, Map<Integer, Integer> ladders, Map<Integer, Integer> snakes) {
        int totalCells = N * N;
        int[] dp = new int[totalCells + 1];
        Arrays.fill(dp, Integer.MAX_VALUE);
        dp[1] = 0;

        boolean changed;
        int maxIterations = totalCells * 2;

        for (int iter = 0; iter < maxIterations; iter++) {
            changed = false;

            for (int i = 1; i <= totalCells; i++) {
                if (dp[i] == Integer.MAX_VALUE) continue;

                for (int dice = 1; dice <= 6; dice++) {
                    int next = i + dice;
                    if (next > totalCells) continue;

                    int destination = next;
                    if (ladders.containsKey(next)) {
                        destination = ladders.get(next);
                    } else if (snakes.containsKey(next)) {
                        destination = snakes.get(next);
                    }

                    if (dp[destination] > dp[i] + 1) {
                        dp[destination] = dp[i] + 1;
                        changed = true;
                    }
                }
            }

            if (!changed) break;
        }

        return dp[totalCells] != Integer.MAX_VALUE ? dp[totalCells] : -1;
    }

    // Generate MCQ options
    private List<Integer> generateOptions(int correct) {
        List<Integer> options = new ArrayList<>();
        options.add(correct);

        Random rand = new Random();

        while (options.size() < 3) {
            int offset = rand.nextInt(7) - 3;
            int val = correct + offset;

            if (val > 0 && val != correct && !options.contains(val)) {
                options.add(val);
            }

            if (options.size() == 1 && rand.nextInt(10) > 7) {
                options.add(correct + 2);
            }
            if (options.size() == 2 && rand.nextInt(10) > 7) {
                options.add(Math.max(1, correct - 1));
            }
        }

        Collections.shuffle(options);
        return options;
    }

    // Optional: Get performance history from database
    public List<SnakeLadderGameResult> getPerformanceHistory() {
        List<SnakeLadderGameResult> allResults = snakeLadderResultRepo.findAll();
        if (allResults.size() > 20) {
            return allResults.subList(allResults.size() - 20, allResults.size());
        }
        return allResults;
    }

    //PERFORMANCE DASHBOARD
    private int getNextRoundNumber(String playerName) {
        int nextRound = playerRoundCounters.getOrDefault(playerName, 0) + 1;
        playerRoundCounters.put(playerName, nextRound);
        return nextRound;
    }


    public List<PerformanceRecord> getPerformanceHistoryForPlayer(String playerName) {
        List<SnakeLadderGameResult> results;

        if (playerName != null && !playerName.trim().isEmpty()) {
            results = snakeLadderResultRepo.findByPlayerName(playerName);
        } else {
            results = snakeLadderResultRepo.findAll();
        }

        // Sort by playedAt descending and take last 20
        results.sort((a, b) -> b.getPlayedAt().compareTo(a.getPlayedAt()));
        if (results.size() > 20) {
            results = results.subList(0, 20);
        }

        // Convert to PerformanceRecord and reverse to show chronological order
        List<PerformanceRecord> records = new ArrayList<>();
        for (int i = results.size() - 1; i >= 0; i--) {
            SnakeLadderGameResult r = results.get(i);

            // Convert microseconds to milliseconds for display
            double bfsMs = r.getBfsTimeMs() / 1000.0;
            double dpMs = r.getDpTimeMs() / 1000.0;

            String winner = bfsMs <= dpMs ? "BFS" : "DP";

            PerformanceRecord record = new PerformanceRecord(
                    r.getRoundNumber(),
                    r.getBoardSize(),
                    bfsMs,
                    dpMs,
                    winner,
                    r.getPlayedAt()
            );
            records.add(record);
        }

        return records;
    }


    public Map<String, Object> getPerformanceStatsForPlayer(String playerName) {
        List<PerformanceRecord> history = getPerformanceHistoryForPlayer(playerName);

        if (history.isEmpty()) {
            Map<String, Object> emptyStats = new HashMap<>();
            emptyStats.put("totalRounds", 0);
            emptyStats.put("avgBfsMs", 0);
            emptyStats.put("avgDpMs", 0);
            emptyStats.put("dpFasterCount", 0);
            emptyStats.put("dpFasterPercentage", 0);
            emptyStats.put("bestBfsTime", 0);
            emptyStats.put("bestDpTime", 0);
            emptyStats.put("worstBfsTime", 0);
            emptyStats.put("worstDpTime", 0);
            return emptyStats;
        }

        double avgBfs = history.stream().mapToDouble(PerformanceRecord::getBfsTimeMs).average().orElse(0);
        double avgDp = history.stream().mapToDouble(PerformanceRecord::getDpTimeMs).average().orElse(0);
        long dpFasterCount = history.stream().filter(r -> r.getWinner().equals("DP")).count();
        double bestBfs = history.stream().mapToDouble(PerformanceRecord::getBfsTimeMs).min().orElse(0);
        double bestDp = history.stream().mapToDouble(PerformanceRecord::getDpTimeMs).min().orElse(0);
        double worstBfs = history.stream().mapToDouble(PerformanceRecord::getBfsTimeMs).max().orElse(0);
        double worstDp = history.stream().mapToDouble(PerformanceRecord::getDpTimeMs).max().orElse(0);

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalRounds", history.size());
        stats.put("avgBfsMs", Math.round(avgBfs * 100) / 100.0);
        stats.put("avgDpMs", Math.round(avgDp * 100) / 100.0);
        stats.put("dpFasterCount", dpFasterCount);
        stats.put("dpFasterPercentage", Math.round((dpFasterCount * 100.0 / history.size()) * 10) / 10.0);
        stats.put("bestBfsTime", Math.round(bestBfs * 100) / 100.0);
        stats.put("bestDpTime", Math.round(bestDp * 100) / 100.0);
        stats.put("worstBfsTime", Math.round(worstBfs * 100) / 100.0);
        stats.put("worstDpTime", Math.round(worstDp * 100) / 100.0);

        return stats;
    }
}