package com.dsagamehub.service;

import com.dsagamehub.repository.KnightTourRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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
        return Map.of(
                "playerName", playerName,
                "boardSize", boardSize,
                "startRow", startRow,
                "startCol", startCol,
                "playerAnswer", playerAnswer,
                "algo1TimeMs", 10L,
                "algo2TimeMs", 20L
        );
    }

    // ─────────────────────────────────────────
    // Correct Answers
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
    // Wrong Answers
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
    // Repository (DB Save) — always saves now
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
    void submitAnswer_NegativeAnswer_ShouldAlsoSaveToRepository() {
        Map<String, Object> request = buildRequest("Alice", 8, 0, 0, "-64");
        knightTourService.submitAnswer(request);
        verify(knightTourRepository, times(1)).save(any());
    }

    @Test
    void submitAnswer_CorrectAnswer_16x16_ShouldSaveToRepository() {
        Map<String, Object> request = buildRequest("Bob", 16, 5, 5, "256");
        knightTourService.submitAnswer(request);
        verify(knightTourRepository, times(1)).save(any());
    }

    // ─────────────────────────────────────────
    // Different Start Positions
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
}
