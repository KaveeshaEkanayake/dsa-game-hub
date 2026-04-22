package com.dsagamehub.service;

import com.dsagamehub.model.KnightTourResult;
import com.dsagamehub.repository.KnightTourRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class KnightTourServiceTest {

    @Mock
    private KnightTourRepository knightTourRepository;

    @InjectMocks
    private KnightTourService knightTourService;

    private Map<String, Object> buildRequest(String playerName, int boardSize,
                                             int startRow, int startCol,
                                             String playerAnswer) {
        return new java.util.HashMap<>(Map.of(
                "playerName", playerName,
                "boardSize", boardSize,
                "startRow", startRow,
                "startCol", startCol,
                "playerAnswer", playerAnswer,
                "algo1TimeMs", 10L,
                "algo2TimeMs", 20L
        ));
    }

    // ─────────────────────────────────────────
    // submitAnswer() — Correct Answers
    // ─────────────────────────────────────────

    @Test
    void submitAnswer_CorrectAnswer_8x8_ShouldReturnTrue() {
        Map<String, Object> request = buildRequest("Alice", 8, 0, 0, "64");
        assertTrue((Boolean) knightTourService.submitAnswer(request).get("correct"));
    }

    @Test
    void submitAnswer_CorrectAnswer_16x16_ShouldReturnTrue() {
        Map<String, Object> request = buildRequest("Alice", 16, 0, 0, "256");
        assertTrue((Boolean) knightTourService.submitAnswer(request).get("correct"));
    }

    @Test
    void submitAnswer_ShouldReturnCorrectAnswerValue_8x8() {
        Map<String, Object> request = buildRequest("Alice", 8, 0, 0, "64");
        assertEquals(64, knightTourService.submitAnswer(request).get("correctAnswer"));
    }

    @Test
    void submitAnswer_ShouldReturnCorrectAnswerValue_16x16() {
        Map<String, Object> request = buildRequest("Alice", 16, 0, 0, "256");
        assertEquals(256, knightTourService.submitAnswer(request).get("correctAnswer"));
    }

    @Test
    void submitAnswer_ShouldReturnPlayerName() {
        Map<String, Object> request = buildRequest("Kaveesha", 8, 0, 0, "64");
        assertEquals("Kaveesha", knightTourService.submitAnswer(request).get("playerName"));
    }

    // ─────────────────────────────────────────
    // submitAnswer() — Game Result
    // ─────────────────────────────────────────

    @Test
    void submitAnswer_CorrectAnswer_ShouldReturnWin() {
        Map<String, Object> request = buildRequest("Alice", 8, 0, 0, "64");
        assertEquals("win", knightTourService.submitAnswer(request).get("gameResult"));
    }

    @Test
    void submitAnswer_WrongAnswer_ShouldReturnLose() {
        Map<String, Object> request = buildRequest("Alice", 8, 0, 0, "32");
        assertEquals("lose", knightTourService.submitAnswer(request).get("gameResult"));
    }

    // ─────────────────────────────────────────
    // submitAnswer() — Wrong Answers
    // ─────────────────────────────────────────

    @Test
    void submitAnswer_WrongAnswer_ShouldReturnFalse() {
        Map<String, Object> request = buildRequest("Alice", 8, 0, 0, "32");
        assertFalse((Boolean) knightTourService.submitAnswer(request).get("correct"));
    }

    @Test
    void submitAnswer_ZeroAnswer_ShouldReturnFalse() {
        Map<String, Object> request = buildRequest("Alice", 8, 0, 0, "0");
        assertFalse((Boolean) knightTourService.submitAnswer(request).get("correct"));
    }

    @Test
    void submitAnswer_NegativeAnswer_ShouldReturnFalse() {
        Map<String, Object> request = buildRequest("Alice", 8, 0, 0, "-64");
        assertFalse((Boolean) knightTourService.submitAnswer(request).get("correct"));
    }

    @Test
    void submitAnswer_EmptyAnswer_ShouldReturnFalse() {
        Map<String, Object> request = buildRequest("Alice", 8, 0, 0, "");
        assertFalse((Boolean) knightTourService.submitAnswer(request).get("correct"));
    }

    @Test
    void submitAnswer_AnswerWithSpaces_ShouldStillWork() {
        Map<String, Object> request = buildRequest("Alice", 8, 0, 0, "  64  ");
        assertTrue((Boolean) knightTourService.submitAnswer(request).get("correct"));
    }

    @Test
    void submitAnswer_OffByOne_Lower_ShouldReturnFalse() {
        Map<String, Object> request = buildRequest("Alice", 8, 0, 0, "63");
        assertFalse((Boolean) knightTourService.submitAnswer(request).get("correct"));
    }

    @Test
    void submitAnswer_OffByOne_Upper_ShouldReturnFalse() {
        Map<String, Object> request = buildRequest("Alice", 8, 0, 0, "65");
        assertFalse((Boolean) knightTourService.submitAnswer(request).get("correct"));
    }

    // ─────────────────────────────────────────
    // submitAnswer() — Repository (always saves)
    // ─────────────────────────────────────────

    @Test
    void submitAnswer_CorrectAnswer_ShouldSaveToRepository() {
        Map<String, Object> request = buildRequest("Alice", 8, 0, 0, "64");
        knightTourService.submitAnswer(request);
        verify(knightTourRepository, times(1)).save(any());
    }

    @Test
    void submitAnswer_WrongAnswer_ShouldAlsoSaveToRepository() {
        Map<String, Object> request = buildRequest("Alice", 8, 0, 0, "10");
        knightTourService.submitAnswer(request);
        verify(knightTourRepository, times(1)).save(any());
    }

    @Test
    void submitAnswer_ZeroAnswer_ShouldAlsoSaveToRepository() {
        Map<String, Object> request = buildRequest("Alice", 8, 0, 0, "0");
        knightTourService.submitAnswer(request);
        verify(knightTourRepository, times(1)).save(any());
    }

    @Test
    void submitAnswer_16x16_ShouldSaveToRepository() {
        Map<String, Object> request = buildRequest("Bob", 16, 5, 5, "256");
        knightTourService.submitAnswer(request);
        verify(knightTourRepository, times(1)).save(any());
    }

    // ─────────────────────────────────────────
    // submitAnswer() — Algo Times
    // ─────────────────────────────────────────

    @Test
    void submitAnswer_ShouldReturnAlgo1Time() {
        Map<String, Object> request = buildRequest("Alice", 8, 0, 0, "64");
        Map<String, Object> result = knightTourService.submitAnswer(request);
        assertNotNull(result.get("algo1TimeMs"));
    }

    @Test
    void submitAnswer_ShouldReturnAlgo2Time() {
        Map<String, Object> request = buildRequest("Alice", 8, 0, 0, "64");
        Map<String, Object> result = knightTourService.submitAnswer(request);
        assertNotNull(result.get("algo2TimeMs"));
    }

    // ─────────────────────────────────────────
    // submitAnswer() — Different Start Positions
    // ─────────────────────────────────────────

    @Test
    void submitAnswer_CornerPosition_ShouldReturnTrue() {
        Map<String, Object> request = buildRequest("Alice", 8, 0, 0, "64");
        assertTrue((Boolean) knightTourService.submitAnswer(request).get("correct"));
    }

    @Test
    void submitAnswer_CenterPosition_ShouldReturnTrue() {
        Map<String, Object> request = buildRequest("Alice", 8, 4, 4, "64");
        assertTrue((Boolean) knightTourService.submitAnswer(request).get("correct"));
    }

    @Test
    void submitAnswer_EdgePosition_ShouldReturnTrue() {
        Map<String, Object> request = buildRequest("Alice", 8, 0, 7, "64");
        assertTrue((Boolean) knightTourService.submitAnswer(request).get("correct"));
    }

    // ─────────────────────────────────────────
    // recordDraw() tests
    // ─────────────────────────────────────────

    @Test
    void recordDraw_ShouldReturnDrawResult() {
        Map<String, Object> request = new java.util.HashMap<>(Map.of(
                "playerName", "Alice",
                "boardSize", 8,
                "startRow", 0,
                "startCol", 0,
                "algo1TimeMs", 10L,
                "algo2TimeMs", 20L
        ));
        Map<String, Object> result = knightTourService.recordDraw(request);
        assertEquals("draw", result.get("gameResult"));
    }

    @Test
    void recordDraw_ShouldReturnPlayerName() {
        Map<String, Object> request = new java.util.HashMap<>(Map.of(
                "playerName", "Alice",
                "boardSize", 8,
                "startRow", 0,
                "startCol", 0,
                "algo1TimeMs", 10L,
                "algo2TimeMs", 20L
        ));
        Map<String, Object> result = knightTourService.recordDraw(request);
        assertEquals("Alice", result.get("playerName"));
    }

    @Test
    void recordDraw_ShouldSaveToRepository() {
        Map<String, Object> request = new java.util.HashMap<>(Map.of(
                "playerName", "Alice",
                "boardSize", 8,
                "startRow", 0,
                "startCol", 0,
                "algo1TimeMs", 10L,
                "algo2TimeMs", 20L
        ));
        knightTourService.recordDraw(request);
        verify(knightTourRepository, times(1)).save(any());
    }

    @Test
    void recordDraw_ShouldReturnCorrectAnswer() {
        Map<String, Object> request = new java.util.HashMap<>(Map.of(
                "playerName", "Alice",
                "boardSize", 8,
                "startRow", 0,
                "startCol", 0,
                "algo1TimeMs", 10L,
                "algo2TimeMs", 20L
        ));
        Map<String, Object> result = knightTourService.recordDraw(request);
        assertEquals(64, result.get("correctAnswer"));
    }

    // ─────────────────────────────────────────
    // getLeaderboard() tests
    // ─────────────────────────────────────────

    @Test
    void getLeaderboard_ShouldReturnList() {
        when(knightTourRepository.findTopCorrectResults()).thenReturn(List.of());
        List<Map<String, Object>> leaderboard = knightTourService.getLeaderboard();
        assertNotNull(leaderboard);
    }

    @Test
    void getLeaderboard_ShouldReturnEmptyWhenNoResults() {
        when(knightTourRepository.findTopCorrectResults()).thenReturn(List.of());
        List<Map<String, Object>> leaderboard = knightTourService.getLeaderboard();
        assertTrue(leaderboard.isEmpty());
    }

    @Test
    void getLeaderboard_ShouldReturnCorrectFields() {
        KnightTourResult mockResult = new KnightTourResult();
        mockResult.setPlayerName("Alice");
        mockResult.setBoardSize(8);
        mockResult.setAlgorithm1TimeMs(10L);
        mockResult.setAlgorithm2TimeMs(20L);

        when(knightTourRepository.findTopCorrectResults()).thenReturn(List.of(mockResult));
        List<Map<String, Object>> leaderboard = knightTourService.getLeaderboard();

        assertEquals(1, leaderboard.size());
        assertEquals("Alice", leaderboard.get(0).get("playerName"));
        assertEquals(8, leaderboard.get(0).get("boardSize"));
    }

    // ─────────────────────────────────────────
    // getAllResults() tests
    // ─────────────────────────────────────────

    @Test
    void getAllResults_ShouldReturnList() {
        when(knightTourRepository.findAllOrderByCreatedAtDesc()).thenReturn(List.of());
        List<Map<String, Object>> results = knightTourService.getAllResults();
        assertNotNull(results);
    }

    @Test
    void getAllResults_ShouldReturnCorrectFields() {
        KnightTourResult mockResult = new KnightTourResult();
        mockResult.setPlayerName("Bob");
        mockResult.setBoardSize(16);
        mockResult.setCorrect(true);
        mockResult.setGameResult("win");
        mockResult.setAlgorithm1TimeMs(5L);
        mockResult.setAlgorithm2TimeMs(10L);

        when(knightTourRepository.findAllOrderByCreatedAtDesc()).thenReturn(List.of(mockResult));
        List<Map<String, Object>> results = knightTourService.getAllResults();

        assertEquals(1, results.size());
        assertEquals("Bob", results.get(0).get("playerName"));
        assertEquals(16, results.get(0).get("boardSize"));
        assertEquals(true, results.get(0).get("correct"));
        assertEquals("win", results.get(0).get("gameResult"));
    }

    // ─────────────────────────────────────────
// Real Algorithm Tests (no mocking)
// ─────────────────────────────────────────

    @Test
    void warnsdorff_ShouldProduceValidKnightTour_8x8() {
        Map<String, Object> result = knightTourService.generateGame(8);
        List<int[]> solution = (List<int[]>) result.get("solution");

        assertNotNull(solution);
        assertFalse(solution.isEmpty());

        // Verify every move is a valid knight move
        for (int i = 1; i < solution.size(); i++) {
            int[] prev = solution.get(i - 1);
            int[] curr = solution.get(i);
            int dr = Math.abs(curr[0] - prev[0]);
            int dc = Math.abs(curr[1] - prev[1]);
            assertTrue((dr == 2 && dc == 1) || (dr == 1 && dc == 2),
                    "Invalid knight move at step " + i);
        }
    }

    @Test
    void warnsdorff_ShouldNotVisitSameSquareTwice_8x8() {
        Map<String, Object> result = knightTourService.generateGame(8);
        List<int[]> solution = (List<int[]>) result.get("solution");

        long uniqueSquares = solution.stream()
                .map(pos -> pos[0] * 100 + pos[1])
                .distinct()
                .count();

        assertEquals(solution.size(), uniqueSquares,
                "Knight visited same square more than once");
    }

    @Test
    void warnsdorff_ShouldStartAtCorrectPosition_8x8() {
        Map<String, Object> result = knightTourService.generateGame(8);
        int startRow = (int) result.get("startRow");
        int startCol = (int) result.get("startCol");
        List<int[]> solution = (List<int[]>) result.get("solution");

        assertArrayEquals(new int[]{startRow, startCol}, solution.get(0));
    }

    @Test
    void warnsdorff_ShouldProduceValidKnightTour_6x6() {
        Map<String, Object> result = knightTourService.generateGame(6);
        List<int[]> solution = (List<int[]>) result.get("solution");

        assertNotNull(solution);
        assertFalse(solution.isEmpty());

        for (int i = 1; i < solution.size(); i++) {
            int[] prev = solution.get(i - 1);
            int[] curr = solution.get(i);
            int dr = Math.abs(curr[0] - prev[0]);
            int dc = Math.abs(curr[1] - prev[1]);
            assertTrue((dr == 2 && dc == 1) || (dr == 1 && dc == 2),
                    "Invalid knight move at step " + i);
        }
    }

    @Test
    void warnsdorff_AlgoTimeShouldBeRecorded_8x8() {
        Map<String, Object> result = knightTourService.generateGame(8);
        long algo1Time = (long) result.get("algo1TimeMs");
        assertTrue(algo1Time >= 1, "Algorithm time should be at least 1ms");
    }

    // NOTE: Real backtracking test is not included in automated tests because backtracking on 8x8 can take up to 5 seconds (our timeout limit) due to O(8^64) worst case complexity.
    // Backtracking is verified manually by running the application and checking the algo2TimeMs recorded in the database.
    // The timeout mechanism is validated by the generateGame() implementation.
}