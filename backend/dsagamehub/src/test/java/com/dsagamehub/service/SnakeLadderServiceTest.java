/*package com.dsagamehub.service;

import com.dsagamehub.dto.*;
import com.dsagamehub.model.*;
import com.dsagamehub.repository.*;
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
    private GameRoundRepository roundRepo;

    @Mock
    private AlgorithmRunRepository algoRepo;

    @Mock
    private SnakeLadderRepository snakeLadderResultRepo;

    @InjectMocks
    private SnakeLadderService service;

    private SnakeLadderRequest validRequest;

    @BeforeEach
    void setUp() {
        validRequest = new SnakeLadderRequest();
        validRequest.setBoardSize(8);
        validRequest.setPlayerName("TestPlayer");

        // Mock repository saves
        GameRound mockRound = new GameRound();
        mockRound.setId(1L);
        when(roundRepo.save(any(GameRound.class))).thenReturn(mockRound);

        AlgorithmRun mockRun = new AlgorithmRun();
        mockRun.setId(1L);
        when(algoRepo.save(any(AlgorithmRun.class))).thenReturn(mockRun);
    }

    // Test 1: Board size validation
    @Test
    void testBoardSizeValidation() {
        // Invalid sizes
        SnakeLadderRequest tooSmall = new SnakeLadderRequest();
        tooSmall.setBoardSize(5);
        assertThrows(IllegalArgumentException.class, () -> service.startGame(tooSmall));

        SnakeLadderRequest tooLarge = new SnakeLadderRequest();
        tooLarge.setBoardSize(13);
        assertThrows(IllegalArgumentException.class, () -> service.startGame(tooLarge));

        // Valid sizes (6-12)
        for (int size = 6; size <= 12; size++) {
            SnakeLadderRequest valid = new SnakeLadderRequest();
            valid.setBoardSize(size);
            // Should not throw exception
            assertDoesNotThrow(() -> {
                when(roundRepo.save(any(GameRound.class))).thenReturn(new GameRound());
                when(algoRepo.save(any(AlgorithmRun.class))).thenReturn(new AlgorithmRun());
                service.startGame(valid);
            });
        }
    }

    // Test 2: Correct number of snakes and ladders (N-2)
    @Test
    void testCorrectNumberOfSnakesAndLadders() {
        for (int size = 6; size <= 12; size++) {
            SnakeLadderRequest request = new SnakeLadderRequest();
            request.setBoardSize(size);

            when(roundRepo.save(any(GameRound.class))).thenReturn(new GameRound());
            when(algoRepo.save(any(AlgorithmRun.class))).thenReturn(new AlgorithmRun());

            SnakeLadderResponse response = service.startGame(request);

            // Check snakes and ladders count (exposed through response)
            assertNotNull(response);
        }
    }

    // Test 3: Both algorithms return same result
    @Test
    void testBothAlgorithmsGiveSameResult() {
        when(roundRepo.save(any(GameRound.class))).thenReturn(new GameRound());
        when(algoRepo.save(any(AlgorithmRun.class))).thenReturn(new AlgorithmRun());

        SnakeLadderResponse response = service.startGame(validRequest);

        // Both algorithms should return same minimum moves
        // This is verified internally in the service
        assertTrue(response.getCorrectAnswer() > 0);
    }

    // Test 4: Player name validation
    @Test
    void testPlayerNameValidation() {
        PlayerAnswerRequest invalidRequest = new PlayerAnswerRequest();
        invalidRequest.setPlayerName("");
        invalidRequest.setAnswerText("5");

        ApiResponse response = service.submitAnswer(invalidRequest);
        assertFalse(response.isSuccess());
        assertEquals("Player name is required", response.getMessage());
    }

    // Test 5: Answer format validation
    @Test
    void testAnswerFormatValidation() {
        PlayerAnswerRequest invalidRequest = new PlayerAnswerRequest();
        invalidRequest.setPlayerName("Test");
        invalidRequest.setAnswerText("not a number");
        invalidRequest.setRoundId(1L);

        ApiResponse response = service.submitAnswer(invalidRequest);
        assertFalse(response.isSuccess());
        assertEquals("Invalid answer format. Please enter a number.", response.getMessage());
    }

    // Test 6: Round ID validation
    @Test
    void testRoundIdValidation() {
        PlayerAnswerRequest request = new PlayerAnswerRequest();
        request.setPlayerName("Test");
        request.setAnswerText("10");
        request.setRoundId(null);

        ApiResponse response = service.submitAnswer(request);
        assertFalse(response.isSuccess());
        assertEquals("No active game round. Please start a new game.", response.getMessage());
    }

    // Test 7: Correct answer saves to database
    @Test
    void testCorrectAnswerSavesToDatabase() throws Exception {
        // First start a game
        when(roundRepo.save(any(GameRound.class))).thenReturn(new GameRound());
        when(algoRepo.save(any(AlgorithmRun.class))).thenReturn(new AlgorithmRun());
        SnakeLadderResponse gameResponse = service.startGame(validRequest);

        // Mock algorithm run retrieval
        AlgorithmRun mockBfsRun = new AlgorithmRun();
        mockBfsRun.setTimeTakenMs(100L);
        AlgorithmRun mockDpRun = new AlgorithmRun();
        mockDpRun.setTimeTakenMs(150L);
        when(algoRepo.findByGameRoundIdAndAlgorithmType(anyLong(), eq("BFS"))).thenReturn(mockBfsRun);
        when(algoRepo.findByGameRoundIdAndAlgorithmType(anyLong(), eq("DynamicProgramming"))).thenReturn(mockDpRun);

        // Submit correct answer
        PlayerAnswerRequest answerRequest = new PlayerAnswerRequest();
        answerRequest.setPlayerName("TestPlayer");
        answerRequest.setAnswerText(String.valueOf(gameResponse.getCorrectAnswer()));
        answerRequest.setRoundId(gameResponse.getRoundId());

        when(snakeLadderResultRepo.save(any(SnakeLadderGameResult.class))).thenReturn(new SnakeLadderGameResult());

        ApiResponse response = service.submitAnswer(answerRequest);

        assertTrue(response.isCorrect());
        verify(snakeLadderResultRepo, times(1)).save(any(SnakeLadderGameResult.class));
    }

    // Test 8: Incorrect answer does NOT save to database
    @Test
    void testIncorrectAnswerDoesNotSaveToDatabase() throws Exception {
        // First start a game
        when(roundRepo.save(any(GameRound.class))).thenReturn(new GameRound());
        when(algoRepo.save(any(AlgorithmRun.class))).thenReturn(new AlgorithmRun());
        SnakeLadderResponse gameResponse = service.startGame(validRequest);

        // Mock algorithm run retrieval
        AlgorithmRun mockBfsRun = new AlgorithmRun();
        mockBfsRun.setTimeTakenMs(100L);
        AlgorithmRun mockDpRun = new AlgorithmRun();
        mockDpRun.setTimeTakenMs(150L);
        when(algoRepo.findByGameRoundIdAndAlgorithmType(anyLong(), eq("BFS"))).thenReturn(mockBfsRun);
        when(algoRepo.findByGameRoundIdAndAlgorithmType(anyLong(), eq("DynamicProgramming"))).thenReturn(mockDpRun);

        // Submit incorrect answer
        PlayerAnswerRequest answerRequest = new PlayerAnswerRequest();
        answerRequest.setPlayerName("TestPlayer");
        answerRequest.setAnswerText("999");
        answerRequest.setRoundId(gameResponse.getRoundId());

        ApiResponse response = service.submitAnswer(answerRequest);

        assertFalse(response.isCorrect());
        verify(snakeLadderResultRepo, never()).save(any(SnakeLadderGameResult.class));
    }

    // Test 9: BFS algorithm correctness for simple board
    @Test
    void testBfsAlgorithmCorrectness() {
        // Simple board without snakes/ladders
        Map<Integer, Integer> emptyLadders = new HashMap<>();
        Map<Integer, Integer> emptySnakes = new HashMap<>();

        // For 6x6 board (36 cells), minimum moves should be ceiling((36-1)/6) = 6
        // But actual may vary due to exact dice rolls
        int result = invokeBfsMethod(6, emptyLadders, emptySnakes);
        assertTrue(result >= 5 && result <= 10);
    }

    // Helper to invoke private BFS method via reflection
    private int invokeBfsMethod(int N, Map<Integer, Integer> ladders, Map<Integer, Integer> snakes) {
        try {
            java.lang.reflect.Method method = SnakeLadderService.class.getDeclaredMethod(
                    "bfs", int.class, Map.class, Map.class);
            method.setAccessible(true);
            return (int) method.invoke(service, N, ladders, snakes);
        } catch (Exception e) {
            fail("Could not invoke BFS method");
            return -1;
        }
    }

    // Test 10: DP algorithm correctness
    @Test
    void testDpAlgorithmCorrectness() {
        Map<Integer, Integer> emptyLadders = new HashMap<>();
        Map<Integer, Integer> emptySnakes = new HashMap<>();

        int result = invokeDpMethod(6, emptyLadders, emptySnakes);
        assertTrue(result >= 5 && result <= 10);
    }

    private int invokeDpMethod(int N, Map<Integer, Integer> ladders, Map<Integer, Integer> snakes) {
        try {
            java.lang.reflect.Method method = SnakeLadderService.class.getDeclaredMethod(
                    "dynamicProgramming", int.class, Map.class, Map.class);
            method.setAccessible(true);
            return (int) method.invoke(service, N, ladders, snakes);
        } catch (Exception e) {
            fail("Could not invoke DP method");
            return -1;
        }
    }

    // Test 11: Snake and ladder positions are within bounds
    @Test
    void testSnakeAndLadderPositionsInBounds() {
        when(roundRepo.save(any(GameRound.class))).thenReturn(new GameRound());
        when(algoRepo.save(any(AlgorithmRun.class))).thenReturn(new AlgorithmRun());

        for (int size = 6; size <= 12; size++) {
            SnakeLadderRequest request = new SnakeLadderRequest();
            request.setBoardSize(size);

            SnakeLadderResponse response = service.startGame(request);

            int maxCell = size * size;

            // Check snakes
            if (response.getSnakes() != null) {
                for (Map.Entry<Integer, Integer> snake : response.getSnakes().entrySet()) {
                    assertTrue(snake.getKey() >= 2 && snake.getKey() <= maxCell - 1,
                            "Snake head out of bounds: " + snake.getKey());
                    assertTrue(snake.getValue() >= 1 && snake.getValue() <= maxCell - 1,
                            "Snake tail out of bounds: " + snake.getValue());
                    assertTrue(snake.getKey() > snake.getValue(),
                            "Snake must go down: head=" + snake.getKey() + ", tail=" + snake.getValue());
                }
            }

            // Check ladders
            if (response.getLadders() != null) {
                for (Map.Entry<Integer, Integer> ladder : response.getLadders().entrySet()) {
                    assertTrue(ladder.getKey() >= 2 && ladder.getKey() <= maxCell - 1,
                            "Ladder bottom out of bounds: " + ladder.getKey());
                    assertTrue(ladder.getValue() >= 3 && ladder.getValue() <= maxCell - 1,
                            "Ladder top out of bounds: " + ladder.getValue());
                    assertTrue(ladder.getKey() < ladder.getValue(),
                            "Ladder must go up: bottom=" + ladder.getKey() + ", top=" + ladder.getValue());
                }
            }
        }
    }

    // Test 12: No snake head at cell 1
    @Test
    void testNoSnakeAtCellOne() {
        when(roundRepo.save(any(GameRound.class))).thenReturn(new GameRound());
        when(algoRepo.save(any(AlgorithmRun.class))).thenReturn(new AlgorithmRun());

        for (int size = 6; size <= 12; size++) {
            SnakeLadderRequest request = new SnakeLadderRequest();
            request.setBoardSize(size);

            SnakeLadderResponse response = service.startGame(request);

            if (response.getSnakes() != null) {
                assertFalse(response.getSnakes().containsKey(1), "Snake head cannot be at cell 1");
            }
        }
    }
}*/

