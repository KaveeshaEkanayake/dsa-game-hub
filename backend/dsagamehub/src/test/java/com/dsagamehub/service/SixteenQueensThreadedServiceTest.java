package com.dsagamehub.service;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class SixteenQueensThreadedServiceTest {

    private final SixteenQueensThreadedService service = new SixteenQueensThreadedService();

    @Test
    void shouldReturnSolutions() {
        List<String> solutions = service.solveQueensThreaded();

        assertNotNull(solutions);
        assertFalse(solutions.isEmpty());
    }
}