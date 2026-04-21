package com.dsagamehub.service;

import com.dsagamehub.dto.MinimumCostRoundResponse;
import com.dsagamehub.model.MinimumCostRound;
import com.dsagamehub.repository.MinimumCostRoundRepository;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class MinimumCostGameServiceTest {

    @Test
    void shouldRunMinimumCostRoundSuccessfully() {
        MinimumCostRoundRepository repository = mock(MinimumCostRoundRepository.class);

        when(repository.findTopByOrderByRoundNumberDesc()).thenReturn(Optional.empty());
        when(repository.save(any(MinimumCostRound.class))).thenAnswer(invocation -> {
            MinimumCostRound round = invocation.getArgument(0);
            round.setId(1L);
            return round;
        });

        MinimumCostGameService service = new MinimumCostGameService(repository);

        MinimumCostRoundResponse response = service.runRound(50);

        assertNotNull(response);
        assertEquals(1L, response.getRoundId());
        assertEquals(1, response.getRoundNumber());
        assertEquals(50, response.getTaskCount());

        assertNotNull(response.getCostMatrixSummary());
        assertTrue(response.getCostMatrixSummary().getMinimumValue() >= 20);
        assertTrue(response.getCostMatrixSummary().getMaximumValue() <= 200);

        assertNotNull(response.getAlgorithm1());
        assertNotNull(response.getAlgorithm2());

        assertEquals(50, response.getAlgorithm1().getAssignments().size());
        assertEquals(50, response.getAlgorithm2().getAssignments().size());

        assertTrue(response.getAlgorithm1().isValidAssignment());
        assertTrue(response.getAlgorithm2().isValidAssignment());

        assertEquals(
                response.getAlgorithm1().getMinimumTotalCost(),
                response.getAlgorithm2().getMinimumTotalCost()
        );

        verify(repository, times(1)).save(any(MinimumCostRound.class));
    }

    @Test
    void shouldRejectInvalidTaskCount() {
        MinimumCostRoundRepository repository = mock(MinimumCostRoundRepository.class);
        MinimumCostGameService service = new MinimumCostGameService(repository);

        assertThrows(IllegalArgumentException.class, () -> service.runRound(49));
        assertThrows(IllegalArgumentException.class, () -> service.runRound(101));
    }
}