package com.dsagamehub.service;

import com.dsagamehub.dto.ApiResponse;
import com.dsagamehub.dto.PlayerAnswerRequest;
import com.dsagamehub.dto.SnakeLadderRequest;
import com.dsagamehub.dto.SnakeLadderResponse;
import com.dsagamehub.model.AlgorithmRun;
import com.dsagamehub.model.GameRound;
import com.dsagamehub.repository.AlgorithmRunRepository;
import com.dsagamehub.repository.GameRoundRepository;
import com.dsagamehub.repository.PlayerAnswerRepository;
import com.dsagamehub.repository.PlayerRepository;
import com.dsagamehub.repository.SnakeLadderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SnakeLadderServiceTest {

    @Mock
    private GameRoundRepository roundRepo;

    @Mock
    private AlgorithmRunRepository algoRepo;

    @Mock
    private SnakeLadderRepository snakeLadderResultRepo;

    @Mock
    private PlayerRepository playerRepo;

    @Mock
    private PlayerAnswerRepository playerAnswerRepo;

    @InjectMocks
    private SnakeLadderService service;

    private SnakeLadderRequest validRequest;

    @BeforeEach
    void setUp() {
        validRequest = new SnakeLadderRequest();
        validRequest.setBoardSize(8);
        validRequest.setPlayerName("TestPlayer");
    }

    @Test
    void testBoardSizeValidation() {
        SnakeLadderRequest tooSmall = new SnakeLadderRequest();
        tooSmall.setBoardSize(5);
        assertThrows(IllegalArgumentException.class, () -> service.startGame(tooSmall));

        SnakeLadderRequest tooLarge = new SnakeLadderRequest();
        tooLarge.setBoardSize(13);
        assertThrows(IllegalArgumentException.class, () -> service.startGame(tooLarge));
    }

    @Test
    void testStartGameCreatesSnakesAndLadders() {
        when(roundRepo.findTopByGameNameOrderByRoundNumberDesc("SNAKE_LADDER")).thenReturn(Optional.empty());

        GameRound savedRound = new GameRound();
        when(roundRepo.save(any(GameRound.class))).thenReturn(savedRound);
        when(algoRepo.save(any(AlgorithmRun.class))).thenReturn(new AlgorithmRun());

        SnakeLadderResponse response = service.startGame(validRequest);

        assertNotNull(response);
        assertEquals(8, response.getBoardSize());
        assertNotNull(response.getSnakes());
        assertNotNull(response.getLadders());
        assertEquals(6, response.getSnakes().size());
        assertEquals(6, response.getLadders().size());
        assertTrue(response.getCorrectAnswer() > 0);
        assertEquals(4, response.getOptions().size());
    }

    @Test
    void testPlayerNameValidation() {
        PlayerAnswerRequest request = new PlayerAnswerRequest();
        request.setPlayerName("");
        request.setAnswerText("5");

        ApiResponse response = service.submitAnswer(request);

        assertFalse(response.isSuccess());
        assertEquals("Player name is required", response.getMessage());
    }

    @Test
    void testAnswerFormatValidation() {
        when(roundRepo.findTopByGameNameOrderByRoundNumberDesc("SNAKE_LADDER")).thenReturn(Optional.of(new GameRound()));

        PlayerAnswerRequest request = new PlayerAnswerRequest();
        request.setPlayerName("Test");
        request.setAnswerText("abc");

        ApiResponse response = service.submitAnswer(request);

        assertFalse(response.isSuccess());
        assertEquals("Invalid answer format. Please enter a number.", response.getMessage());
    }

    @Test
    void testNoActiveRoundValidation() {
        when(roundRepo.findTopByGameNameOrderByRoundNumberDesc("SNAKE_LADDER")).thenReturn(Optional.empty());

        PlayerAnswerRequest request = new PlayerAnswerRequest();
        request.setPlayerName("Test");
        request.setAnswerText("4");

        ApiResponse response = service.submitAnswer(request);

        assertFalse(response.isSuccess());
        assertEquals("No active game round. Please start a new game.", response.getMessage());
    }

    @Test
    void testCorrectAnswerSavesResult() {
        when(roundRepo.findTopByGameNameOrderByRoundNumberDesc("SNAKE_LADDER")).thenReturn(Optional.empty());

        GameRound savedRound = new GameRound();
        savedRound.setRoundNumber(1);
        when(roundRepo.save(any(GameRound.class))).thenReturn(savedRound);
        when(algoRepo.save(any(AlgorithmRun.class))).thenReturn(new AlgorithmRun());

        SnakeLadderResponse startResponse = service.startGame(validRequest);

        when(roundRepo.findTopByGameNameOrderByRoundNumberDesc("SNAKE_LADDER")).thenReturn(Optional.of(savedRound));
        when(algoRepo.findByGameNameAndRoundNumberOrderByCreatedAtAsc("SNAKE_LADDER", 1)).thenReturn(List.of());

        when(playerRepo.findByName("TestPlayer")).thenReturn(Optional.empty());

        PlayerAnswerRequest answerRequest = new PlayerAnswerRequest();
        answerRequest.setPlayerName("TestPlayer");
        answerRequest.setAnswerText(String.valueOf(startResponse.getCorrectAnswer()));

        ApiResponse response = service.submitAnswer(answerRequest);

        assertTrue(response.isSuccess());
        assertTrue(response.isCorrect());
        verify(snakeLadderResultRepo, times(1)).save(any());
        verify(playerAnswerRepo, times(1)).save(any());
    }

    @Test
    void testIncorrectAnswerDoesNotSaveGameResult() {
        when(roundRepo.findTopByGameNameOrderByRoundNumberDesc("SNAKE_LADDER")).thenReturn(Optional.empty());

        GameRound savedRound = new GameRound();
        savedRound.setRoundNumber(1);
        when(roundRepo.save(any(GameRound.class))).thenReturn(savedRound);
        when(algoRepo.save(any(AlgorithmRun.class))).thenReturn(new AlgorithmRun());

        service.startGame(validRequest);

        when(roundRepo.findTopByGameNameOrderByRoundNumberDesc("SNAKE_LADDER")).thenReturn(Optional.of(savedRound));
        when(algoRepo.findByGameNameAndRoundNumberOrderByCreatedAtAsc("SNAKE_LADDER", 1)).thenReturn(List.of());
        when(playerRepo.findByName("TestPlayer")).thenReturn(Optional.empty());

        PlayerAnswerRequest answerRequest = new PlayerAnswerRequest();
        answerRequest.setPlayerName("TestPlayer");
        answerRequest.setAnswerText("999");

        ApiResponse response = service.submitAnswer(answerRequest);

        assertTrue(response.isSuccess());
        assertFalse(response.isCorrect());
        verify(snakeLadderResultRepo, never()).save(any());
        verify(playerAnswerRepo, times(1)).save(any());
    }

    @Test
    void testBfsAlgorithmCorrectness() {
        Map<Integer, Integer> ladders = new HashMap<>();
        Map<Integer, Integer> snakes = new HashMap<>();

        int result = invokePrivateMethod("bfs", 6, ladders, snakes);
        assertTrue(result >= 5 && result <= 10);
    }

    @Test
    void testDynamicProgrammingAlgorithmCorrectness() {
        Map<Integer, Integer> ladders = new HashMap<>();
        Map<Integer, Integer> snakes = new HashMap<>();

        int result = invokePrivateMethod("dynamicProgramming", 6, ladders, snakes);
        assertTrue(result >= 5 && result <= 10);
    }

    @Test
    void testGeneratedPositionsAreValid() {
        when(roundRepo.findTopByGameNameOrderByRoundNumberDesc("SNAKE_LADDER")).thenReturn(Optional.empty());
        when(roundRepo.save(any(GameRound.class))).thenReturn(new GameRound());
        when(algoRepo.save(any(AlgorithmRun.class))).thenReturn(new AlgorithmRun());

        for (int size = 6; size <= 12; size++) {
            SnakeLadderRequest request = new SnakeLadderRequest();
            request.setBoardSize(size);

            SnakeLadderResponse response = service.startGame(request);
            int maxCell = size * size;

            for (Map.Entry<Integer, Integer> snake : response.getSnakes().entrySet()) {
                assertTrue(snake.getKey() > snake.getValue());
                assertTrue(snake.getKey() >= 2 && snake.getKey() <= maxCell - 1);
                assertTrue(snake.getValue() >= 1 && snake.getValue() <= maxCell - 1);
            }

            for (Map.Entry<Integer, Integer> ladder : response.getLadders().entrySet()) {
                assertTrue(ladder.getKey() < ladder.getValue());
                assertTrue(ladder.getKey() >= 2 && ladder.getKey() <= maxCell - 1);
                assertTrue(ladder.getValue() >= 2 && ladder.getValue() <= maxCell - 1);
            }
        }
    }

    private int invokePrivateMethod(String methodName, int n, Map<Integer, Integer> ladders, Map<Integer, Integer> snakes) {
        try {
            java.lang.reflect.Method method = SnakeLadderService.class.getDeclaredMethod(
                    methodName, int.class, Map.class, Map.class
            );
            method.setAccessible(true);
            return (int) method.invoke(service, n, ladders, snakes);
        } catch (Exception e) {
            fail("Could not invoke method: " + e.getMessage());
            return -1;
        }
    }
}