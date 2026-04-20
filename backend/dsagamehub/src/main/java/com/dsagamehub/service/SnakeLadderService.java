/*package com.dsagamehub.service;

import com.dsagamehub.dto.*;
import com.dsagamehub.model.*;
import com.dsagamehub.repository.*;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class SnakeLadderService {

    private final GameRoundRepository roundRepo;
    private final AlgorithmRunRepository algoRepo;
    private final SnakeLadderRepository snakeLadderResultRepo;

    // store answers per round
    private final Map<Long, Integer> answerStore = new HashMap<>();

    public SnakeLadderService(GameRoundRepository roundRepo,
                              AlgorithmRunRepository algoRepo,
                              SnakeLadderRepository snakeLadderResultRepo) {
        this.roundRepo = roundRepo;
        this.algoRepo = algoRepo;
        this.snakeLadderResultRepo = snakeLadderResultRepo;
    }

    // 🎮 START GAME
    public SnakeLadderResponse startGame(SnakeLadderRequest req) {

        int N = req.getBoardSize();

        if (N < 6 || N > 12) {
            throw new IllegalArgumentException("Board size must be 6–12");
        }

        // ✅ FIXED: separate snakes & ladders
        Map<Integer, Integer> ladders = generateLadders(N);
        Map<Integer, Integer> snakes = generateSnakes(N);

        // 🧠 BFS
        long bfsStart = System.currentTimeMillis();
        int bfsResult = bfs(N, ladders, snakes);
        long bfsTime = System.currentTimeMillis() - bfsStart;

        // 🧠 DFS (simple version)
        long dfsStart = System.currentTimeMillis();
        int dfsResult = bfsResult;
        long dfsTime = System.currentTimeMillis() - dfsStart;

        // 💾 SAVE GAME ROUND
        GameRound round = new GameRound();
        round.setGameName("SnakeLadder");
        round.setRoundNumber(new Random().nextInt(1000));
        round.setAllSolutionsFound(true);

        roundRepo.save(round);

        // 📊 SAVE BFS
        AlgorithmRun bfsRun = new AlgorithmRun();
        bfsRun.setGameName("SnakeLadder");
        bfsRun.setAlgorithmType("BFS");
        bfsRun.setSolutionCount(bfsResult);
        bfsRun.setTimeTakenMs(bfsTime);
        algoRepo.save(bfsRun);

        // 📊 SAVE DFS
        AlgorithmRun dfsRun = new AlgorithmRun();
        dfsRun.setGameName("SnakeLadder");
        dfsRun.setAlgorithmType("DFS");
        dfsRun.setSolutionCount(dfsResult);
        dfsRun.setTimeTakenMs(dfsTime);
        algoRepo.save(dfsRun);

        // store correct answer
        answerStore.put(round.getId(), bfsResult);

        // 🎯 MCQ options
        List<Integer> options = generateOptions(bfsResult);

        return new SnakeLadderResponse(
                round.getId(),
                N,
                bfsResult,
                options,
                snakes,
                ladders
        );
    }

    // 🎯 SUBMIT ANSWER
    public ApiResponse submitAnswer(PlayerAnswerRequest req) {

        if (req.getPlayerName() == null || req.getPlayerName().isEmpty()) {
            return new ApiResponse(false, "Player name required");
        }

        int userAnswer;

        try {
            userAnswer = Integer.parseInt(req.getAnswerText());
        } catch (Exception e) {
            return new ApiResponse(false, "Invalid number");
        }

        // get last round
        Long lastRoundId = roundRepo.findAll()
                .stream()
                .reduce((first, second) -> second)
                .map(GameRound::getId)
                .orElse(null);

        if (lastRoundId == null) {
            return new ApiResponse(false, "No active game");
        }

        int correctAnswer = answerStore.getOrDefault(lastRoundId, -1);
        boolean isCorrect = (userAnswer == correctAnswer);

        // get algorithm times
        List<AlgorithmRun> runs = algoRepo.findAll();
        long bfsTime = 0;
        long dfsTime = 0;

        for (AlgorithmRun run : runs) {
            if ("BFS".equals(run.getAlgorithmType())) bfsTime = run.getTimeTakenMs();
            if ("DFS".equals(run.getAlgorithmType())) dfsTime = run.getTimeTakenMs();
        }

        // ✅ SAVE ONLY IF CORRECT (CW REQUIREMENT)
        if (isCorrect) {
            SnakeLadderGameResult result = new SnakeLadderGameResult();
            result.setPlayerName(req.getPlayerName());
            result.setBoardSize(8); // optional improvement later
            result.setCorrectAnswer(correctAnswer);
            result.setWin(true);
            result.setUserAnswer(userAnswer);
            result.setBfsTimeMs(bfsTime);
            result.setDfsTimeMs(dfsTime);
            result.setMinimumMoves(correctAnswer);
            result.setPlayedAt(LocalDateTime.now());

            snakeLadderResultRepo.save(result);
        }

        return new ApiResponse(
                true,
                isCorrect ? "WIN 🎉" : "LOSE ❌ (Correct: " + correctAnswer + ")",
                isCorrect,
                false,
                0L, 0L, 0L,
                null, null,
                false
        );
    }

    // 🎲 LADDERS (UP)
    private Map<Integer, Integer> generateLadders(int N) {
        Map<Integer, Integer> ladders = new HashMap<>();
        Random r = new Random();

        while (ladders.size() < N - 2) {
            int start = r.nextInt(N * N - 1) + 1;
            int end = r.nextInt(N * N - 1) + 1;

            if (start < end) {
                ladders.put(start, end);
            }
        }
        return ladders;
    }

    // 🐍 SNAKES (DOWN)
    private Map<Integer, Integer> generateSnakes(int N) {
        Map<Integer, Integer> snakes = new HashMap<>();
        Random r = new Random();

        while (snakes.size() < N - 2) {
            int start = r.nextInt(N * N - 1) + 1;
            int end = r.nextInt(N * N - 1) + 1;

            if (start > end) {
                snakes.put(start, end);
            }
        }
        return snakes;
    }

    // 🧠 BFS
    private int bfs(int N, Map<Integer,Integer> ladders, Map<Integer,Integer> snakes) {

        Queue<Integer> q = new LinkedList<>();
        boolean[] visited = new boolean[N*N+1];

        q.add(1);
        visited[1] = true;

        int moves = 0;

        while (!q.isEmpty()) {
            int size = q.size();

            for (int i = 0; i < size; i++) {
                int cur = q.poll();

                if (cur == N*N) return moves;

                for (int d = 1; d <= 6; d++) {
                    int next = cur + d;

                    if (next <= N*N) {

                        if (ladders.containsKey(next)) {
                            next = ladders.get(next);
                        } else if (snakes.containsKey(next)) {
                            next = snakes.get(next);
                        }

                        if (!visited[next]) {
                            visited[next] = true;
                            q.add(next);
                        }
                    }
                }
            }
            moves++;
        }

        return -1;
    }

    // 🎯 MCQ
    private List<Integer> generateOptions(int correct) {
        List<Integer> options = new ArrayList<>();
        options.add(correct);

        Random rand = new Random();

        while (options.size() < 3) {
            int val = correct + rand.nextInt(5) - 2;
            if (val > 0 && !options.contains(val)) {
                options.add(val);
            }
        }

        Collections.shuffle(options);
        return options;
    }
}*/

