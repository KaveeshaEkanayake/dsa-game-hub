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

        AlgorithmRun run = new AlgorithmRun(GAME_NAME, "SEQUENTIAL", (int) solutionCount, end - start);
        algorithmRunRepository.save(run);

        return new AlgorithmRunResponse(GAME_NAME, "SEQUENTIAL", (int) solutionCount, end - start);
    }

    public AlgorithmRunResponse runThreaded() {
        long start = System.currentTimeMillis();
        long solutionCount = threadedService.countQueensThreaded();
        long end = System.currentTimeMillis();

        AlgorithmRun run = new AlgorithmRun(GAME_NAME, "THREADED", (int) solutionCount, end - start);
        algorithmRunRepository.save(run);

        return new AlgorithmRunResponse(GAME_NAME, "THREADED", (int) solutionCount, end - start);
    }

    public ApiResponse submitAnswer(PlayerAnswerRequest request) {
        String playerName = request.getPlayerName().trim();
        String normalizedAnswer = validationService.normalizeAnswer(request.getAnswerText());

        if (!validationService.isValidFormat(normalizedAnswer)) {
            throw new InvalidAnswerException("Invalid format. Enter 16 unique numbers between 0 and 15.");
        }

        if (!validationService.isSelfConflictFree(normalizedAnswer)) {
            throw new InvalidAnswerException("This answer has queen conflicts.");
        }

        Optional<Player> existingPlayer = playerRepository.findByName(playerName);
        if (existingPlayer.isEmpty()) {
            playerRepository.save(new Player(playerName));
        }

        Optional<PlayerAnswer> existingCorrectAnswer =
                playerAnswerRepository.findByAnswerTextAndCorrectTrue(normalizedAnswer);

        if (existingCorrectAnswer.isPresent() && existingCorrectAnswer.get().isRecognized()) {
            playerAnswerRepository.save(
                    new PlayerAnswer(playerName, normalizedAnswer, true, true, "Already recognized")
            );
            return new ApiResponse(false, "This correct solution is already recognized. Try again.");
        }

        if (existingCorrectAnswer.isPresent()) {
            playerAnswerRepository.save(
                    new PlayerAnswer(playerName, normalizedAnswer, true, true, "Already recognized")
            );
            return new ApiResponse(false, "This correct solution is already recognized. Try again.");
        }

        playerAnswerRepository.save(
                new PlayerAnswer(playerName, normalizedAnswer, true, true, "Correct solution")
        );

        checkAndResetRecognizedAnswers();

        return new ApiResponse(true, "Correct solution submitted successfully");
    }

    private void checkAndResetRecognizedAnswers() {
        long totalCorrectSolutions = playerAnswerRepository.countDistinctCorrectAnswers();
        long latestMaximumSolutions = getLatestMaximumSolutionCount();

        if (latestMaximumSolutions > 0 && totalCorrectSolutions >= latestMaximumSolutions) {
            List<PlayerAnswer> correctAnswers = playerAnswerRepository.findByCorrectTrue();

            for (PlayerAnswer answer : correctAnswers) {
                answer.setRecognized(false);
                playerAnswerRepository.save(answer);
            }

            int roundNumber = (int) gameRoundRepository.count() + 1;
            gameRoundRepository.save(new GameRound(GAME_NAME, roundNumber, true));
        }
    }

    private long getLatestMaximumSolutionCount() {
        List<AlgorithmRun> runs = algorithmRunRepository.findByGameNameOrderByCreatedAtDesc(GAME_NAME);

        if (runs.isEmpty()) {
            return 0;
        }

        return runs.get(0).getSolutionCount();
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
            playerAnswerRepository.save(answer);
        }

        return new ApiResponse(true, "All recognized flags reset successfully");
    }
}