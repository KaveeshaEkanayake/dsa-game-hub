package com.dsagamehub.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class SixteenQueensSequentialServiceTest {

    private final SixteenQueensSequentialService service = new SixteenQueensSequentialService();

    @Test
    void shouldReturnSolutionCount() {
        long solutionCount = service.countQueens();

        assertTrue(solutionCount > 0);
    }
}