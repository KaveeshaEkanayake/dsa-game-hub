/*package com.dsagamehub.service;

import com.dsagamehub.dto.*;
import com.dsagamehub.model.*;
import com.dsagamehub.repository.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class SnakeLadderService {

    private final GameRoundRepository roundRepo;
    private final AlgorithmRunRepository algoRepo;
    private final SnakeLadderRepository snakeLadderResultRepo;
    private final ObjectMapper objectMapper;

    // Store answers per round
    private final Map<Long, Integer> answerStore = new HashMap<>();
    // Store board configurations per round
    private final Map<Long, BoardConfig> boardConfigStore = new HashMap<>();
    // Store algorithm times per round
    private final Map<Long, AlgorithmTimes> algorithmTimesStore = new HashMap<>();

    public SnakeLadderService(GameRoundRepository roundRepo,
                              AlgorithmRunRepository algoRepo,
                              SnakeLadderRepository snakeLadderResultRepo) {
        this.roundRepo = roundRepo;
        this.algoRepo = algoRepo;
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
        long bfsTime;
        long dpTime;

        AlgorithmTimes(long bfsTime, long dpTime) {
            this.bfsTime = bfsTime;
            this.dpTime = dpTime;
        }
    }

    // START GAME
    public SnakeLadderResponse startGame(SnakeLadderRequest req) {
        int N = req.getBoardSize();

        if (N < 6 || N > 12) {
            throw new IllegalArgumentException("Board size must be between 6 and 12");
        }

        // Generate valid snakes and ladders (N-2 each)
        Map<Integer, Integer> ladders = generateValidLadders(N);
        Map<Integer, Integer> snakes = generateValidSnakes(N, ladders);
        validateNoConflicts(snakes, ladders);

        // Algorithm 1: BFS
        long bfsStart = System.nanoTime();
        int bfsResult = bfs(N, ladders, snakes);
        long bfsTimeMs = (System.nanoTime() - bfsStart) / 1_000_000;

        // Algorithm 2: Dynamic Programming
        long dpStart = System.nanoTime();
        int dpResult = dynamicProgramming(N, ladders, snakes);
        long dpTimeMs = (System.nanoTime() - dpStart) / 1_000_000;

        int correctAnswer = bfsResult;

        // SAVE GAME ROUND
        GameRound round = new GameRound();
        round.setGameName("SnakeLadder");
        round.setRoundNumber(new Random().nextInt(1000));
        round.setAllSolutionsFound(false);
        GameRound savedRound = roundRepo.save(round);

        // Store board config
        boardConfigStore.put(savedRound.getId(), new BoardConfig(snakes, ladders, N));

        // Store algorithm times in memory
        algorithmTimesStore.put(savedRound.getId(), new AlgorithmTimes(bfsTimeMs, dpTimeMs));

        // SAVE BFS
        AlgorithmRun bfsRun = new AlgorithmRun();
        bfsRun.setGameRoundId(savedRound.getId());
        bfsRun.setGameName("SnakeLadder");
        bfsRun.setAlgorithmType("BFS");
        bfsRun.setSolutionCount(bfsResult);
        bfsRun.setTimeTakenMs(bfsTimeMs);
        algoRepo.save(bfsRun);

        // SAVE DP
        AlgorithmRun dpRun = new AlgorithmRun();
        dpRun.setGameRoundId(savedRound.getId());
        dpRun.setGameName("SnakeLadder");
        dpRun.setAlgorithmType("DynamicProgramming");
        dpRun.setSolutionCount(dpResult);
        dpRun.setTimeTakenMs(dpTimeMs);
        algoRepo.save(dpRun);

        answerStore.put(savedRound.getId(), correctAnswer);
        List<Integer> options = generateOptions(correctAnswer);

        return new SnakeLadderResponse(
                savedRound.getId(),
                N,
                correctAnswer,
                options,
                snakes,
                ladders,
                bfsTimeMs,
                dpTimeMs
        );
    }

    // SUBMIT ANSWER
    public ApiResponse submitAnswer(PlayerAnswerRequest req) {
        // Validation
        if (req.getPlayerName() == null || req.getPlayerName().trim().isEmpty()) {
            return new ApiResponse(false, "Player name is required");
        }

        int userAnswer;
        try {
            userAnswer = Integer.parseInt(req.getAnswerText());
        } catch (NumberFormatException e) {
            return new ApiResponse(false, "Invalid answer format. Please enter a number.");
        }

        Long roundId = req.getRoundId();
        if (roundId == null) {
            return new ApiResponse(false, "No active game round. Please start a new game.");
        }

        Integer correctAnswer = answerStore.get(roundId);
        if (correctAnswer == null) {
            return new ApiResponse(false, "Game round expired. Please start a new game.");
        }

        // FIXED: Get algorithm times - First try from memory store
        AlgorithmTimes times = algorithmTimesStore.get(roundId);
        long bfsTime = 0;
        long dpTime = 0;

        if (times != null) {
            bfsTime = times.bfsTime;
            dpTime = times.dpTime;
            System.out.println("Retrieved times from memory - BFS: " + bfsTime + "ms, DP: " + dpTime + "ms");
        } else {
            // Fallback: Try to get from database
            AlgorithmRun bfsRun = algoRepo.findByGameRoundIdAndAlgorithmType(roundId, "BFS");
            AlgorithmRun dpRun = algoRepo.findByGameRoundIdAndAlgorithmType(roundId, "DynamicProgramming");

            bfsTime = bfsRun != null ? bfsRun.getTimeTakenMs() : 0;
            dpTime = dpRun != null ? dpRun.getTimeTakenMs() : 0;
            System.out.println("Retrieved times from DB - BFS: " + bfsTime + "ms, DP: " + dpTime + "ms");
        }

        boolean isCorrect = (userAnswer == correctAnswer);

        BoardConfig boardConfig = boardConfigStore.get(roundId);
        String boardStateJson = "";
        if (boardConfig != null) {
            try {
                Map<String, Object> boardState = new HashMap<>();
                boardState.put("snakes", boardConfig.snakes);
                boardState.put("ladders", boardConfig.ladders);
                boardState.put("boardSize", boardConfig.boardSize);
                boardStateJson = objectMapper.writeValueAsString(boardState);
            } catch (JsonProcessingException e) {
                boardStateJson = "{}";
            }
        }

        // Save to database when correct - WITH CORRECT TIMES
        if (isCorrect) {
            SnakeLadderGameResult result = new SnakeLadderGameResult();
            result.setPlayerName(req.getPlayerName());
            result.setBoardSize(boardConfig != null ? boardConfig.boardSize : 8);
            result.setCorrectAnswer(correctAnswer);
            result.setWin(true);
            result.setUserAnswer(userAnswer);
            result.setBfsTimeMs(bfsTime);  // Now will have correct value
            result.setDpTimeMs(dpTime);    // Now will have correct value
            result.setMinimumMoves(correctAnswer);
            result.setGameRoundId(roundId);
            result.setRoundNumber(new Random().nextInt(1000));

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

            System.out.println("Saved to snake_ladder_game_result - BFS: " + bfsTime + "ms, DP: " + dpTime + "ms");

            // Comparing BFS vs DP to determine best algorithm
            String bestAlgorithm = bfsTime <= dpTime ? "BFS" : "Dynamic Programming";
            String comparisonMessage = String.format("BFS took %dms, DP took %dms. %s was faster.",
                    bfsTime, dpTime, bestAlgorithm);

            return new ApiResponse(
                    true,
                    "✅ Correct! Well done! The minimum dice throws needed is " + correctAnswer,
                    true,
                    false,
                    bfsTime,
                    dpTime,
                    bfsTime + dpTime,
                    bestAlgorithm,
                    comparisonMessage,
                    false
            );
        } else {
            String bestAlgorithm = bfsTime <= dpTime ? "BFS" : "Dynamic Programming";
            String comparisonMessage = String.format("BFS took %dms, DP took %dms.", bfsTime, dpTime);

            return new ApiResponse(
                    true,
                    "❌ Incorrect! The correct answer is " + correctAnswer + ". Try again!",
                    false,
                    false,
                    bfsTime,
                    dpTime,
                    bfsTime + dpTime,
                    bestAlgorithm,
                    comparisonMessage,
                    false
            );
        }
    }

    // ==================== VALID LADDER GENERATION ====================

    private Map<Integer, Integer> generateValidLadders(int N) {
        Map<Integer, Integer> ladders = new HashMap<>();
        Random rand = new Random();
        int totalCells = N * N;
        int targetCount = N - 2;

        while (ladders.size() < targetCount) {
            int bottom = rand.nextInt(totalCells - 2) + 2;
            int top = rand.nextInt(totalCells - bottom - 1) + bottom + 1;

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

        while (snakes.size() < targetCount) {
            int head = rand.nextInt(totalCells - 2) + 2;
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
                throw new IllegalStateException("Snake head cannot be at ladder bottom");
            }
        }
    }

    // ==================== ALGORITHM 1: BFS ====================

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

    // ==================== ALGORITHM 2: DYNAMIC PROGRAMMING ====================

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

    // ==================== HELPER METHODS ====================

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
}*/



