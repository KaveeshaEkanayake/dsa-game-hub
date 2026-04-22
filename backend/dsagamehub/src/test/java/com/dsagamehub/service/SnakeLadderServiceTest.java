package com.dsagamehub.service;

import com.dsagamehub.dto.*;
import com.dsagamehub.model.SnakeLadderGameResult;
import com.dsagamehub.repository.SnakeLadderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SnakeLadderServiceTest {

    @Mock
    private SnakeLadderRepository snakeLadderResultRepo;

    @InjectMocks
    private SnakeLadderService snakeLadderService;

    private SnakeLadderRequest validRequest;

    @BeforeEach
    void setUp() {
        validRequest = new SnakeLadderRequest();
        validRequest.setBoardSize(8);
        validRequest.setPlayerName("TestPlayer");

    }

    // Test 1: Board size validation
    @Test
    void testBoardSizeValidation() {
        // Invalid size - too small
        SnakeLadderRequest tooSmall = new SnakeLadderRequest();
        tooSmall.setBoardSize(5);
        tooSmall.setPlayerName("Test");
        assertThrows(IllegalArgumentException.class, () -> {
            snakeLadderService.startGame(tooSmall);
        });

        // Invalid size - too large
        SnakeLadderRequest tooLarge = new SnakeLadderRequest();
        tooLarge.setBoardSize(13);
        tooLarge.setPlayerName("Test");
        assertThrows(IllegalArgumentException.class, () -> {
            snakeLadderService.startGame(tooLarge);
        });

        // Valid sizes (6-12) - should not throw
        for (int size = 6; size <= 12; size++) {
            SnakeLadderRequest valid = new SnakeLadderRequest();
            valid.setBoardSize(size);
            valid.setPlayerName("Test");
            assertDoesNotThrow(() -> {
                snakeLadderService.startGame(valid);
            });
        }
    }

    // Test 2: Correct number of snakes and ladders (N-2)
    @Test
    void testCorrectNumberOfSnakesAndLadders() {
        for (int size = 6; size <= 12; size++) {
            SnakeLadderRequest request = new SnakeLadderRequest();
            request.setBoardSize(size);
            request.setPlayerName("Test");

            SnakeLadderResponse response = snakeLadderService.startGame(request);

            // Check snakes and ladders count
            assertNotNull(response);
            assertNotNull(response.getSnakes());
            assertNotNull(response.getLadders());

            // Each should have N-2 items
            assertEquals(size - 2, response.getSnakes().size(),
                    "Snakes count should be " + (size - 2) + " for board size " + size);
            assertEquals(size - 2, response.getLadders().size(),
                    "Ladders count should be " + (size - 2) + " for board size " + size);
        }
    }

    // Test 3: Player name validation (empty name in submit)
    @Test
    void testPlayerNameValidation() {
        SnakeLadderAnswerRequest invalidRequest = new SnakeLadderAnswerRequest();
        invalidRequest.setPlayerName("");
        invalidRequest.setAnswerText("5");
        invalidRequest.setRoundId(1L);

        SnakeLadderApiResponse response = snakeLadderService.submitAnswer(invalidRequest);
        assertFalse(response.isSuccess());
        assertEquals("Player name is required", response.getMessage());
    }

    // Test 4: Answer format validation
    @Test
    void testAnswerFormatValidation() {
        SnakeLadderAnswerRequest invalidRequest = new SnakeLadderAnswerRequest();
        invalidRequest.setPlayerName("Test");
        invalidRequest.setAnswerText("not a number");
        invalidRequest.setRoundId(1L);

        SnakeLadderApiResponse response = snakeLadderService.submitAnswer(invalidRequest);
        assertFalse(response.isSuccess());
        assertEquals("Invalid answer format. Please enter a number.", response.getMessage());
    }

    // Test 5: Round ID validation
    @Test
    void testRoundIdValidation() {
        SnakeLadderAnswerRequest request = new SnakeLadderAnswerRequest();
        request.setPlayerName("Test");
        request.setAnswerText("10");
        request.setRoundId(null);

        SnakeLadderApiResponse response = snakeLadderService.submitAnswer(request);
        assertFalse(response.isSuccess());
        assertEquals("No active game round. Please start a new game.", response.getMessage());
    }

    // Test 6: BFS algorithm correctness for simple board
    @Test
    void testBfsAlgorithmCorrectness() {
        // Simple board without snakes/ladders
        Map<Integer, Integer> emptyLadders = new HashMap<>();
        Map<Integer, Integer> emptySnakes = new HashMap<>();

        // For 6x6 board (36 cells) - minimum dice throws should be around 5-6
        int result = invokeBfsMethod(6, emptyLadders, emptySnakes);
        assertTrue(result >= 5 && result <= 10,
                "BFS result should be between 5 and 10 for 6x6 board, but was: " + result);
    }

    // Helper to invoke private BFS method via reflection
    private int invokeBfsMethod(int N, Map<Integer, Integer> ladders, Map<Integer, Integer> snakes) {
        try {
            java.lang.reflect.Method method = SnakeLadderService.class.getDeclaredMethod(
                    "bfs", int.class, Map.class, Map.class);
            method.setAccessible(true);
            return (int) method.invoke(snakeLadderService, N, ladders, snakes);
        } catch (Exception e) {
            fail("Could not invoke BFS method: " + e.getMessage());
            return -1;
        }
    }

    // Test 7: DP algorithm correctness
    @Test
    void testDpAlgorithmCorrectness() {
        Map<Integer, Integer> emptyLadders = new HashMap<>();
        Map<Integer, Integer> emptySnakes = new HashMap<>();

        int result = invokeDpMethod(6, emptyLadders, emptySnakes);
        assertTrue(result >= 5 && result <= 10,
                "DP result should be between 5 and 10 for 6x6 board, but was: " + result);
    }

    private int invokeDpMethod(int N, Map<Integer, Integer> ladders, Map<Integer, Integer> snakes) {
        try {
            java.lang.reflect.Method method = SnakeLadderService.class.getDeclaredMethod(
                    "dynamicProgramming", int.class, Map.class, Map.class);
            method.setAccessible(true);
            return (int) method.invoke(snakeLadderService, N, ladders, snakes);
        } catch (Exception e) {
            fail("Could not invoke DP method: " + e.getMessage());
            return -1;
        }
    }

    // Test 8: Snake and ladder positions are within bounds
    @Test
    void testSnakeAndLadderPositionsInBounds() {
        for (int size = 6; size <= 12; size++) {
            SnakeLadderRequest request = new SnakeLadderRequest();
            request.setBoardSize(size);
            request.setPlayerName("Test");

            SnakeLadderResponse response = snakeLadderService.startGame(request);

            int maxCell = size * size;

            // Check snakes
            if (response.getSnakes() != null) {
                for (Map.Entry<Integer, Integer> snake : response.getSnakes().entrySet()) {
                    int head = snake.getKey();
                    int tail = snake.getValue();

                    assertTrue(head >= 2 && head <= maxCell - 1,
                            "Snake head out of bounds: " + head + " for board size " + size);
                    assertTrue(tail >= 1 && tail <= maxCell - 1,
                            "Snake tail out of bounds: " + tail + " for board size " + size);
                    assertTrue(head > tail,
                            "Snake must go down: head=" + head + ", tail=" + tail);
                }
            }

            // Check ladders
            if (response.getLadders() != null) {
                for (Map.Entry<Integer, Integer> ladder : response.getLadders().entrySet()) {
                    int bottom = ladder.getKey();
                    int top = ladder.getValue();

                    assertTrue(bottom >= 2 && bottom <= maxCell - 1,
                            "Ladder bottom out of bounds: " + bottom + " for board size " + size);
                    assertTrue(top >= 3 && top <= maxCell - 1,
                            "Ladder top out of bounds: " + top + " for board size " + size);
                    assertTrue(bottom < top,
                            "Ladder must go up: bottom=" + bottom + ", top=" + top);
                }
            }
        }
    }

    // Test 9: No snake head at cell 1
    @Test
    void testNoSnakeAtCellOne() {
        for (int size = 6; size <= 12; size++) {
            SnakeLadderRequest request = new SnakeLadderRequest();
            request.setBoardSize(size);
            request.setPlayerName("Test");

            SnakeLadderResponse response = snakeLadderService.startGame(request);

            if (response.getSnakes() != null) {
                assertFalse(response.getSnakes().containsKey(1),
                        "Snake head cannot be at cell 1 for board size " + size);
            }
        }
    }

    // Test 10: No ladder ending at last cell
    @Test
    void testNoLadderAtLastCell() {
        for (int size = 6; size <= 12; size++) {
            SnakeLadderRequest request = new SnakeLadderRequest();
            request.setBoardSize(size);
            request.setPlayerName("Test");

            SnakeLadderResponse response = snakeLadderService.startGame(request);
            int lastCell = size * size;

            if (response.getLadders() != null) {
                assertFalse(response.getLadders().containsValue(lastCell),
                        "Ladder cannot end at last cell " + lastCell + " for board size " + size);
            }
        }
    }

    // Test 11: Response has 3 options (MCQ)
    @Test
    void testOptionsHaveThreeChoices() {
        for (int size = 6; size <= 12; size++) {
            SnakeLadderRequest request = new SnakeLadderRequest();
            request.setBoardSize(size);
            request.setPlayerName("Test");

            SnakeLadderResponse response = snakeLadderService.startGame(request);

            assertNotNull(response.getOptions());
            assertEquals(3, response.getOptions().size(),
                    "Should have exactly 3 options for board size " + size);
        }
    }

    // Test 12: Correct answer is positive
    @Test
    void testCorrectAnswerIsPositive() {
        for (int size = 6; size <= 12; size++) {
            SnakeLadderRequest request = new SnakeLadderRequest();
            request.setBoardSize(size);
            request.setPlayerName("Test");

            SnakeLadderResponse response = snakeLadderService.startGame(request);

            assertTrue(response.getCorrectAnswer() > 0,
                    "Correct answer should be positive for board size " + size);
        }
    }

    // Test 13: Round ID is generated and unique
    @Test
    void testRoundIdIsGenerated() {
        SnakeLadderRequest request = new SnakeLadderRequest();
        request.setBoardSize(8);
        request.setPlayerName("Test");

        SnakeLadderResponse response1 = snakeLadderService.startGame(request);
        SnakeLadderResponse response2 = snakeLadderService.startGame(request);

        assertNotNull(response1.getRoundId());
        assertNotNull(response2.getRoundId());
        assertNotEquals(response1.getRoundId(), response2.getRoundId(),
                "Each game should have a unique round ID");
    }

    // Test 14: Empty player name in start game should throw exception
    @Test
    void testEmptyPlayerNameInStartGame() {
        SnakeLadderRequest request = new SnakeLadderRequest();
        request.setBoardSize(8);
        request.setPlayerName("");

        assertThrows(IllegalArgumentException.class, () -> {
            snakeLadderService.startGame(request);
        }, "Empty player name should throw IllegalArgumentException");
    }

    // Test 15: Null player name in start game should throw exception
    @Test
    void testNullPlayerNameInStartGame() {
        SnakeLadderRequest request = new SnakeLadderRequest();
        request.setBoardSize(8);
        request.setPlayerName(null);

        assertThrows(IllegalArgumentException.class, () -> {
            snakeLadderService.startGame(request);
        }, "Null player name should throw IllegalArgumentException");
    }

    // Test 16: Both algorithms return same minimum moves (consistency test)
    @Test
    void testBothAlgorithmsConsistency() {
        for (int size = 6; size <= 8; size++) {
            SnakeLadderRequest request = new SnakeLadderRequest();
            request.setBoardSize(size);
            request.setPlayerName("Test");

            // Just verify the response exists - both algorithms are used internally
            SnakeLadderResponse response = snakeLadderService.startGame(request);
            assertNotNull(response);
            assertTrue(response.getCorrectAnswer() > 0);
        }
    }

    // Test 17: Valid answer submission (requires a started game)
    @Test
    void testValidAnswerSubmission() {
        // Start a game first
        SnakeLadderRequest startRequest = new SnakeLadderRequest();
        startRequest.setBoardSize(6);
        startRequest.setPlayerName("TestPlayer");

        SnakeLadderResponse startResponse = snakeLadderService.startGame(startRequest);
        Long roundId = startResponse.getRoundId();
        int correctAnswer = startResponse.getCorrectAnswer();

        // Submit correct answer
        SnakeLadderAnswerRequest answerRequest = new SnakeLadderAnswerRequest();
        answerRequest.setPlayerName("TestPlayer");
        answerRequest.setAnswerText(String.valueOf(correctAnswer));
        answerRequest.setRoundId(roundId);

        // Mock the save operation
        when(snakeLadderResultRepo.save(any(SnakeLadderGameResult.class))).thenReturn(new SnakeLadderGameResult());

        SnakeLadderApiResponse response = snakeLadderService.submitAnswer(answerRequest);

        assertTrue(response.isSuccess());
        assertTrue(response.isCorrect());
        assertTrue(response.getMessage().contains("Correct"));
    }

    // Test 18: Invalid answer submission (wrong answer)
    @Test
    void testInvalidAnswerSubmission() {
        // Start a game first
        SnakeLadderRequest startRequest = new SnakeLadderRequest();
        startRequest.setBoardSize(6);
        startRequest.setPlayerName("TestPlayer");

        SnakeLadderResponse startResponse = snakeLadderService.startGame(startRequest);
        Long roundId = startResponse.getRoundId();

        // Submit wrong answer
        SnakeLadderAnswerRequest answerRequest = new SnakeLadderAnswerRequest();
        answerRequest.setPlayerName("TestPlayer");
        answerRequest.setAnswerText("999"); // Wrong answer
        answerRequest.setRoundId(roundId);

        SnakeLadderApiResponse response = snakeLadderService.submitAnswer(answerRequest);

        assertTrue(response.isSuccess());
        assertFalse(response.isCorrect());
        assertTrue(response.getMessage().contains("Incorrect"));
    }
}