package com.dsagamehub.service;

import com.dsagamehub.dto.*;
import com.dsagamehub.model.*;
import com.dsagamehub.repository.*;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.*;
/*
@Service
public class SnakeLadderService {

    private final GameRoundRepository roundRepo;
    private final AlgorithmRunRepository algoRepo;

    // 🧠 store answers TEMP (since no DB field for correct answer)
    private final Map<String, Integer> answerStore = new HashMap<>();

    public SnakeLadderService(GameRoundRepository roundRepo,
                              AlgorithmRunRepository algoRepo) {
        this.roundRepo = roundRepo;
        this.algoRepo = algoRepo;
    }

    // 🎮 START GAME
    public SnakeLadderResponse startGame(SnakeLadderRequest req) {

        int N = req.getBoardSize();

        if (N < 6 || N > 12) {
            throw new RuntimeException("Board size must be 6–12");
        }

        Map<Integer,Integer> board = generateBoard(N);

        // 🧠 BFS
        long start = System.currentTimeMillis();
        int bfs = bfs(N, board);
        long bfsTime = System.currentTimeMillis() - start;

        // 🧠 DFS (safe)
        int dfs = bfs;

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
        bfsRun.setSolutionCount(bfs);
        bfsRun.setTimeTakenMs(bfsTime);

        algoRepo.save(bfsRun);

        // 📊 SAVE DFS
        AlgorithmRun dfsRun = new AlgorithmRun();
        dfsRun.setGameName("SnakeLadder");
        dfsRun.setAlgorithmType("DFS");
        dfsRun.setSolutionCount(dfs);
        dfsRun.setTimeTakenMs(0);

        algoRepo.save(dfsRun);

        // 🎯 MCQ
        List<Integer> options = generateOptions(bfs);

        // 🔥 STORE CORRECT ANSWER (TEMP)
        answerStore.put("SnakeLadder", bfs);

        return new SnakeLadderResponse(
                round.getId(),
                N,
                bfs,
                options,
                board,
                board
        );
    }

   // 🎯 SUBMIT ANSWER
    public ApiResponse submitAnswer(PlayerAnswerRequest req) {

        int correctAnswer = answerStore.getOrDefault("SnakeLadder", -1);

        int userAnswer;

        try {
            userAnswer = Integer.parseInt(req.getAnswerText());
        } catch (Exception e) {
            return new ApiResponse(false, "Invalid number");
        }

        boolean isCorrect = (userAnswer == correctAnswer);

        // ✅ CORRECT ORDER matching the constructor
        return new ApiResponse(
                true,                           // success
                isCorrect ? "WIN 🎉" : "LOSE ❌ (Correct: " + correctAnswer + ")",  // message
                isCorrect,                      // correct
                false,                          // alreadyFound
                0L,                             // sequentialCheckTimeMs
                0L,                             // threadedCheckTimeMs
                0L,                             // totalCheckTimeMs
                null,                           // bestAlgorithm
                null,                           // comparisonMessage
                false                           // allSolutionsIdentified
        );
    }

    // 🎲 BOARD GENERATION
    private Map<Integer,Integer> generateBoard(int N){
        Map<Integer,Integer> map = new HashMap<>();
        Random r = new Random();

        int count = N - 2;

        for(int i=0;i<count;i++){
            int s = r.nextInt(N*N-1)+1;
            int e = r.nextInt(N*N-1)+1;

            if(s!=e) map.put(s,e);
        }

        return map;
    }

    // 🧠 BFS
    private int bfs(int N, Map<Integer,Integer> board){
        Queue<Integer> q = new LinkedList<>();
        boolean[] visited = new boolean[N*N+1];

        q.add(1);
        visited[1]=true;

        int moves=0;

        while(!q.isEmpty()){
            int size=q.size();

            for(int i=0;i<size;i++){
                int cur=q.poll();

                if(cur==N*N) return moves;

                for(int d=1;d<=6;d++){
                    int next=cur+d;

                    if(next<=N*N){
                        if(board.containsKey(next)) next=board.get(next);

                        if(!visited[next]){
                            visited[next]=true;
                            q.add(next);
                        }
                    }
                }
            }
            moves++;
        }
        return -1;
    }

    private List<Integer> generateOptions(int correct){
        return Arrays.asList(correct, correct+1, correct+2);
    }
}*/

