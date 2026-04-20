package com.dsagamehub.service;

import com.dsagamehub.dto.ApiResponse;
import com.dsagamehub.dto.PlayerAnswerRequest;
import com.dsagamehub.model.PlayerAnswer;
import com.dsagamehub.repository.AlgorithmRunRepository;
import com.dsagamehub.repository.GameRoundRepository;
import com.dsagamehub.repository.PlayerAnswerRepository;
import com.dsagamehub.repository.PlayerRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SixteenQueensGameServiceTest {

    @Mock
    private SixteenQueensSequentialService sequentialService;

    @Mock
    private SixteenQueensThreadedService threadedService;

    @Mock
    private SixteenQueensValidationService validationService;

    @Mock
    private PlayerRepository playerRepository;

    @Mock
    private PlayerAnswerRepository playerAnswerRepository;

    @Mock
    private AlgorithmRunRepository algorithmRunRepository;

    @Mock
    private GameRoundRepository gameRoundRepository;

    @InjectMocks
    private SixteenQueensGameService service;

    @Test
    void testCorrectAnswer() {
        PlayerAnswerRequest req = new PlayerAnswerRequest();
        req.setPlayerName("Alice");
        req.setAnswerText("0,4,7,5,2,6,1,3,8,10,12,14,9,11,13,15");

        when(validationService.normalizeAnswer(any())).thenReturn(req.getAnswerText());
        when(validationService.isValidFormat(any())).thenReturn(true);
        when(validationService.parseBoard(any())).thenReturn(new int[16]);

        when(sequentialService.isValidSolution(any())).thenReturn(true);
        when(threadedService.isValidSolution(any())).thenReturn(true);

        when(playerRepository.findByName("Alice")).thenReturn(Optional.empty());
        when(playerAnswerRepository.findByAnswerText(any())).thenReturn(Optional.empty());

        when(gameRoundRepository.findTopByGameNameOrderByRoundNumberDesc(any()))
                .thenReturn(Optional.empty());
        when(algorithmRunRepository.findTopByGameNameOrderBySolutionCountDescCreatedAtDesc(any()))
                .thenReturn(Optional.empty());

        ApiResponse res = service.submitAnswer(req);

        assertTrue(res.isCorrect());
        assertFalse(res.isAlreadyFound());
        assertTrue(res.isSuccess());
    }

    @Test
    void testWrongAnswer() {
        PlayerAnswerRequest req = new PlayerAnswerRequest();
        req.setPlayerName("Bob");
        req.setAnswerText("0,1,2,3");

        when(validationService.normalizeAnswer(any())).thenReturn(req.getAnswerText());
        when(validationService.isValidFormat(any())).thenReturn(true);
        when(validationService.parseBoard(any())).thenReturn(new int[16]);

        when(sequentialService.isValidSolution(any())).thenReturn(false);
        when(threadedService.isValidSolution(any())).thenReturn(false);

        ApiResponse res = service.submitAnswer(req);

        assertFalse(res.isCorrect());
        assertFalse(res.isAlreadyFound());
        assertFalse(res.isSuccess());
    }

    @Test
    void testAlreadyFoundAnswer() {
        PlayerAnswerRequest req = new PlayerAnswerRequest();
        req.setPlayerName("Charlie");
        req.setAnswerText("0,4,7,5,2,6,1,3,8,10,12,14,9,11,13,15");

        when(validationService.normalizeAnswer(any())).thenReturn(req.getAnswerText());
        when(validationService.isValidFormat(any())).thenReturn(true);
        when(validationService.parseBoard(any())).thenReturn(new int[16]);

        when(sequentialService.isValidSolution(any())).thenReturn(true);
        when(threadedService.isValidSolution(any())).thenReturn(true);

        when(playerAnswerRepository.findByAnswerText(any()))
                .thenReturn(Optional.of(mock(PlayerAnswer.class)));

        ApiResponse res = service.submitAnswer(req);

        assertTrue(res.isAlreadyFound());
        assertTrue(res.isCorrect());
        assertFalse(res.isSuccess());
    }
}