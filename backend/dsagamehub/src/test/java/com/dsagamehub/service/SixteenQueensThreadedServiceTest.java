package com.dsagamehub.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class SixteenQueensThreadedServiceTest {

    private final SixteenQueensThreadedService service = new SixteenQueensThreadedService();

    @Test
    void shouldReturnSolutionCount() {
        long solutionCount = service.countQueensThreaded();

        assertTrue(solutionCount > 0);
    }
}