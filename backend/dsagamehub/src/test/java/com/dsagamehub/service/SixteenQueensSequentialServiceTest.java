package com.dsagamehub.service;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class SixteenQueensSequentialServiceTest {

    private final SixteenQueensSequentialService service = new SixteenQueensSequentialService();

    @Test
    void shouldReturnSolutions() {
        List<String> solutions = service.solveQueens();

        assertNotNull(solutions);
        assertFalse(solutions.isEmpty());
    }
}