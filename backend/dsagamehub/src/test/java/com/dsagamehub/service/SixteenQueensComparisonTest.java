package com.dsagamehub.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class SixteenQueensComparisonTest {

    @Test
    void sequentialAndThreadedShouldReturnSameCount() {
        SixteenQueensSequentialService sequentialService = new SixteenQueensSequentialService();
        SixteenQueensThreadedService threadedService = new SixteenQueensThreadedService();

        long sequentialCount = sequentialService.countQueens();
        long threadedCount = threadedService.countQueensThreaded();

        assertEquals(sequentialCount, threadedCount);
    }
}