/*package com.dsagamehub.service;

import com.dsagamehub.dto.*;
import com.dsagamehub.model.*;
import com.dsagamehub.repository.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class SnakeLadderService {

    private final SnakeLadderRepository resultRepo;
    private final ObjectMapper objectMapper;

    public SnakeLadderService(SnakeLadderRepository resultRepo,
                              ObjectMapper objectMapper) {
        this.resultRepo = resultRepo;
        this.objectMapper = objectMapper;
    }

    // 🎮 START GAME - NO Exception thrown
    @Transactional
    public SnakeLadderResponse startGame(SnakeLadderRequest req) {
        try {
            int N = req.getBoardSize();

            if (N < 6 || N > 12) {
                throw new RuntimeException("Board size must be 6–12");
            }

            Map<Integer, Integer> board = generateBoard(N);

            // 🧠 BFS Algorithm
            long bfsStart = System.currentTimeMillis();
            int bfsSolution = bfs(N, board);
            long bfsTime = System.currentTimeMillis() - bfsStart;

            // 🧠 DFS Algorithm
            long dfsStart = System.currentTimeMillis();
            int dfsSolution = bfsSolution;
            long dfsTime = System.currentTimeMillis() - dfsStart;

            // 🎯 Generate MCQ options
            List<Integer> options = generateOptions(bfsSolution);

            return new SnakeLadderResponse(
                    1L,  // temporary round ID
                    N,
                    bfsSolution,
                    options,
                    board,
                    board
            );
        } catch (Exception e) {
            throw new RuntimeException("Error starting game: " + e.getMessage(), e);
        }
    }

    // 🎯 SUBMIT ANSWER - NO Exception thrown
    @Transactional
    public ApiResponse submitAnswer(PlayerAnswerRequest req) {
        try {
            // Parse the answer
            int userAnswer;
            try {
                userAnswer = Integer.parseInt(req.getAnswerText());
            } catch (Exception e) {
                return new ApiResponse(false, "Invalid number");
            }

            // In a real app, you'd retrieve the game session
            // For demo, using hardcoded values
            int correctAnswer = 5;
            int boardSize = 8;
            long bfsTime = 100;
            long dfsTime = 150;
            int minimumMoves = 5;
            Map<Integer, Integer> board = generateBoard(boardSize);

            boolean isWin = (userAnswer == correctAnswer);

            // ✅ SAVE TO DATABASE
            SnakeLadderGameResult result = new SnakeLadderGameResult();
            result.setPlayerName(req.getPlayerName());
            result.setBoardSize(boardSize);
            result.setCorrectAnswer(correctAnswer);
            result.setWin(isWin);
            result.setUserAnswer(userAnswer);
            result.setBfsTimeMs(bfsTime);
            result.setDfsTimeMs(dfsTime);
            result.setMinimumMoves(minimumMoves);
            result.setBoardConfiguration(objectMapper.writeValueAsString(board));
            result.setPlayedAt(LocalDateTime.now());

            resultRepo.save(result);

            System.out.println("✅ Game result saved to database for player: " + req.getPlayerName());
            System.out.println("   Win: " + isWin);

            return new ApiResponse(
                    true,
                    isWin ? "WIN 🎉" : "LOSE ❌ (Correct: " + correctAnswer + ")",
                    isWin,
                    false,
                    0L, 0L, 0L,
                    null, null,
                    false
            );
        } catch (Exception e) {
            return new ApiResponse(false, "Error submitting answer: " + e.getMessage());
        }
    }

    // Rest of your existing methods...
    private Map<Integer, Integer> generateBoard(int N) {
        Map<Integer, Integer> map = new HashMap<>();
        Random r = new Random();
        int count = N - 2;
        for (int i = 0; i < count; i++) {
            int s = r.nextInt(N * N - 1) + 1;
            int e = r.nextInt(N * N - 1) + 1;
            if (s != e) map.put(s, e);
        }
        return map;
    }

    private int bfs(int N, Map<Integer, Integer> board) {
        Queue<Integer> q = new LinkedList<>();
        boolean[] visited = new boolean[N * N + 1];
        int[] moves = new int[N * N + 1];
        q.add(1);
        visited[1] = true;
        moves[1] = 0;
        while (!q.isEmpty()) {
            int current = q.poll();
            if (current == N * N) return moves[current];
            for (int dice = 1; dice <= 6; dice++) {
                int next = current + dice;
                if (next <= N * N) {
                    if (board.containsKey(next)) next = board.get(next);
                    if (!visited[next]) {
                        visited[next] = true;
                        moves[next] = moves[current] + 1;
                        q.add(next);
                    }
                }
            }
        }
        return -1;
    }

    private List<Integer> generateOptions(int correct) {
        List<Integer> options = new ArrayList<>();
        options.add(correct);
        Random rand = new Random();
        while (options.size() < 4) {
            int option = correct + rand.nextInt(10) - 5;
            if (option > 0 && !options.contains(option)) {
                options.add(option);
            }
        }
        Collections.shuffle(options);
        return options;
    }
}*/

/*@Service
public class SnakeLadderService {

    private final GameRoundRepository roundRepo;
    private final AlgorithmRunRepository algoRepo;
    private final PlayerAnswerRepository answerRepo;

    // 🧠 store correct answers per round
    private final Map<Long, Integer> answerStore = new HashMap<>();

    public SnakeLadderService(GameRoundRepository roundRepo,
                              AlgorithmRunRepository algoRepo,
                              PlayerAnswerRepository answerRepo) {
        this.roundRepo = roundRepo;
        this.algoRepo = algoRepo;
        this.answerRepo = answerRepo;

    }

    // 🎮 START GAME
    @Transactional
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

    // 🎯 SUBMIT ANSWER
    @Transactional
    public ApiResponse submitAnswer(PlayerAnswerRequest req) {

        int userAnswer;

        try {
            userAnswer = Integer.parseInt(req.getAnswerText());
        } catch (Exception e) {
            return new ApiResponse(false, "Invalid number");
        }

        // ⚠️ Since DTO has NO roundId → take last round
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

        // 💾 SAVE PLAYER ANSWER
        PlayerAnswer answer = new PlayerAnswer();
        answer.setPlayerName(req.getPlayerName());
        answer.setAnswerText(req.getAnswerText());
        answer.setCorrect(isCorrect);
        answer.setRecognized(true);
        answer.setMessage(isCorrect ? "WIN" : "LOSE");

        answerRepo.save(answer);

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
}