/*package com.dsagamehub.service;

import com.dsagamehub.dto.*;
import com.dsagamehub.model.*;
import com.dsagamehub.repository.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class SnakeLadderService {

    private final GameRoundRepository roundRepo;
    private final AlgorithmRunRepository algoRepo;
    private final SnakeLadderRepository snakeLadderResultRepo;
    private final PlayerRepository playerRepo;
    private final PlayerAnswerRepository playerAnswerRepo;
    private final ObjectMapper objectMapper;

    // Store answers per round
    private final Map<Long, Integer> answerStore = new HashMap<>();
    // Store board configurations per round
    private final Map<Long, BoardConfig> boardConfigStore = new HashMap<>();
    // Store algorithm times per round (in MICROSECONDS for better precision)
    private final Map<Long, AlgorithmTimes> algorithmTimesStore = new HashMap<>();

    public SnakeLadderService(GameRoundRepository roundRepo,
                              AlgorithmRunRepository algoRepo,
                              SnakeLadderRepository snakeLadderResultRepo,
                              PlayerRepository playerRepo,
                              PlayerAnswerRepository playerAnswerRepo) {
        this.roundRepo = roundRepo;
        this.algoRepo = algoRepo;
        this.snakeLadderResultRepo = snakeLadderResultRepo;
        this.playerRepo = playerRepo;
        this.playerAnswerRepo = playerAnswerRepo;
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
        long bfsTimeMicro;  // Store in microseconds for precision
        long dpTimeMicro;

        AlgorithmTimes(long bfsTimeMicro, long dpTimeMicro) {
            this.bfsTimeMicro = bfsTimeMicro;
            this.dpTimeMicro = dpTimeMicro;
        }

        long getBfsTimeMs() { return bfsTimeMicro / 1000; }
        long getDpTimeMs() { return dpTimeMicro / 1000; }
    }

    // START GAME
    public SnakeLadderResponse startGame(SnakeLadderRequest req) {
        int N = req.getBoardSize();

        if (N < 6 || N > 12) {
            throw new IllegalArgumentException("Board size must be between 6 and 12");
        }

        // Generate valid snakes and ladders (N-2 each)
        Map<Integer, Integer> ladders = generateValidLadders(N);
        Map<Integer, Integer> snakes = generateValidSnakes(N, ladders);
        validateNoConflicts(snakes, ladders);

        // Algorithm 1: BFS - Measure in nanoseconds for precision
        long bfsStart = System.nanoTime();
        int bfsResult = bfs(N, ladders, snakes);
        long bfsTimeNano = System.nanoTime() - bfsStart;
        long bfsTimeMicro = bfsTimeNano / 1000;
        long bfsTimeMs = bfsTimeNano / 1_000_000;

        // Algorithm 2: Dynamic Programming
        long dpStart = System.nanoTime();
        int dpResult = dynamicProgramming(N, ladders, snakes);
        long dpTimeNano = System.nanoTime() - dpStart;
        long dpTimeMicro = dpTimeNano / 1000;
        long dpTimeMs = dpTimeNano / 1_000_000;

        System.out.println("=========================================");
        System.out.println("Board Size: " + N);
        System.out.println("BFS - Nanoseconds: " + bfsTimeNano + " ns, Microseconds: " + bfsTimeMicro + " μs, Milliseconds: " + bfsTimeMs + " ms");
        System.out.println("DP  - Nanoseconds: " + dpTimeNano + " ns, Microseconds: " + dpTimeMicro + " μs, Milliseconds: " + dpTimeMs + " ms");
        System.out.println("=========================================");

        int correctAnswer = bfsResult;

        // SAVE GAME ROUND
        GameRound round = new GameRound();
        round.setGameName("SnakeLadder");
        round.setRoundNumber(new Random().nextInt(1000));
        round.setAllSolutionsFound(false);
        GameRound savedRound = roundRepo.save(round);

        // Store board config
        boardConfigStore.put(savedRound.getId(), new BoardConfig(snakes, ladders, N));
        algorithmTimesStore.put(savedRound.getId(), new AlgorithmTimes(bfsTimeMicro, dpTimeMicro));

        long bfsTimeToStore = bfsTimeMicro > 0 ? bfsTimeMicro : (bfsTimeNano > 0 ? 1 : 0);
        long dpTimeToStore = dpTimeMicro > 0 ? dpTimeMicro : (dpTimeNano > 0 ? 1 : 0);

        // SAVE BFS
        AlgorithmRun bfsRun = new AlgorithmRun();
        bfsRun.setGameRoundId(savedRound.getId());
        bfsRun.setGameName("SnakeLadder");
        bfsRun.setAlgorithmType("BFS");
        bfsRun.setSolutionCount(bfsResult);
        bfsRun.setTimeTakenMs(bfsTimeToStore);
        algoRepo.save(bfsRun);

        // SAVE DP
        AlgorithmRun dpRun = new AlgorithmRun();
        dpRun.setGameRoundId(savedRound.getId());
        dpRun.setGameName("SnakeLadder");
        dpRun.setAlgorithmType("DynamicProgramming");
        dpRun.setSolutionCount(dpResult);
        dpRun.setTimeTakenMs(dpTimeToStore);
        algoRepo.save(dpRun);

        answerStore.put(savedRound.getId(), correctAnswer);
        List<Integer> options = generateOptions(correctAnswer);

        return new SnakeLadderResponse(
                savedRound.getId(),
                N,
                correctAnswer,
                options,
                snakes,
                ladders,
                bfsTimeMs,
                dpTimeMs
        );
    }

    // SUBMIT ANSWER
    public ApiResponse submitAnswer(PlayerAnswerRequest req) {
        // Validation
        if (req.getPlayerName() == null || req.getPlayerName().trim().isEmpty()) {
            return new ApiResponse(false, "Player name is required");
        }

        int userAnswer;
        try {
            userAnswer = Integer.parseInt(req.getAnswerText());
        } catch (NumberFormatException e) {
            return new ApiResponse(false, "Invalid answer format. Please enter a number.");
        }

        Long roundId = req.getRoundId();
        if (roundId == null) {
            return new ApiResponse(false, "No active game round. Please start a new game.");
        }

        Integer correctAnswer = answerStore.get(roundId);
        if (correctAnswer == null) {
            return new ApiResponse(false, "Game round expired. Please start a new game.");
        }

        // Get algorithm times from memory store
        AlgorithmTimes times = algorithmTimesStore.get(roundId);
        long bfsTimeMicro = 0;
        long dpTimeMicro = 0;

        if (times != null) {
            bfsTimeMicro = times.bfsTimeMicro;
            dpTimeMicro = times.dpTimeMicro;
            System.out.println("Retrieved times - BFS: " + bfsTimeMicro + " μs, DP: " + dpTimeMicro + " μs");
        } else {
            AlgorithmRun bfsRun = algoRepo.findByGameRoundIdAndAlgorithmType(roundId, "BFS");
            AlgorithmRun dpRun = algoRepo.findByGameRoundIdAndAlgorithmType(roundId, "DynamicProgramming");
            bfsTimeMicro = bfsRun != null ? bfsRun.getTimeTakenMs() : 0;
            dpTimeMicro = dpRun != null ? dpRun.getTimeTakenMs() : 0;
        }

        long bfsTimeMs = bfsTimeMicro / 1000;
        long dpTimeMs = dpTimeMicro / 1000;
        long bfsTimeForDisplay = bfsTimeMs > 0 ? bfsTimeMs : (bfsTimeMicro > 0 ? 1 : 0);
        long dpTimeForDisplay = dpTimeMs > 0 ? dpTimeMs : (dpTimeMicro > 0 ? 1 : 0);

        boolean isCorrect = (userAnswer == correctAnswer);

        BoardConfig boardConfig = boardConfigStore.get(roundId);
        String boardStateJson = "";
        if (boardConfig != null) {
            try {
                Map<String, Object> boardState = new HashMap<>();
                boardState.put("snakes", boardConfig.snakes);
                boardState.put("ladders", boardConfig.ladders);
                boardState.put("boardSize", boardConfig.boardSize);
                boardStateJson = objectMapper.writeValueAsString(boardState);
            } catch (JsonProcessingException e) {
                boardStateJson = "{}";
            }
        }

        // SAVE PLAYER TO DATABASE
        Optional<Player> existingPlayer = playerRepo.findByName(req.getPlayerName());
        Player player;
        if (existingPlayer.isPresent()) {
            player = existingPlayer.get();
            System.out.println("Existing player found: " + player.getName());
        } else {
            player = new Player(req.getPlayerName());
            playerRepo.save(player);
            System.out.println("New player saved: " + player.getName());
        }

        // SAVE PLAYER ANSWER TO DATABASE
        String answerMessage = isCorrect ? "WIN"  : "LOSE" ;
        PlayerAnswer playerAnswer = new PlayerAnswer(
                req.getPlayerName(),
                req.getAnswerText(),
                isCorrect,
                false,
                answerMessage
        );
        playerAnswerRepo.save(playerAnswer);
        System.out.println("Player answer saved - Player: " + req.getPlayerName() + ", Answer: " + req.getAnswerText() + ", Correct: " + isCorrect);

        // Save to database when correct
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
            result.setRoundNumber(new Random().nextInt(1000));

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

            System.out.println("Saved to DB - BFS: " + bfsTimeMicro + " μs (" + bfsTimeForDisplay + " ms), DP: " + dpTimeMicro + " μs (" + dpTimeForDisplay + " ms)");

            String bestAlgorithm = bfsTimeMicro <= dpTimeMicro ? "BFS" : "Dynamic Programming";
            String comparisonMessage = String.format("BFS took %d μs (%d ms), DP took %d μs (%d ms). %s was faster.",
                    bfsTimeMicro, bfsTimeForDisplay, dpTimeMicro, dpTimeForDisplay, bestAlgorithm);

            return new ApiResponse(
                    true,
                    "✅ Correct! Well done! The minimum dice throws needed is " + correctAnswer,
                    true,
                    false,
                    bfsTimeForDisplay,
                    dpTimeForDisplay,
                    bfsTimeForDisplay + dpTimeForDisplay,
                    bestAlgorithm,
                    comparisonMessage,
                    false
            );
        } else {
            String bestAlgorithm = bfsTimeMicro <= dpTimeMicro ? "BFS" : "Dynamic Programming";
            String comparisonMessage = String.format("BFS took %d μs, DP took %d μs.", bfsTimeMicro, dpTimeMicro);

            return new ApiResponse(
                    true,
                    "❌ Incorrect! The correct answer is " + correctAnswer + ". Try again!",
                    false,
                    false,
                    bfsTimeForDisplay,
                    dpTimeForDisplay,
                    bfsTimeForDisplay + dpTimeForDisplay,
                    bestAlgorithm,
                    comparisonMessage,
                    false
            );
        }
    }

    // VALID LADDER GENERATION

    /*private Map<Integer, Integer> generateValidLadders(int N) {
        Map<Integer, Integer> ladders = new HashMap<>();
        Random rand = new Random();
        int totalCells = N * N;
        int targetCount = N - 2;

        while (ladders.size() < targetCount) {
            int bottom = rand.nextInt(totalCells - 2) + 2;
            int top = rand.nextInt(totalCells - bottom - 1) + bottom + 1;

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

        while (snakes.size() < targetCount) {
            int head = rand.nextInt(totalCells - 2) + 2;
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
                throw new IllegalStateException("Snake head cannot be at ladder bottom");
            }
        }
    }
     */

   /* private Map<Integer, Integer> generateValidLadders(int N) {
        Map<Integer, Integer> ladders = new HashMap<>();
        Random rand = new Random();
        int totalCells = N * N;
        int targetCount = N - 2;

        // FIX: Make sure there are enough cells to place ladders
        if (totalCells <= 3) return ladders;

        while (ladders.size() < targetCount) {
            // FIX: Ensure the range is valid (bottom from 2 to totalCells-2)
            int maxBottom = totalCells - 2;
            if (maxBottom <= 2) break;

            int bottom = rand.nextInt(maxBottom - 1) + 2;

            // FIX: Ensure top is within bounds
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

        // FIX: Make sure there are enough cells to place snakes
        if (totalCells <= 3) return snakes;

        while (snakes.size() < targetCount) {
            // FIX: Ensure head is within valid range (2 to totalCells-2)
            int maxHead = totalCells - 2;
            if (maxHead <= 2) break;

            int head = rand.nextInt(maxHead - 1) + 2;

            // FIX: Ensure tail is less than head
            if (head <= 2) continue;

            int tail = rand.nextInt(head - 1) + 1;

            if (head > tail && !snakes.containsKey(head) && !ladders.containsKey(head)) {
                snakes.put(head, tail);
            }
        }
        return snakes;
    }

    // ==================== VALIDATION METHOD ====================

   /* private void validateNoConflicts(Map<Integer, Integer> snakes, Map<Integer, Integer> ladders) {
        // Check snake heads don't land on ladder bottoms
        for (int snakeHead : snakes.keySet()) {
            if (ladders.containsKey(snakeHead)) {
                throw new IllegalStateException("Snake head cannot be at ladder bottom");
            }
        }

        // Check ladder tops don't land on snake heads
        for (int ladderTop : ladders.values()) {
            if (snakes.containsKey(ladderTop)) {
                throw new IllegalStateException("Ladder top cannot be at snake head");
            }
        }

        // Check snakes don't overlap with each other
        Set<Integer> snakeHeads = snakes.keySet();
        if (snakeHeads.size() != new HashSet<>(snakeHeads).size()) {
            throw new IllegalStateException("Duplicate snake head positions");
        }

        // Check ladders don't overlap with each other
        Set<Integer> ladderBottoms = ladders.keySet();
        if (ladderBottoms.size() != new HashSet<>(ladderBottoms).size()) {
            throw new IllegalStateException("Duplicate ladder bottom positions");
        }
    }*/

    /*private void validateNoConflicts(Map<Integer, Integer> snakes, Map<Integer, Integer> ladders) {
        // Only check for direct conflicts where a snake head and ladder bottom share the same cell
        for (int snakeHead : snakes.keySet()) {
            if (ladders.containsKey(snakeHead)) {
                throw new IllegalStateException("Snake head cannot be at ladder bottom: " + snakeHead);
            }
        }

        // Ladder tops can land on snake heads - this is allowed in Snakes & Ladders!
        // For example, you can climb a ladder and then slide down a snake
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

    // HELPER METHODS....

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
}*/

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

        long getBfsTimeMs() { return bfsTimeMicro / 1000; }
        long getDpTimeMs() { return dpTimeMicro / 1000; }
    }

    // START GAME
    public SnakeLadderResponse startGame(SnakeLadderRequest req) {
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
        long bfsTimeMs = bfsTimeNano / 1_000_000;

        // Algorithm 2: Dynamic Programming
        long dpStart = System.nanoTime();
        int dpResult = dynamicProgramming(N, ladders, snakes);
        long dpTimeNano = System.nanoTime() - dpStart;
        long dpTimeMicro = dpTimeNano / 1000;
        long dpTimeMs = dpTimeNano / 1_000_000;

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

    // SUBMIT ANSWER
    public ApiResponse submitAnswer(PlayerAnswerRequest req) {
        // Validation
        if (req.getPlayerName() == null || req.getPlayerName().trim().isEmpty()) {
            return new ApiResponse(false, "Player name is required");
        }

        int userAnswer;
        try {
            userAnswer = Integer.parseInt(req.getAnswerText());
        } catch (NumberFormatException e) {
            return new ApiResponse(false, "Invalid answer format. Please enter a number.");
        }

        Long roundId = req.getRoundId();
        if (roundId == null) {
            return new ApiResponse(false, "No active game round. Please start a new game.");
        }

        Integer correctAnswer = answerStore.get(roundId);
        if (correctAnswer == null) {
            return new ApiResponse(false, "Game round expired. Please start a new game.");
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

        long bfsTimeMs = bfsTimeMicro / 1000;
        long dpTimeMs = dpTimeMicro / 1000;
        long bfsTimeForDisplay = bfsTimeMs > 0 ? bfsTimeMs : (bfsTimeMicro > 0 ? 1 : 0);
        long dpTimeForDisplay = dpTimeMs > 0 ? dpTimeMs : (dpTimeMicro > 0 ? 1 : 0);

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
            result.setRoundNumber(new Random().nextInt(1000));

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
            String comparisonMessage = String.format("BFS took %d μs (%d ms), DP took %d μs (%d ms). %s was faster.",
                    bfsTimeMicro, bfsTimeForDisplay, dpTimeMicro, dpTimeForDisplay, bestAlgorithm);

            return new ApiResponse(
                    true,
                    "✅ Correct! Well done! The minimum dice throws needed is " + correctAnswer,
                    true,
                    false,
                    bfsTimeForDisplay,
                    dpTimeForDisplay,
                    bfsTimeForDisplay + dpTimeForDisplay,
                    bestAlgorithm,
                    comparisonMessage,
                    false
            );
        } else {
            String bestAlgorithm = bfsTimeMicro <= dpTimeMicro ? "BFS" : "Dynamic Programming";
            String comparisonMessage = String.format("BFS took %d μs, DP took %d μs.", bfsTimeMicro, dpTimeMicro);

            return new ApiResponse(
                    true,
                    "❌ Incorrect! The correct answer is " + correctAnswer + ". Try again!",
                    false,
                    false,
                    bfsTimeForDisplay,
                    dpTimeForDisplay,
                    bfsTimeForDisplay + dpTimeForDisplay,
                    bestAlgorithm,
                    comparisonMessage,
                    false
            );
        }
    }

    // ==================== HELPER METHODS ====================

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
}