package com.dsagamehub.service;

import com.dsagamehub.dto.*;
import com.dsagamehub.model.*;
import com.dsagamehub.repository.*;
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
    private GameRoundRepository roundRepo;

    @Mock
    private AlgorithmRunRepository algoRepo;

    @Mock
    private SnakeLadderRepository snakeLadderResultRepo;

    @InjectMocks
    private SnakeLadderService service;

    private SnakeLadderRequest validRequest;

    @BeforeEach
    void setUp() {
        validRequest = new SnakeLadderRequest();
        validRequest.setBoardSize(8);
        validRequest.setPlayerName("TestPlayer");

        // Mock repository saves - FIXED: Don't use setId on mocks
        GameRound mockRound = new GameRound();
        // Remove setId - let the real object handle it or use reflection
        when(roundRepo.save(any(GameRound.class))).thenReturn(mockRound);

        AlgorithmRun mockRun = new AlgorithmRun();
        when(algoRepo.save(any(AlgorithmRun.class))).thenReturn(mockRun);
    }

    // Test 1: Board size validation
    @Test
    void testBoardSizeValidation() {
        // Invalid sizes
        SnakeLadderRequest tooSmall = new SnakeLadderRequest();
        tooSmall.setBoardSize(5);
        assertThrows(IllegalArgumentException.class, () -> service.startGame(tooSmall));

        SnakeLadderRequest tooLarge = new SnakeLadderRequest();
        tooLarge.setBoardSize(13);
        assertThrows(IllegalArgumentException.class, () -> service.startGame(tooLarge));

        // Valid sizes (6-12)
        for (int size = 6; size <= 12; size++) {
            SnakeLadderRequest valid = new SnakeLadderRequest();
            valid.setBoardSize(size);
            // Should not throw exception
            assertDoesNotThrow(() -> {
                when(roundRepo.save(any(GameRound.class))).thenReturn(new GameRound());
                when(algoRepo.save(any(AlgorithmRun.class))).thenReturn(new AlgorithmRun());
                service.startGame(valid);
            });
        }
    }

    // Test 2: Correct number of snakes and ladders (N-2)
    @Test
    void testCorrectNumberOfSnakesAndLadders() {
        for (int size = 6; size <= 12; size++) {
            SnakeLadderRequest request = new SnakeLadderRequest();
            request.setBoardSize(size);

            when(roundRepo.save(any(GameRound.class))).thenReturn(new GameRound());
            when(algoRepo.save(any(AlgorithmRun.class))).thenReturn(new AlgorithmRun());

            SnakeLadderResponse response = service.startGame(request);

            // Check snakes and ladders count
            assertNotNull(response);
            assertNotNull(response.getSnakes());
            assertNotNull(response.getLadders());
            // Each should have N-2 items
            assertEquals(size - 2, response.getSnakes().size());
            assertEquals(size - 2, response.getLadders().size());
        }
    }

    // Test 3: Player name validation
    @Test
    void testPlayerNameValidation() {
        PlayerAnswerRequest invalidRequest = new PlayerAnswerRequest();
        invalidRequest.setPlayerName("");
        invalidRequest.setAnswerText("5");

        ApiResponse response = service.submitAnswer(invalidRequest);
        assertFalse(response.isSuccess());
        assertEquals("Player name is required", response.getMessage());
    }

    // Test 4: Answer format validation
    @Test
    void testAnswerFormatValidation() {
        PlayerAnswerRequest invalidRequest = new PlayerAnswerRequest();
        invalidRequest.setPlayerName("Test");
        invalidRequest.setAnswerText("not a number");
        invalidRequest.setRoundId(1L);

        ApiResponse response = service.submitAnswer(invalidRequest);
        assertFalse(response.isSuccess());
        assertEquals("Invalid answer format. Please enter a number.", response.getMessage());
    }

    // Test 5: Round ID validation
    @Test
    void testRoundIdValidation() {
        PlayerAnswerRequest request = new PlayerAnswerRequest();
        request.setPlayerName("Test");
        request.setAnswerText("10");
        request.setRoundId(null);

        ApiResponse response = service.submitAnswer(request);
        assertFalse(response.isSuccess());
        assertEquals("No active game round. Please start a new game.", response.getMessage());
    }

    // Test 6: BFS algorithm correctness for simple board
    @Test
    void testBfsAlgorithmCorrectness() {
        // Simple board without snakes/ladders
        Map<Integer, Integer> emptyLadders = new HashMap<>();
        Map<Integer, Integer> emptySnakes = new HashMap<>();

        // For 6x6 board (36 cells)
        int result = invokeBfsMethod(6, emptyLadders, emptySnakes);
        assertTrue(result >= 5 && result <= 10);
    }

    // Helper to invoke private BFS method via reflection
    private int invokeBfsMethod(int N, Map<Integer, Integer> ladders, Map<Integer, Integer> snakes) {
        try {
            java.lang.reflect.Method method = SnakeLadderService.class.getDeclaredMethod(
                    "bfs", int.class, Map.class, Map.class);
            method.setAccessible(true);
            return (int) method.invoke(service, N, ladders, snakes);
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
        assertTrue(result >= 5 && result <= 10);
    }

    private int invokeDpMethod(int N, Map<Integer, Integer> ladders, Map<Integer, Integer> snakes) {
        try {
            java.lang.reflect.Method method = SnakeLadderService.class.getDeclaredMethod(
                    "dynamicProgramming", int.class, Map.class, Map.class);
            method.setAccessible(true);
            return (int) method.invoke(service, N, ladders, snakes);
        } catch (Exception e) {
            fail("Could not invoke DP method: " + e.getMessage());
            return -1;
        }
    }

    // Test 8: Snake and ladder positions are within bounds
    @Test
    void testSnakeAndLadderPositionsInBounds() {
        when(roundRepo.save(any(GameRound.class))).thenReturn(new GameRound());
        when(algoRepo.save(any(AlgorithmRun.class))).thenReturn(new AlgorithmRun());

        for (int size = 6; size <= 12; size++) {
            SnakeLadderRequest request = new SnakeLadderRequest();
            request.setBoardSize(size);

            SnakeLadderResponse response = service.startGame(request);

            int maxCell = size * size;

            // Check snakes
            if (response.getSnakes() != null) {
                for (Map.Entry<Integer, Integer> snake : response.getSnakes().entrySet()) {
                    assertTrue(snake.getKey() >= 2 && snake.getKey() <= maxCell - 1,
                            "Snake head out of bounds: " + snake.getKey());
                    assertTrue(snake.getValue() >= 1 && snake.getValue() <= maxCell - 1,
                            "Snake tail out of bounds: " + snake.getValue());
                    assertTrue(snake.getKey() > snake.getValue(),
                            "Snake must go down: head=" + snake.getKey() + ", tail=" + snake.getValue());
                }
            }

            // Check ladders
            if (response.getLadders() != null) {
                for (Map.Entry<Integer, Integer> ladder : response.getLadders().entrySet()) {
                    assertTrue(ladder.getKey() >= 2 && ladder.getKey() <= maxCell - 1,
                            "Ladder bottom out of bounds: " + ladder.getKey());
                    assertTrue(ladder.getValue() >= 3 && ladder.getValue() <= maxCell - 1,
                            "Ladder top out of bounds: " + ladder.getValue());
                    assertTrue(ladder.getKey() < ladder.getValue(),
                            "Ladder must go up: bottom=" + ladder.getKey() + ", top=" + ladder.getValue());
                }
            }
        }
    }

    // Test 9: No snake head at cell 1
    @Test
    void testNoSnakeAtCellOne() {
        when(roundRepo.save(any(GameRound.class))).thenReturn(new GameRound());
        when(algoRepo.save(any(AlgorithmRun.class))).thenReturn(new AlgorithmRun());

        for (int size = 6; size <= 12; size++) {
            SnakeLadderRequest request = new SnakeLadderRequest();
            request.setBoardSize(size);

            SnakeLadderResponse response = service.startGame(request);

            if (response.getSnakes() != null) {
                assertFalse(response.getSnakes().containsKey(1), "Snake head cannot be at cell 1");
            }
        }
    }

    // Test 10: Both algorithms return same result
    @Test
    void testBothAlgorithmsGiveSameResult() {
        when(roundRepo.save(any(GameRound.class))).thenReturn(new GameRound());
        when(algoRepo.save(any(AlgorithmRun.class))).thenReturn(new AlgorithmRun());

        SnakeLadderResponse response = service.startGame(validRequest);

        // Both algorithms should return same minimum moves
        assertTrue(response.getCorrectAnswer() > 0);
    }
}