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

import java.util.List;
import java.util.Optional;

@Service
public class SixteenQueensGameService {

    private static final String GAME_NAME = "SIXTEEN_QUEENS";
    private static final String SEQUENTIAL = "SEQUENTIAL";
    private static final String THREADED = "THREADED";
    private static final String TIE = "TIE";

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
        long start = System.currentTimeMillis();
        long solutionCount = sequentialService.countQueens();
        long end = System.currentTimeMillis();

        AlgorithmRun run = new AlgorithmRun(GAME_NAME, SEQUENTIAL, (int) solutionCount, end - start);
        algorithmRunRepository.save(run);

        return new AlgorithmRunResponse(GAME_NAME, SEQUENTIAL, (int) solutionCount, end - start);
    }

    public AlgorithmRunResponse runThreaded() {
        long start = System.currentTimeMillis();
        long solutionCount = threadedService.countQueensThreaded();
        long end = System.currentTimeMillis();

        AlgorithmRun run = new AlgorithmRun(GAME_NAME, THREADED, (int) solutionCount, end - start);
        algorithmRunRepository.save(run);

        return new AlgorithmRunResponse(GAME_NAME, THREADED, (int) solutionCount, end - start);
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
        long sequentialTimeMs = nanosToMillis(System.nanoTime() - sequentialStart);

        long threadedStart = System.nanoTime();
        boolean threadedCorrect = threadedService.isValidSolution(board.clone());
        long threadedTimeMs = nanosToMillis(System.nanoTime() - threadedStart);

        long totalCheckTimeMs = sequentialTimeMs + threadedTimeMs;
        String bestAlgorithm = determineBestAlgorithm(sequentialTimeMs, threadedTimeMs);
        String comparisonMessage = buildComparisonMessage(sequentialTimeMs, threadedTimeMs, bestAlgorithm);

        ensurePlayerExists(playerName);

        if (!sequentialCorrect || !threadedCorrect) {
            return new ApiResponse(
                    false,
                    "Wrong answer. This queen arrangement is not a valid Sixteen Queens solution.",
                    false,
                    false,
                    sequentialTimeMs,
                    threadedTimeMs,
                    totalCheckTimeMs,
                    bestAlgorithm,
                    comparisonMessage,
                    false
            );
        }

        Optional<PlayerAnswer> existingRecognizedAnswer =
                playerAnswerRepository.findByAnswerTextAndCorrectTrueAndRecognizedTrue(normalizedAnswer);

        if (existingRecognizedAnswer.isPresent()) {
            return new ApiResponse(
                    false,
                    "Already found. Please try again.",
                    true,
                    true,
                    sequentialTimeMs,
                    threadedTimeMs,
                    totalCheckTimeMs,
                    bestAlgorithm,
                    comparisonMessage,
                    false
            );
        }

        playerAnswerRepository.save(
                new PlayerAnswer(playerName, normalizedAnswer, true, true, "Correct solution")
        );

        boolean allSolutionsIdentified = checkAndResetRecognizedAnswers();
        String successMessage = allSolutionsIdentified
                ? "Congratulations! Correct answer saved. All solutions were identified, so recognized flags were reset for the next round."
                : "Congratulations! Correct answer saved successfully.";

        return new ApiResponse(
                true,
                successMessage,
                true,
                false,
                sequentialTimeMs,
                threadedTimeMs,
                totalCheckTimeMs,
                bestAlgorithm,
                comparisonMessage,
                allSolutionsIdentified
        );
    }

    private void ensurePlayerExists(String playerName) {
        Optional<Player> existingPlayer = playerRepository.findByName(playerName);
        if (existingPlayer.isEmpty()) {
            playerRepository.save(new Player(playerName));
        }
    }

    private boolean checkAndResetRecognizedAnswers() {
        long totalRecognizedCorrectSolutions = playerAnswerRepository.countDistinctRecognizedCorrectAnswers();
        long maximumSolutions = getMaximumSolutionCount();

        if (maximumSolutions > 0 && totalRecognizedCorrectSolutions >= maximumSolutions) {
            List<PlayerAnswer> correctAnswers = playerAnswerRepository.findByCorrectTrue();

            for (PlayerAnswer answer : correctAnswers) {
                answer.setRecognized(false);
            }
            playerAnswerRepository.saveAll(correctAnswers);

            int roundNumber = (int) gameRoundRepository.count() + 1;
            gameRoundRepository.save(new GameRound(GAME_NAME, roundNumber, true));
            return true;
        }

        return false;
    }

    private long getMaximumSolutionCount() {
        return algorithmRunRepository
                .findTopByGameNameOrderBySolutionCountDescCreatedAtDesc(GAME_NAME)
                .map(AlgorithmRun::getSolutionCount)
                .orElse(0);
    }

    private long nanosToMillis(long nanos) {
        return nanos / 1_000_000;
    }

    private String determineBestAlgorithm(long sequentialTimeMs, long threadedTimeMs) {
        if (sequentialTimeMs < threadedTimeMs) {
            return SEQUENTIAL;
        }
        if (threadedTimeMs < sequentialTimeMs) {
            return THREADED;
        }
        return TIE;
    }

    private String buildComparisonMessage(long sequentialTimeMs, long threadedTimeMs, String bestAlgorithm) {
        if (TIE.equals(bestAlgorithm)) {
            return "Sequential and threaded checks performed equally for this submission.";
        }

        return bestAlgorithm + " was faster for this submission. Sequential: "
                + sequentialTimeMs + " ms, Threaded: " + threadedTimeMs + " ms.";
    }

    public List<AlgorithmRun> getAllRuns() {
        return algorithmRunRepository.findByGameNameOrderByCreatedAtDesc(GAME_NAME);
    }

    public List<PlayerAnswer> getAllPlayerAnswers() {
        return playerAnswerRepository.findAll();
    }

    public ApiResponse resetRecognizedAnswers() {
        List<PlayerAnswer> allAnswers = playerAnswerRepository.findByCorrectTrue();

        for (PlayerAnswer answer : allAnswers) {
            answer.setRecognized(false);
        }
        playerAnswerRepository.saveAll(allAnswers);

        return new ApiResponse(true, "All recognized flags reset successfully");
    }
}
