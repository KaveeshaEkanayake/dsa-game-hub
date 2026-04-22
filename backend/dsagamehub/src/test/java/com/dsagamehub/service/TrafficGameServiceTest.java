package com.dsagamehub.service;

import com.dsagamehub.exception.TrafficGameException;
import com.dsagamehub.model.*;
import com.dsagamehub.repository.TrafficGameResultRepository;
import com.dsagamehub.repository.TrafficGameRoundRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TrafficGameServiceTest {

    @Mock
    private TrafficGameRoundRepository roundRepository;

    @Mock
    private TrafficGameResultRepository resultRepository;

    @Mock
    private TrafficFordFulkersonService fordFulkersonService;

    @Mock
    private TrafficEdmondsKarpService edmondsKarpService;

    @InjectMocks
    private TrafficGameService trafficGameService;

    private TrafficGameRound mockRound;

    @BeforeEach
    void setUp() {
        mockRound = new TrafficGameRound(20, 5L, 3L);
        mockRound.setId(1L);
    }

    @Test
    void testGenerateNewRound_success() {
        when(fordFulkersonService.computeMaxFlow(any())).thenReturn(20);
        when(edmondsKarpService.computeMaxFlow(any())).thenReturn(20);
        when(roundRepository.save(any())).thenReturn(mockRound);

        TrafficGameResponse response = trafficGameService.generateNewRound();

        assertNotNull(response);
        assertEquals(20, response.getCorrectMaxFlow());
        verify(roundRepository, times(1)).save(any());
    }

    @Test
    void testGenerateNewRound_algorithmMismatch_throwsException() {
        when(fordFulkersonService.computeMaxFlow(any())).thenReturn(20);
        when(edmondsKarpService.computeMaxFlow(any())).thenReturn(15);

        assertThrows(TrafficGameException.class, () -> {
            trafficGameService.generateNewRound();
        });
    }

    @Test
    void testSubmitAnswer_correctAnswer_returnsWin() {
        when(roundRepository.findById(1L)).thenReturn(Optional.of(mockRound));
        when(resultRepository.save(any())).thenReturn(new TrafficGameResult());

        TrafficAnswerRequest request = new TrafficAnswerRequest(1L, "John", 20);
        TrafficGameResponse response = trafficGameService.submitAnswer(request);

        assertNotNull(response);
        assertTrue(response.isCorrect());
        assertEquals("WIN", getResultFromMessage(response.getMessage()));
    }

    @Test
    void testSubmitAnswer_wrongAnswer_returnsLose() {
        when(roundRepository.findById(1L)).thenReturn(Optional.of(mockRound));
        when(resultRepository.save(any())).thenReturn(new TrafficGameResult());

        TrafficAnswerRequest request = new TrafficAnswerRequest(1L, "John", 10);
        TrafficGameResponse response = trafficGameService.submitAnswer(request);

        assertNotNull(response);
        assertFalse(response.isCorrect());
    }

    @Test
    void testSubmitAnswer_emptyPlayerName_throwsException() {
        TrafficAnswerRequest request = new TrafficAnswerRequest(1L, "", 20);

        assertThrows(TrafficGameException.class, () -> {
            trafficGameService.submitAnswer(request);
        });
    }

    @Test
    void testSubmitAnswer_nullPlayerName_throwsException() {
        TrafficAnswerRequest request = new TrafficAnswerRequest(1L, null, 20);

        assertThrows(TrafficGameException.class, () -> {
            trafficGameService.submitAnswer(request);
        });
    }

    @Test
    void testSubmitAnswer_negativeAnswer_throwsException() {
        TrafficAnswerRequest request = new TrafficAnswerRequest(1L, "John", -5);

        assertThrows(TrafficGameException.class, () -> {
            trafficGameService.submitAnswer(request);
        });
    }

    @Test
    void testSubmitAnswer_roundNotFound_throwsException() {
        when(roundRepository.findById(99L)).thenReturn(Optional.empty());

        TrafficAnswerRequest request = new TrafficAnswerRequest(99L, "John", 20);

        assertThrows(TrafficGameException.class, () -> {
            trafficGameService.submitAnswer(request);
        });
    }

    @Test
    void testGetCorrectAnswers_returnsList() {
        TrafficGameResult result = new TrafficGameResult(
                "John", 20, 20, true, "WIN", 1L, 5L, 3L
        );
        when(resultRepository.findByIsCorrectTrue()).thenReturn(Arrays.asList(result));

        List<TrafficGameResult> results = trafficGameService.getCorrectAnswers();

        assertNotNull(results);
        assertEquals(1, results.size());
        assertEquals("John", results.get(0).getPlayerName());
    }

    @Test
    void testGetAllRounds_returnsList() {
        when(roundRepository.findAllByOrderByCreatedAtDesc()).thenReturn(Arrays.asList(mockRound));

        List<TrafficGameRound> rounds = trafficGameService.getAllRounds();

        assertNotNull(rounds);
        assertEquals(1, rounds.size());
    }

    @Test
    void testGetResultsByRound_invalidRoundId_throwsException() {
        assertThrows(TrafficGameException.class, () -> {
            trafficGameService.getResultsByRound(-1L);
        });
    }

    private String getResultFromMessage(String message) {
        if (message != null && message.startsWith("Correct")) {
            return "WIN";
        }
        return "LOSE";
    }
}