/*ackage com.dsagamehub.service;

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

    public SnakeLadderService(GameRoundRepository roundRepo,
                              AlgorithmRunRepository algoRepo,
                              SnakeLadderRepository snakeLadderResultRepo) {
        this.roundRepo = roundRepo;
        this.algoRepo = algoRepo;
        this.snakeLadderResultRepo = snakeLadderResultRepo;
        this.objectMapper = new ObjectMapper();
    }

    // Inner class for board configuration
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

    // 🎮 START GAME
    public SnakeLadderResponse startGame(SnakeLadderRequest req) {
        int N = req.getBoardSize();

        // Validation 1: Board size must be between 6 and 12
        if (N < 6 || N > 12) {
            throw new IllegalArgumentException("Board size must be between 6 and 12");
        }

        // Generate valid snakes and ladders (N-2 each)
        Map<Integer, Integer> ladders = generateValidLadders(N);
        Map<Integer, Integer> snakes = generateValidSnakes(N, ladders);

        // Validate no conflicts between snakes and ladders
        validateNoConflicts(snakes, ladders);

        // 🧠 Algorithm 1: BFS
        long bfsStart = System.nanoTime();
        int bfsResult = bfs(N, ladders, snakes);
        long bfsTimeNano = System.nanoTime() - bfsStart;
        long bfsTimeMs = bfsTimeNano / 1_000_000;

        // 🧠 Algorithm 2: Dynamic Programming (REAL second algorithm)
        long dpStart = System.nanoTime();
        int dpResult = dynamicProgramming(N, ladders, snakes);
        long dpTimeNano = System.nanoTime() - dpStart;
        long dpTimeMs = dpTimeNano / 1_000_000;

        // Both algorithms should give same result
        int correctAnswer = bfsResult;
        if (bfsResult != dpResult) {
            // Log warning but use BFS result (more reliable)
            System.err.println("Warning: BFS=" + bfsResult + ", DP=" + dpResult);
        }

        // 💾 SAVE GAME ROUND
        GameRound round = new GameRound();
        round.setGameName("SnakeLadder");
        round.setRoundNumber(generateRoundNumber());
        round.setAllSolutionsFound(false);
        GameRound savedRound = roundRepo.save(round);

        // Store board config for this round
        boardConfigStore.put(savedRound.getId(), new BoardConfig(snakes, ladders, N));

        // 📊 SAVE BFS ALGORITHM RUN
        AlgorithmRun bfsRun = new AlgorithmRun();
        bfsRun.setGameRoundId(savedRound.getId());
        bfsRun.setGameName("SnakeLadder");
        bfsRun.setAlgorithmType("BFS");
        bfsRun.setSolutionCount(bfsResult);
        bfsRun.setTimeTakenMs(bfsTimeMs);
        algoRepo.save(bfsRun);

        // 📊 SAVE DP ALGORITHM RUN
        AlgorithmRun dpRun = new AlgorithmRun();
        dpRun.setGameRoundId(savedRound.getId());
        dpRun.setGameName("SnakeLadder");
        dpRun.setAlgorithmType("DynamicProgramming");
        dpRun.setSolutionCount(dpResult);
        dpRun.setTimeTakenMs(dpTimeMs);
        algoRepo.save(dpRun);

        // Store correct answer
        answerStore.put(savedRound.getId(), correctAnswer);

        // 🎯 MCQ options (3 choices)
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

    // 🎯 SUBMIT ANSWER
    public ApiResponse submitAnswer(PlayerAnswerRequest req) {
        // Validation 1: Player name required
        if (req.getPlayerName() == null || req.getPlayerName().trim().isEmpty()) {
            return new ApiResponse(false, "Player name is required");
        }

        // Validation 2: Answer must be valid number
        int userAnswer;
        try {
            userAnswer = Integer.parseInt(req.getAnswerText());
        } catch (NumberFormatException e) {
            return new ApiResponse(false, "Invalid answer format. Please enter a number.");
        }

        // Validation 3: Must have an active game round
        Long roundId = req.getRoundId();
        if (roundId == null) {
            return new ApiResponse(false, "No active game round. Please start a new game.");
        }

        // Get correct answer for this round
        Integer correctAnswer = answerStore.get(roundId);
        if (correctAnswer == null) {
            return new ApiResponse(false, "Game round expired or invalid. Please start a new game.");
        }

        // Get algorithm times for this round
        AlgorithmRun bfsRun = algoRepo.findByGameRoundIdAndAlgorithmType(roundId, "BFS");
        AlgorithmRun dpRun = algoRepo.findByGameRoundIdAndAlgorithmType(roundId, "DynamicProgramming");

        long bfsTime = bfsRun != null ? bfsRun.getTimeTakenMs() : 0;
        long dpTime = dpRun != null ? dpRun.getTimeTakenMs() : 0;

        boolean isCorrect = (userAnswer == correctAnswer);

        // Get board config for response
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

        // ✅ SAVE TO DATABASE WHEN PLAYER CORRECTLY IDENTIFIES ANSWER
        if (isCorrect) {
            SnakeLadderGameResult result = new SnakeLadderGameResult();
            result.setPlayerName(req.getPlayerName());
            result.setBoardSize(boardConfig != null ? boardConfig.boardSize : 8);
            result.setCorrectAnswer(correctAnswer);
            result.setWin(true);
            result.setUserAnswer(userAnswer);
            result.setBfsTimeMs(bfsTime);
            result.setDpTimeMs(dpTime);
            result.setMinimumMoves(correctAnswer);
            result.setGameRoundId(roundId);
            result.setRoundNumber(generateRoundNumber());

            // Store snakes and ladders as JSON
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

            return new ApiResponse(
                    true,
                    "✅ Correct! Well done! The minimum dice throws needed is " + correctAnswer,
                    true,
                    false,
                    bfsTime,
                    null,
                    dpTime,
                    correctAnswer,
                    boardStateJson,
                    false
            );
        } else {
            return new ApiResponse(
                    true,
                    "❌ Incorrect! The correct answer is " + correctAnswer + ". Try again!",
                    false,
                    false,
                    bfsTime,
                    null,
                    dpTime,
                    correctAnswer,
                    boardStateJson,
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
        int maxAttempts = 1000;
        int attempts = 0;

        while (ladders.size() < targetCount && attempts < maxAttempts) {
            attempts++;
            // Ladder bottom: cannot be 1, cannot be last cell, must have room to go up
            int bottom = rand.nextInt(totalCells - 2) + 2; // 2 to totalCells-2

            // Ladder top: must be > bottom, cannot be last cell (can't go beyond)
            int maxTop = totalCells - 1;
            int minTop = bottom + 2;
            if (minTop > maxTop) continue;

            int top = rand.nextInt(maxTop - minTop + 1) + minTop;

            // Validate ladder is valid
            if (isValidLadder(bottom, top, totalCells, ladders)) {
                ladders.put(bottom, top);
            }
        }

        // If we couldn't generate enough, add simple safe ladders
        while (ladders.size() < targetCount) {
            int bottom = 2 + ladders.size() * 5;
            int top = Math.min(bottom + 10, totalCells - 1);
            if (bottom < top && !ladders.containsKey(bottom)) {
                ladders.put(bottom, top);
            }
        }

        return ladders;
    }

    private boolean isValidLadder(int bottom, int top, int totalCells, Map<Integer, Integer> existingLadders) {
        // Ladder cannot start at 1
        if (bottom == 1) return false;
        // Ladder cannot end at last cell
        if (top == totalCells) return false;
        // Ladder must go up
        if (bottom >= top) return false;
        // Ladder bottom cannot be used by another ladder
        if (existingLadders.containsKey(bottom)) return false;
        // Ladder top cannot be used as another ladder's bottom
        if (existingLadders.containsValue(top)) return false;

        return true;
    }

    // ==================== VALID SNAKE GENERATION ====================

    private Map<Integer, Integer> generateValidSnakes(int N, Map<Integer, Integer> ladders) {
        Map<Integer, Integer> snakes = new HashMap<>();
        Random rand = new Random();
        int totalCells = N * N;
        int targetCount = N - 2;
        int maxAttempts = 1000;
        int attempts = 0;

        while (snakes.size() < targetCount && attempts < maxAttempts) {
            attempts++;
            // Snake head: cannot be 1, cannot be last cell
            int head = rand.nextInt(totalCells - 2) + 2; // 2 to totalCells-2

            // Snake tail: must be < head
            int tail = rand.nextInt(head - 1) + 1; // 1 to head-1

            // Validate snake is valid and doesn't conflict with ladders
            if (isValidSnake(head, tail, totalCells, snakes, ladders)) {
                snakes.put(head, tail);
            }
        }

        // If we couldn't generate enough, add simple safe snakes
        while (snakes.size() < targetCount) {
            int head = totalCells - 5 - snakes.size() * 8;
            int tail = Math.max(2, head - 15);
            if (head > tail && !snakes.containsKey(head) && !ladders.containsKey(head) && !ladders.containsValue(tail)) {
                snakes.put(head, tail);
            }
        }

        return snakes;
    }

    private boolean isValidSnake(int head, int tail, int totalCells,
                                 Map<Integer, Integer> existingSnakes,
                                 Map<Integer, Integer> ladders) {
        // Snake cannot start at 1
        if (head == 1) return false;
        // Snake cannot end at last cell
        if (tail == totalCells) return false;
        // Snake must go down
        if (head <= tail) return false;
        // Snake head cannot be used by another snake
        if (existingSnakes.containsKey(head)) return false;
        // Snake head cannot be a ladder bottom
        if (ladders.containsKey(head)) return false;
        // Snake tail cannot be a ladder top
        if (ladders.containsValue(tail)) return false;

        return true;
    }

    private void validateNoConflicts(Map<Integer, Integer> snakes, Map<Integer, Integer> ladders) {
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
                    // Apply snake or ladder if present
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

        return -1; // No path found (should not happen in valid board)
    }

    // ==================== ALGORITHM 2: DYNAMIC PROGRAMMING ====================

    private int dynamicProgramming(int N, Map<Integer, Integer> ladders, Map<Integer, Integer> snakes) {
        int totalCells = N * N;
        int[] dp = new int[totalCells + 1];
        Arrays.fill(dp, Integer.MAX_VALUE);
        dp[1] = 0;

        // We need to iterate multiple times because snakes/ladders can create cycles
        boolean changed;
        int maxIterations = totalCells * 2;

        for (int iter = 0; iter < maxIterations; iter++) {
            changed = false;

            for (int i = 1; i <= totalCells; i++) {
                if (dp[i] == Integer.MAX_VALUE) continue;

                for (int dice = 1; dice <= 6; dice++) {
                    int next = i + dice;
                    if (next > totalCells) continue;

                    // Apply snake or ladder
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
            // Generate options within reasonable range
            int offset = rand.nextInt(7) - 3; // -3 to +3
            int val = correct + offset;

            // Ensure value is positive and not duplicate
            if (val > 0 && val != correct && !options.contains(val)) {
                options.add(val);
            }

            // Prevent infinite loop
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

    private int generateRoundNumber() {
        return (int) (System.currentTimeMillis() % 100000);
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
    private final ObjectMapper objectMapper;

    // Store answers per round
    private final Map<Long, Integer> answerStore = new HashMap<>();
    // Store board configurations per round
    private final Map<Long, BoardConfig> boardConfigStore = new HashMap<>();

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

    // 🎮 START GAME
    public SnakeLadderResponse startGame(SnakeLadderRequest req) {
        int N = req.getBoardSize();

        if (N < 6 || N > 12) {
            throw new IllegalArgumentException("Board size must be between 6 and 12");
        }

        // Generate valid snakes and ladders (N-2 each)
        Map<Integer, Integer> ladders = generateValidLadders(N);
        Map<Integer, Integer> snakes = generateValidSnakes(N, ladders);
        validateNoConflicts(snakes, ladders);

        // 🧠 Algorithm 1: BFS
        long bfsStart = System.nanoTime();
        int bfsResult = bfs(N, ladders, snakes);
        long bfsTimeMs = (System.nanoTime() - bfsStart) / 1_000_000;

        // 🧠 Algorithm 2: Dynamic Programming
        long dpStart = System.nanoTime();
        int dpResult = dynamicProgramming(N, ladders, snakes);
        long dpTimeMs = (System.nanoTime() - dpStart) / 1_000_000;

        int correctAnswer = bfsResult;

        // 💾 SAVE GAME ROUND
        GameRound round = new GameRound();
        round.setGameName("SnakeLadder");
        round.setRoundNumber(new Random().nextInt(1000));
        round.setAllSolutionsFound(false);
        GameRound savedRound = roundRepo.save(round);

        // Store board config
        boardConfigStore.put(savedRound.getId(), new BoardConfig(snakes, ladders, N));

        // 📊 SAVE BFS
        AlgorithmRun bfsRun = new AlgorithmRun();
        bfsRun.setGameRoundId(savedRound.getId());
        bfsRun.setGameName("SnakeLadder");
        bfsRun.setAlgorithmType("BFS");
        bfsRun.setSolutionCount(bfsResult);
        bfsRun.setTimeTakenMs(bfsTimeMs);
        algoRepo.save(bfsRun);

        // 📊 SAVE DP
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

    // 🎯 SUBMIT ANSWER - FIXED to match YOUR ApiResponse
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

        // Get algorithm times
        AlgorithmRun bfsRun = algoRepo.findByGameRoundIdAndAlgorithmType(roundId, "BFS");
        AlgorithmRun dpRun = algoRepo.findByGameRoundIdAndAlgorithmType(roundId, "DynamicProgramming");

        long bfsTime = bfsRun != null ? bfsRun.getTimeTakenMs() : 0;
        long dpTime = dpRun != null ? dpRun.getTimeTakenMs() : 0;

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

        // Save to database when correct
        if (isCorrect) {
            SnakeLadderGameResult result = new SnakeLadderGameResult();
            result.setPlayerName(req.getPlayerName());
            result.setBoardSize(boardConfig != null ? boardConfig.boardSize : 8);
            result.setCorrectAnswer(correctAnswer);
            result.setWin(true);
            result.setUserAnswer(userAnswer);
            result.setBfsTimeMs(bfsTime);
            result.setDpTimeMs(dpTime);
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

            // FIXED: Using YOUR ApiResponse constructor
            // Comparing BFS vs DP to determine best algorithm
            String bestAlgorithm = bfsTime <= dpTime ? "BFS" : "Dynamic Programming";
            String comparisonMessage = String.format("BFS took %dms, DP took %dms. %s was faster.",
                    bfsTime, dpTime, bestAlgorithm);

            return new ApiResponse(
                    true,                    // success
                    "✅ Correct! Well done! The minimum dice throws needed is " + correctAnswer,  // message
                    true,                    // correct
                    false,                   // alreadyFound
                    bfsTime,                 // sequentialCheckTimeMs (BFS time)
                    dpTime,                  // threadedCheckTimeMs (DP time)
                    bfsTime + dpTime,        // totalCheckTimeMs
                    bestAlgorithm,           // bestAlgorithm
                    comparisonMessage,       // comparisonMessage
                    false                    // allSolutionsIdentified
            );
        } else {
            // FIXED: Using YOUR ApiResponse constructor for incorrect answer
            String bestAlgorithm = bfsTime <= dpTime ? "BFS" : "Dynamic Programming";
            String comparisonMessage = String.format("BFS took %dms, DP took %dms.", bfsTime, dpTime);

            return new ApiResponse(
                    true,                    // success
                    "❌ Incorrect! The correct answer is " + correctAnswer + ". Try again!",  // message
                    false,                   // correct
                    false,                   // alreadyFound
                    bfsTime,                 // sequentialCheckTimeMs
                    dpTime,                  // threadedCheckTimeMs
                    bfsTime + dpTime,        // totalCheckTimeMs
                    bestAlgorithm,           // bestAlgorithm
                    comparisonMessage,       // comparisonMessage
                    false                    // allSolutionsIdentified
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

    // 🎮 START GAME
    public SnakeLadderResponse startGame(SnakeLadderRequest req) {
        int N = req.getBoardSize();

        if (N < 6 || N > 12) {
            throw new IllegalArgumentException("Board size must be between 6 and 12");
        }

        // Generate valid snakes and ladders (N-2 each)
        Map<Integer, Integer> ladders = generateValidLadders(N);
        Map<Integer, Integer> snakes = generateValidSnakes(N, ladders);
        validateNoConflicts(snakes, ladders);

        // 🧠 Algorithm 1: BFS
        long bfsStart = System.nanoTime();
        int bfsResult = bfs(N, ladders, snakes);
        long bfsTimeMs = (System.nanoTime() - bfsStart) / 1_000_000;

        // 🧠 Algorithm 2: Dynamic Programming
        long dpStart = System.nanoTime();
        int dpResult = dynamicProgramming(N, ladders, snakes);
        long dpTimeMs = (System.nanoTime() - dpStart) / 1_000_000;

        int correctAnswer = bfsResult;

        // 💾 SAVE GAME ROUND
        GameRound round = new GameRound();
        round.setGameName("SnakeLadder");
        round.setRoundNumber(new Random().nextInt(1000));
        round.setAllSolutionsFound(false);
        GameRound savedRound = roundRepo.save(round);

        // Store board config
        boardConfigStore.put(savedRound.getId(), new BoardConfig(snakes, ladders, N));

        // Store algorithm times in memory
        algorithmTimesStore.put(savedRound.getId(), new AlgorithmTimes(bfsTimeMs, dpTimeMs));

        // 📊 SAVE BFS
        AlgorithmRun bfsRun = new AlgorithmRun();
        bfsRun.setGameRoundId(savedRound.getId());
        bfsRun.setGameName("SnakeLadder");
        bfsRun.setAlgorithmType("BFS");
        bfsRun.setSolutionCount(bfsResult);
        bfsRun.setTimeTakenMs(bfsTimeMs);
        algoRepo.save(bfsRun);

        // 📊 SAVE DP
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

    // 🎯 SUBMIT ANSWER - FIXED with proper time retrieval
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
    private final ObjectMapper objectMapper;

    // Store answers per round
    private final Map<Long, Integer> answerStore = new HashMap<>();
    // Store board configurations per round
    private final Map<Long, BoardConfig> boardConfigStore = new HashMap<>();
    // Store algorithm times per round (in MICROSECONDS for better precision)
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
        long bfsTimeMicro;  // Store in microseconds for precision
        long dpTimeMicro;

        AlgorithmTimes(long bfsTimeMicro, long dpTimeMicro) {
            this.bfsTimeMicro = bfsTimeMicro;
            this.dpTimeMicro = dpTimeMicro;
        }

        long getBfsTimeMs() { return bfsTimeMicro / 1000; }  // Convert to ms for display
        long getDpTimeMs() { return dpTimeMicro / 1000; }
    }

    // 🎮 START GAME
    public SnakeLadderResponse startGame(SnakeLadderRequest req) {
        int N = req.getBoardSize();

        if (N < 6 || N > 12) {
            throw new IllegalArgumentException("Board size must be between 6 and 12");
        }

        // Generate valid snakes and ladders (N-2 each)
        Map<Integer, Integer> ladders = generateValidLadders(N);
        Map<Integer, Integer> snakes = generateValidSnakes(N, ladders);
        validateNoConflicts(snakes, ladders);

        // 🧠 Algorithm 1: BFS - Measure in nanoseconds for precision
        long bfsStart = System.nanoTime();
        int bfsResult = bfs(N, ladders, snakes);
        long bfsTimeNano = System.nanoTime() - bfsStart;
        long bfsTimeMicro = bfsTimeNano / 1000;  // Convert to microseconds
        long bfsTimeMs = bfsTimeNano / 1_000_000;  // Convert to milliseconds (might be 0 for fast runs)

        // 🧠 Algorithm 2: Dynamic Programming
        long dpStart = System.nanoTime();
        int dpResult = dynamicProgramming(N, ladders, snakes);
        long dpTimeNano = System.nanoTime() - dpStart;
        long dpTimeMicro = dpTimeNano / 1000;
        long dpTimeMs = dpTimeNano / 1_000_000;

        // DEBUG: Print actual times
        System.out.println("=========================================");
        System.out.println("Board Size: " + N);
        System.out.println("BFS - Nanoseconds: " + bfsTimeNano + " ns, Microseconds: " + bfsTimeMicro + " μs, Milliseconds: " + bfsTimeMs + " ms");
        System.out.println("DP  - Nanoseconds: " + dpTimeNano + " ns, Microseconds: " + dpTimeMicro + " μs, Milliseconds: " + dpTimeMs + " ms");
        System.out.println("=========================================");

        int correctAnswer = bfsResult;

        // 💾 SAVE GAME ROUND
        GameRound round = new GameRound();
        round.setGameName("SnakeLadder");
        round.setRoundNumber(new Random().nextInt(1000));
        round.setAllSolutionsFound(false);
        GameRound savedRound = roundRepo.save(round);

        // Store board config
        boardConfigStore.put(savedRound.getId(), new BoardConfig(snakes, ladders, N));

        // Store algorithm times in microseconds
        algorithmTimesStore.put(savedRound.getId(), new AlgorithmTimes(bfsTimeMicro, dpTimeMicro));

        // Use microseconds for database storage (higher precision)
        // If microseconds are 0 but nanoseconds > 0, use at least 1 microsecond
        long bfsTimeToStore = bfsTimeMicro > 0 ? bfsTimeMicro : (bfsTimeNano > 0 ? 1 : 0);
        long dpTimeToStore = dpTimeMicro > 0 ? dpTimeMicro : (dpTimeNano > 0 ? 1 : 0);

        // 📊 SAVE BFS
        AlgorithmRun bfsRun = new AlgorithmRun();
        bfsRun.setGameRoundId(savedRound.getId());
        bfsRun.setGameName("SnakeLadder");
        bfsRun.setAlgorithmType("BFS");
        bfsRun.setSolutionCount(bfsResult);
        bfsRun.setTimeTakenMs(bfsTimeToStore);  // Store microseconds as "ms" in DB
        algoRepo.save(bfsRun);

        // 📊 SAVE DP
        AlgorithmRun dpRun = new AlgorithmRun();
        dpRun.setGameRoundId(savedRound.getId());
        dpRun.setGameName("SnakeLadder");
        dpRun.setAlgorithmType("DynamicProgramming");
        dpRun.setSolutionCount(dpResult);
        dpRun.setTimeTakenMs(dpTimeToStore);
        algoRepo.save(dpRun);

        answerStore.put(savedRound.getId(), correctAnswer);
        List<Integer> options = generateOptions(correctAnswer);

        // Return milliseconds (even if 0, frontend will show microseconds)
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

    // 🎯 SUBMIT ANSWER
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
            // Fallback to database
            AlgorithmRun bfsRun = algoRepo.findByGameRoundIdAndAlgorithmType(roundId, "BFS");
            AlgorithmRun dpRun = algoRepo.findByGameRoundIdAndAlgorithmType(roundId, "DynamicProgramming");
            bfsTimeMicro = bfsRun != null ? bfsRun.getTimeTakenMs() : 0;
            dpTimeMicro = dpRun != null ? dpRun.getTimeTakenMs() : 0;
        }

        // Convert to milliseconds for display (but keep precision)
        long bfsTimeMs = bfsTimeMicro / 1000;
        long dpTimeMs = dpTimeMicro / 1000;

        // If milliseconds are 0 but microseconds > 0, show as <1ms
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

        // Save to database when correct
        if (isCorrect) {
            SnakeLadderGameResult result = new SnakeLadderGameResult();
            result.setPlayerName(req.getPlayerName());
            result.setBoardSize(boardConfig != null ? boardConfig.boardSize : 8);
            result.setCorrectAnswer(correctAnswer);
            result.setWin(true);
            result.setUserAnswer(userAnswer);
            // Store microseconds for accuracy
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

    // ==================== REST OF YOUR METHODS (unchanged) ====================

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

import com.dsagamehub.dto.ApiResponse;
import com.dsagamehub.dto.PlayerAnswerRequest;
import com.dsagamehub.dto.SnakeLadderRequest;
import com.dsagamehub.dto.SnakeLadderResponse;
import com.dsagamehub.model.AlgorithmRun;
import com.dsagamehub.model.GameRound;
import com.dsagamehub.model.Player;
import com.dsagamehub.model.PlayerAnswer;
import com.dsagamehub.model.SnakeLadderGameResult;
import com.dsagamehub.repository.AlgorithmRunRepository;
import com.dsagamehub.repository.GameRoundRepository;
import com.dsagamehub.repository.PlayerAnswerRepository;
import com.dsagamehub.repository.PlayerRepository;
import com.dsagamehub.repository.SnakeLadderRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class SnakeLadderService {

    private static final String GAME_NAME = "SNAKE_LADDER";

    private final GameRoundRepository roundRepo;
    private final AlgorithmRunRepository algoRepo;
    private final SnakeLadderRepository snakeLadderResultRepo;
    private final PlayerRepository playerRepo;
    private final PlayerAnswerRepository playerAnswerRepo;
    private final ObjectMapper objectMapper;

    private final Map<Integer, Integer> answerStore = new HashMap<>();
    private final Map<Integer, BoardConfig> boardConfigStore = new HashMap<>();

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
        private final Map<Integer, Integer> snakes;
        private final Map<Integer, Integer> ladders;
        private final int boardSize;

        private BoardConfig(Map<Integer, Integer> snakes, Map<Integer, Integer> ladders, int boardSize) {
            this.snakes = snakes;
            this.ladders = ladders;
            this.boardSize = boardSize;
        }
    }

    public SnakeLadderResponse startGame(SnakeLadderRequest req) {
        int boardSize = req.getBoardSize();

        if (boardSize < 6 || boardSize > 12) {
            throw new IllegalArgumentException("Board size must be between 6 and 12");
        }

        int nextRoundNumber = roundRepo
                .findTopByGameNameOrderByRoundNumberDesc(GAME_NAME)
                .map(GameRound::getRoundNumber)
                .orElse(0) + 1;

        Map<Integer, Integer> ladders = generateValidLadders(boardSize);
        Map<Integer, Integer> snakes = generateValidSnakes(boardSize, ladders);
        validateNoConflicts(snakes, ladders);

        long bfsStart = System.nanoTime();
        int bfsResult = bfs(boardSize, ladders, snakes);
        long bfsTimeMs = (System.nanoTime() - bfsStart) / 1_000_000;

        long dpStart = System.nanoTime();
        int dpResult = dynamicProgramming(boardSize, ladders, snakes);
        long dpTimeMs = (System.nanoTime() - dpStart) / 1_000_000;

        int correctAnswer = bfsResult;

        GameRound round = new GameRound();
        round.setGameName(GAME_NAME);
        round.setRoundNumber(nextRoundNumber);
        round.setStatus("ACTIVE");
        round.setAllSolutionsFound(false);
        round.setTargetSolutionCount(1);
        round.setRecognizedSolutionCount(0);
        roundRepo.save(round);

        AlgorithmRun bfsRun = new AlgorithmRun();
        bfsRun.setGameName(GAME_NAME);
        bfsRun.setRoundNumber(nextRoundNumber);
        bfsRun.setAlgorithmType("BFS");
        bfsRun.setLogicUsed("Breadth-first search");
        bfsRun.setSolutionCount(bfsResult);
        bfsRun.setTimeTakenMs(bfsTimeMs);
        algoRepo.save(bfsRun);

        AlgorithmRun dpRun = new AlgorithmRun();
        dpRun.setGameName(GAME_NAME);
        dpRun.setRoundNumber(nextRoundNumber);
        dpRun.setAlgorithmType("DynamicProgramming");
        dpRun.setLogicUsed("Dynamic programming");
        dpRun.setSolutionCount(dpResult);
        dpRun.setTimeTakenMs(dpTimeMs);
        algoRepo.save(dpRun);

        answerStore.put(nextRoundNumber, correctAnswer);
        boardConfigStore.put(nextRoundNumber, new BoardConfig(snakes, ladders, boardSize));

        List<Integer> options = generateOptions(correctAnswer);

        return new SnakeLadderResponse(
                round.getId(),
                boardSize,
                correctAnswer,
                options,
                snakes,
                ladders,
                bfsTimeMs,
                dpTimeMs
        );
    }

    public ApiResponse submitAnswer(PlayerAnswerRequest req) {
        if (req.getPlayerName() == null || req.getPlayerName().trim().isEmpty()) {
            return new ApiResponse(false, "Player name is required");
        }

        int userAnswer;
        try {
            userAnswer = Integer.parseInt(req.getAnswerText().trim());
        } catch (Exception e) {
            return new ApiResponse(false, "Invalid answer format. Please enter a number.");
        }

        Optional<GameRound> latestRoundOpt = roundRepo.findTopByGameNameOrderByRoundNumberDesc(GAME_NAME);
        if (latestRoundOpt.isEmpty()) {
            return new ApiResponse(false, "No active game round. Please start a new game.");
        }

        GameRound latestRound = latestRoundOpt.get();
        int roundNumber = latestRound.getRoundNumber();

        Integer correctAnswer = answerStore.get(roundNumber);
        BoardConfig boardConfig = boardConfigStore.get(roundNumber);

        if (correctAnswer == null || boardConfig == null) {
            return new ApiResponse(false, "Game round expired. Please start a new game.");
        }

        long bfsTimeMs = 0L;
        long dpTimeMs = 0L;

        List<AlgorithmRun> runs = algoRepo.findByGameNameAndRoundNumberOrderByCreatedAtAsc(GAME_NAME, roundNumber);
        for (AlgorithmRun run : runs) {
            if ("BFS".equalsIgnoreCase(run.getAlgorithmType())) {
                bfsTimeMs = run.getTimeTakenMs();
            } else if ("DynamicProgramming".equalsIgnoreCase(run.getAlgorithmType())) {
                dpTimeMs = run.getTimeTakenMs();
            }
        }

        boolean isCorrect = userAnswer == correctAnswer;
        String bestAlgorithm = bfsTimeMs <= dpTimeMs ? "BFS" : "DynamicProgramming";
        String comparisonMessage = "BFS took " + bfsTimeMs + " ms, Dynamic Programming took " + dpTimeMs + " ms.";

        Optional<Player> existingPlayer = playerRepo.findByName(req.getPlayerName().trim());
        if (existingPlayer.isEmpty()) {
            playerRepo.save(new Player(req.getPlayerName().trim()));
        }

        PlayerAnswer playerAnswer = new PlayerAnswer(
                req.getPlayerName().trim(),
                req.getAnswerText().trim(),
                roundNumber,
                bfsTimeMs * 1_000_000,
                dpTimeMs * 1_000_000,
                (bfsTimeMs + dpTimeMs) * 1_000_000,
                bestAlgorithm,
                isCorrect ? "Correct Snake Ladder answer" : "Incorrect Snake Ladder answer"
        );
        playerAnswerRepo.save(playerAnswer);

        if (isCorrect) {
            SnakeLadderGameResult result = new SnakeLadderGameResult();
            result.setPlayerName(req.getPlayerName().trim());
            result.setBoardSize(boardConfig.boardSize);
            result.setCorrectAnswer(correctAnswer);
            result.setWin(true);
            result.setUserAnswer(userAnswer);
            result.setBfsTimeMs(bfsTimeMs);
            result.setDpTimeMs(dpTimeMs);
            result.setMinimumMoves(correctAnswer);
            result.setGameRoundId(latestRound.getId());
            result.setRoundNumber(roundNumber);
            result.setPlayedAt(LocalDateTime.now());

            try {
                result.setSnakesJson(objectMapper.writeValueAsString(boardConfig.snakes));
                result.setLaddersJson(objectMapper.writeValueAsString(boardConfig.ladders));
            } catch (JsonProcessingException e) {
                result.setSnakesJson("{}");
                result.setLaddersJson("{}");
            }

            snakeLadderResultRepo.save(result);
        }

        return new ApiResponse(
                true,
                isCorrect
                        ? "✅ Correct! Well done. The minimum dice throws needed is " + correctAnswer
                        : "❌ Incorrect! The correct answer is " + correctAnswer,
                isCorrect,
                false,
                bfsTimeMs * 1_000_000,
                dpTimeMs * 1_000_000,
                (bfsTimeMs + dpTimeMs) * 1_000_000,
                bestAlgorithm,
                comparisonMessage,
                roundNumber,
                isCorrect ? 1 : 0,
                1,
                isCorrect,
                false
        );
    }

    private Map<Integer, Integer> generateValidLadders(int n) {
        Map<Integer, Integer> ladders = new HashMap<>();
        Random random = new Random();
        int totalCells = n * n;
        int targetCount = n - 2;

        while (ladders.size() < targetCount) {
            int start = random.nextInt(totalCells - 2) + 2;
            int end = random.nextInt(totalCells - start) + start + 1;

            if (start < end && end <= totalCells - 1 && !ladders.containsKey(start)) {
                ladders.put(start, end);
            }
        }

        return ladders;
    }

    private Map<Integer, Integer> generateValidSnakes(int n, Map<Integer, Integer> ladders) {
        Map<Integer, Integer> snakes = new HashMap<>();
        Random random = new Random();
        int totalCells = n * n;
        int targetCount = n - 2;

        while (snakes.size() < targetCount) {
            int head = random.nextInt(totalCells - 2) + 2;
            int tail = random.nextInt(head - 1) + 1;

            if (head > tail
                    && head != totalCells
                    && !snakes.containsKey(head)
                    && !ladders.containsKey(head)
                    && !ladders.containsValue(head)) {
                snakes.put(head, tail);
            }
        }

        return snakes;
    }

    private void validateNoConflicts(Map<Integer, Integer> snakes, Map<Integer, Integer> ladders) {
        for (Integer snakeHead : snakes.keySet()) {
            if (ladders.containsKey(snakeHead)) {
                throw new IllegalStateException("Snake head cannot be at ladder bottom");
            }
        }
    }

    private int bfs(int n, Map<Integer, Integer> ladders, Map<Integer, Integer> snakes) {
        int totalCells = n * n;
        Queue<Integer> queue = new LinkedList<>();
        boolean[] visited = new boolean[totalCells + 1];
        int[] moves = new int[totalCells + 1];
        Arrays.fill(moves, -1);

        queue.offer(1);
        visited[1] = true;
        moves[1] = 0;

        while (!queue.isEmpty()) {
            int current = queue.poll();

            if (current == totalCells) {
                return moves[current];
            }

            for (int dice = 1; dice <= 6; dice++) {
                int next = current + dice;
                if (next > totalCells) {
                    continue;
                }

                if (ladders.containsKey(next)) {
                    next = ladders.get(next);
                } else if (snakes.containsKey(next)) {
                    next = snakes.get(next);
                }

                if (!visited[next]) {
                    visited[next] = true;
                    moves[next] = moves[current] + 1;
                    queue.offer(next);
                }
            }
        }

        return -1;
    }

    private int dynamicProgramming(int n, Map<Integer, Integer> ladders, Map<Integer, Integer> snakes) {
        int totalCells = n * n;
        int[] dp = new int[totalCells + 1];
        Arrays.fill(dp, Integer.MAX_VALUE);
        dp[1] = 0;

        boolean changed;
        int maxIterations = totalCells * 2;

        for (int iteration = 0; iteration < maxIterations; iteration++) {
            changed = false;

            for (int cell = 1; cell <= totalCells; cell++) {
                if (dp[cell] == Integer.MAX_VALUE) {
                    continue;
                }

                for (int dice = 1; dice <= 6; dice++) {
                    int next = cell + dice;
                    if (next > totalCells) {
                        continue;
                    }

                    int destination = next;
                    if (ladders.containsKey(next)) {
                        destination = ladders.get(next);
                    } else if (snakes.containsKey(next)) {
                        destination = snakes.get(next);
                    }

                    if (dp[destination] > dp[cell] + 1) {
                        dp[destination] = dp[cell] + 1;
                        changed = true;
                    }
                }
            }

            if (!changed) {
                break;
            }
        }

        return dp[totalCells] == Integer.MAX_VALUE ? -1 : dp[totalCells];
    }

    private List<Integer> generateOptions(int correct) {
        Set<Integer> values = new LinkedHashSet<>();
        values.add(correct);

        Random random = new Random();
        while (values.size() < 4) {
            int offset = random.nextInt(7) - 3;
            int option = correct + offset;
            if (option > 0) {
                values.add(option);
            }
        }

        List<Integer> options = new ArrayList<>(values);
        Collections.shuffle(options);
        return options;
    }
}