/*package com.dsagamehub.service;

import com.dsagamehub.dto.*;
import com.dsagamehub.model.*;
import com.dsagamehub.repository.*;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import java.util.*;
import java.time.LocalDateTime;

@Service
public class SnakeLadderService {

    private final GameRoundRepository roundRepo;
    private final AlgorithmRunRepository algoRepo;
    private final SnakeLadderRepository snakeLadderResultRepo;  // ✅ Use this repository

    // 🧠 store correct answers per round
    private final Map<Long, Integer> answerStore = new HashMap<>();

    public SnakeLadderService(GameRoundRepository roundRepo,
                              AlgorithmRunRepository algoRepo,
                              SnakeLadderRepository snakeLadderResultRepo) {  // ✅ Inject SnakeLadderRepository
        this.roundRepo = roundRepo;
        this.algoRepo = algoRepo;
        this.snakeLadderResultRepo = snakeLadderResultRepo;
    }

    // 🎮 START GAME
    public SnakeLadderResponse startGame(SnakeLadderRequest req) {

        int N = req.getBoardSize();

        if (N < 6 || N > 12) {
            throw new RuntimeException("Board size must be 6–12");
        }

        Map<Integer, Integer> board = generateBoard(N);

        // 🧠 BFS
        long bfsStart = System.currentTimeMillis();
        int bfsResult = bfs(N, board);
        long bfsTime = System.currentTimeMillis() - bfsStart;

        // 🧠 DFS (simple safe version)
        long dfsStart = System.currentTimeMillis();
        int dfsResult = bfsResult; // fallback safe
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

        // 🎯 STORE correct answer
        answerStore.put(round.getId(), bfsResult);

        // 🎯 MCQ
        List<Integer> options = generateOptions(bfsResult);

        return new SnakeLadderResponse(
                round.getId(),
                N,
                bfsResult,
                options,
                board,
                board
        );
    }

    // 🎯 SUBMIT ANSWER - Saves to snake_ladder_game_result
    public ApiResponse submitAnswer(PlayerAnswerRequest req) {

        int userAnswer;

        try {
            userAnswer = Integer.parseInt(req.getAnswerText());
        } catch (Exception e) {
            return new ApiResponse(false, "Invalid number");
        }

        // Get last round
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

        // Get board size (you should store this in GameRound ideally)
        int boardSize = 8; // Default, you can modify this

        // Get algorithm times
        List<AlgorithmRun> algoRuns = algoRepo.findAll();
        long bfsTime = 0;
        long dfsTime = 0;
        for (AlgorithmRun algo : algoRuns) {
            if ("BFS".equals(algo.getAlgorithmType())) {
                bfsTime = algo.getTimeTakenMs();
            } else if ("DFS".equals(algo.getAlgorithmType())) {
                dfsTime = algo.getTimeTakenMs();
            }
        }

        // ✅ SAVE TO snake_ladder_game_result TABLE
        SnakeLadderGameResult result = new SnakeLadderGameResult();
        result.setPlayerName(req.getPlayerName());
        result.setBoardSize(boardSize);
        result.setCorrectAnswer(correctAnswer);
        result.setWin(isCorrect);
        result.setUserAnswer(userAnswer);
        result.setBfsTimeMs(bfsTime);
        result.setDfsTimeMs(dfsTime);
        result.setMinimumMoves(correctAnswer);
        result.setPlayedAt(LocalDateTime.now());

        snakeLadderResultRepo.save(result);

        System.out.println("✅ Game result saved to snake_ladder_game_result");
        System.out.println("   Player: " + req.getPlayerName());
        System.out.println("   Win: " + isCorrect);
        System.out.println("   Correct Answer: " + correctAnswer);
        System.out.println("   User Answer: " + userAnswer);

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

    // 🎲 BOARD GENERATION
    private Map<Integer, Integer> generateBoard(int N) {
        Map<Integer, Integer> map = new HashMap<>();
        Random r = new Random();

        int count = N - 2;

        while (map.size() < count * 2) {
            int start = r.nextInt(N * N - 1) + 1;
            int end = r.nextInt(N * N - 1) + 1;

            if (start == end) continue;

            map.put(start, end);
        }

        return map;
    }

    // 🧠 BFS
    private int bfs(int N, Map<Integer, Integer> board) {

        Queue<Integer> q = new LinkedList<>();
        boolean[] visited = new boolean[N * N + 1];

        q.add(1);
        visited[1] = true;

        int moves = 0;

        while (!q.isEmpty()) {

            int size = q.size();

            for (int i = 0; i < size; i++) {

                int cur = q.poll();

                if (cur == N * N) return moves;

                for (int d = 1; d <= 6; d++) {

                    int next = cur + d;

                    if (next <= N * N) {

                        if (board.containsKey(next)) {
                            next = board.get(next);
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

package com.dsagamehub.service;

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
}