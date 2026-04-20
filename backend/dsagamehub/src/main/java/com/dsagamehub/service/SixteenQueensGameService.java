package com.dsagamehub.service;

import com.dsagamehub.dto.AlgorithmRunResponse;
import com.dsagamehub.dto.ApiResponse;
import com.dsagamehub.dto.PlayerAnswerRequest;
import com.dsagamehub.exception.InvalidAnswerException;
import com.dsagamehub.model.AlgorithmRun;
import com.dsagamehub.model.GameRound;
import com.dsagamehub.model.Player;
import com.dsagamehub.model.PlayerAnswer;
import com.dsagamehub.repository.AlgorithmRunRepository;
import com.dsagamehub.repository.GameRoundRepository;
import com.dsagamehub.repository.PlayerAnswerRepository;
import com.dsagamehub.repository.PlayerRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class SixteenQueensGameService {

    private static final String GAME_NAME = "SIXTEEN_QUEENS";
    private static final String SEQUENTIAL = "SEQUENTIAL";
    private static final String THREADED = "THREADED";
    private static final String TIE = "TIE";
    private static final String COMPLETED = "COMPLETED";
    private static final String SEQUENTIAL_LOGIC = "Classic backtracking, row-by-row depth-first search";
    private static final String THREADED_LOGIC = "Parallel backtracking by splitting the first row across worker threads";

    private final SixteenQueensSequentialService sequentialService;
    private final SixteenQueensThreadedService threadedService;
    private final SixteenQueensValidationService validationService;
    private final AlgorithmRunRepository algorithmRunRepository;
    private final PlayerRepository playerRepository;
    private final PlayerAnswerRepository playerAnswerRepository;
    private final GameRoundRepository gameRoundRepository;

    public SixteenQueensGameService(SixteenQueensSequentialService sequentialService,
                                    SixteenQueensThreadedService threadedService,
                                    SixteenQueensValidationService validationService,
                                    AlgorithmRunRepository algorithmRunRepository,
                                    PlayerRepository playerRepository,
                                    PlayerAnswerRepository playerAnswerRepository,
                                    GameRoundRepository gameRoundRepository) {
        this.sequentialService = sequentialService;
        this.threadedService = threadedService;
        this.validationService = validationService;
        this.algorithmRunRepository = algorithmRunRepository;
        this.playerRepository = playerRepository;
        this.playerAnswerRepository = playerAnswerRepository;
        this.gameRoundRepository = gameRoundRepository;
    }

    public AlgorithmRunResponse runSequential() {
        int nextRoundNumber = getNextRoundNumber();

        long start = System.currentTimeMillis();
        long solutionCount = sequentialService.countQueens();
        long end = System.currentTimeMillis();
        long timeTaken = end - start;

        AlgorithmRun run = new AlgorithmRun(
                GAME_NAME,
                nextRoundNumber,
                SEQUENTIAL,
                SEQUENTIAL_LOGIC,
                solutionCount,
                timeTaken
        );
        algorithmRunRepository.save(run);

        return new AlgorithmRunResponse(GAME_NAME, SEQUENTIAL, (int) solutionCount, timeTaken);
    }

    public AlgorithmRunResponse runThreaded() {
        int nextRoundNumber = getNextRoundNumber();

        long start = System.currentTimeMillis();
        long solutionCount = threadedService.countQueensThreaded();
        long end = System.currentTimeMillis();
        long timeTaken = end - start;

        AlgorithmRun run = new AlgorithmRun(
                GAME_NAME,
                nextRoundNumber,
                THREADED,
                THREADED_LOGIC,
                solutionCount,
                timeTaken
        );
        algorithmRunRepository.save(run);

        return new AlgorithmRunResponse(GAME_NAME, THREADED, (int) solutionCount, timeTaken);
    }

    public ApiResponse submitAnswer(PlayerAnswerRequest request) {
        String playerName = request.getPlayerName().trim();
        String normalizedAnswer = validationService.normalizeAnswer(request.getAnswerText());

        if (!validationService.isValidFormat(normalizedAnswer)) {
            throw new InvalidAnswerException("Invalid format. Enter 16 unique numbers between 0 and 15.");
        }

        int[] board = validationService.parseBoard(normalizedAnswer);

        long sequentialStart = System.nanoTime();
        boolean sequentialCorrect = sequentialService.isValidSolution(board.clone());
        long sequentialCheckTimeNs = System.nanoTime() - sequentialStart;

        long threadedStart = System.nanoTime();
        boolean threadedCorrect = threadedService.isValidSolution(board.clone());
        long threadedCheckTimeNs = System.nanoTime() - threadedStart;

        long totalCheckTimeNs = sequentialCheckTimeNs + threadedCheckTimeNs;

        String bestAlgorithm = determineBestAlgorithm(sequentialCheckTimeNs, threadedCheckTimeNs);
        String comparisonMessage = buildComparisonMessage(sequentialCheckTimeNs, threadedCheckTimeNs, bestAlgorithm);

        ensurePlayerExists(playerName);

        if (!sequentialCorrect || !threadedCorrect) {
            return new ApiResponse(
                    false,
                    "Wrong answer. This queen arrangement is not a valid Sixteen Queens solution.",
                    false,
                    false,
                    sequentialCheckTimeNs,
                    threadedCheckTimeNs,
                    totalCheckTimeNs,
                    bestAlgorithm,
                    comparisonMessage,
                    0,
                    0,
                    0,
                    false,
                    false
            );
        }

        Optional<PlayerAnswer> existingCorrectAnswer = playerAnswerRepository.findByAnswerText(normalizedAnswer);
        if (existingCorrectAnswer.isPresent()) {
            return new ApiResponse(
                    false,
                    "Already found. Please try again.",
                    true,
                    true,
                    sequentialCheckTimeNs,
                    threadedCheckTimeNs,
                    totalCheckTimeNs,
                    bestAlgorithm,
                    comparisonMessage,
                    0,
                    0,
                    0,
                    false,
                    false
            );
        }

        int roundNumber = getNextRoundNumber();
        long targetSolutionCount = getMaximumSolutionCount();

        GameRound round = new GameRound(GAME_NAME, roundNumber);
        round.setStatus(COMPLETED);
        round.setAllSolutionsFound(false);
        round.setTargetSolutionCount(targetSolutionCount);
        round.setRecognizedSolutionCount(1);
        round.setSequentialRunCompleted(true);
        round.setThreadedRunCompleted(true);
        round.setSequentialTimeTakenNs(sequentialCheckTimeNs);
        round.setThreadedTimeTakenNs(threadedCheckTimeNs);
        round.setSequentialSolutionCount(targetSolutionCount);
        round.setThreadedSolutionCount(targetSolutionCount);
        round.setCompletedAt(LocalDateTime.now());
        gameRoundRepository.save(round);

        PlayerAnswer correctAnswer = new PlayerAnswer(
                playerName,
                normalizedAnswer,
                roundNumber,
                sequentialCheckTimeNs,
                threadedCheckTimeNs,
                totalCheckTimeNs,
                bestAlgorithm,
                comparisonMessage
        );
        playerAnswerRepository.save(correctAnswer);

        return new ApiResponse(
                true,
                "Congratulations! Correct answer saved successfully.",
                true,
                false,
                sequentialCheckTimeNs,
                threadedCheckTimeNs,
                totalCheckTimeNs,
                bestAlgorithm,
                comparisonMessage,
                roundNumber,
                1,
                targetSolutionCount,
                false,
                false
        );
    }

    private void ensurePlayerExists(String playerName) {
        Optional<Player> existingPlayer = playerRepository.findByName(playerName);
        if (existingPlayer.isEmpty()) {
            playerRepository.save(new Player(playerName));
        }
    }

    private int getNextRoundNumber() {
        return gameRoundRepository
                .findTopByGameNameOrderByRoundNumberDesc(GAME_NAME)
                .map(GameRound::getRoundNumber)
                .orElse(0) + 1;
    }

    private long getMaximumSolutionCount() {
        return algorithmRunRepository
                .findTopByGameNameOrderBySolutionCountDescCreatedAtDesc(GAME_NAME)
                .map(AlgorithmRun::getSolutionCount)
                .orElse(0L);
    }

    private String determineBestAlgorithm(long sequentialTimeNs, long threadedTimeNs) {
        if (sequentialTimeNs < threadedTimeNs) {
            return SEQUENTIAL;
        }
        if (threadedTimeNs < sequentialTimeNs) {
            return THREADED;
        }
        return TIE;
    }

    private String buildComparisonMessage(long sequentialTimeNs, long threadedTimeNs, String bestAlgorithm) {
        if (TIE.equals(bestAlgorithm)) {
            return "Sequential and threaded checks performed equally for this submission.";
        }

        return bestAlgorithm + " was faster for this submission. Sequential: "
                + sequentialTimeNs + " ns, Threaded: " + threadedTimeNs + " ns.";
    }

    public List<AlgorithmRun> getAllRuns() {
        return algorithmRunRepository.findByGameNameOrderByRoundNumberDescCreatedAtDesc(GAME_NAME);
    }

    public List<GameRound> getAllRounds() {
        return gameRoundRepository.findByGameNameOrderByRoundNumberDesc(GAME_NAME);
    }

    public List<PlayerAnswer> getAllPlayerAnswers() {
        return playerAnswerRepository.findAllByOrderBySubmittedAtDesc();
    }

    public ApiResponse resetRecognizedAnswers() {
        return new ApiResponse(
                true,
                "Reset is not needed now because each correct answer is stored as a separate round."
        );
    }
}