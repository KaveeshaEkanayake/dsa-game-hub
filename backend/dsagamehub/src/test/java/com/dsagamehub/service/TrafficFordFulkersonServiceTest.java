package com.dsagamehub.service;

import com.dsagamehub.model.TrafficEdge;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TrafficFordFulkersonServiceTest {

    private TrafficFordFulkersonService fordFulkersonService;

    @BeforeEach
    void setUp() {
        fordFulkersonService = new TrafficFordFulkersonService();
    }

    private List<TrafficEdge> buildSampleGraph() {
        return Arrays.asList(
                new TrafficEdge("A", "B", 10, 1L),
                new TrafficEdge("A", "C", 10, 1L),
                new TrafficEdge("A", "D", 10, 1L),
                new TrafficEdge("B", "E", 10, 1L),
                new TrafficEdge("B", "F", 10, 1L),
                new TrafficEdge("C", "E", 10, 1L),
                new TrafficEdge("C", "F", 10, 1L),
                new TrafficEdge("D", "F", 10, 1L),
                new TrafficEdge("E", "G", 10, 1L),
                new TrafficEdge("E", "H", 10, 1L),
                new TrafficEdge("F", "H", 10, 1L),
                new TrafficEdge("G", "T", 10, 1L),
                new TrafficEdge("H", "T", 10, 1L)
        );
    }

    @Test
    void testComputeMaxFlow_returnsPositiveValue() {
        List<TrafficEdge> edges = buildSampleGraph();
        int maxFlow = fordFulkersonService.computeMaxFlow(edges);
        assertTrue(maxFlow > 0, "Max flow should be greater than 0");
    }

    @Test
    void testComputeMaxFlow_withEqualCapacities() {
        List<TrafficEdge> edges = buildSampleGraph();
        int maxFlow = fordFulkersonService.computeMaxFlow(edges);
        assertEquals(20, maxFlow, "Max flow should be 20 with capacity 10 on all edges");
    }

    @Test
    void testComputeMaxFlow_withMinCapacity() {
        List<TrafficEdge> edges = Arrays.asList(
                new TrafficEdge("A", "B", 5, 1L),
                new TrafficEdge("A", "C", 5, 1L),
                new TrafficEdge("A", "D", 5, 1L),
                new TrafficEdge("B", "E", 5, 1L),
                new TrafficEdge("B", "F", 5, 1L),
                new TrafficEdge("C", "E", 5, 1L),
                new TrafficEdge("C", "F", 5, 1L),
                new TrafficEdge("D", "F", 5, 1L),
                new TrafficEdge("E", "G", 5, 1L),
                new TrafficEdge("E", "H", 5, 1L),
                new TrafficEdge("F", "H", 5, 1L),
                new TrafficEdge("G", "T", 5, 1L),
                new TrafficEdge("H", "T", 5, 1L)
        );
        int maxFlow = fordFulkersonService.computeMaxFlow(edges);
        assertTrue(maxFlow > 0, "Max flow should be greater than 0 with min capacity");
    }

    @Test
    void testComputeMaxFlow_withMaxCapacity() {
        List<TrafficEdge> edges = Arrays.asList(
                new TrafficEdge("A", "B", 15, 1L),
                new TrafficEdge("A", "C", 15, 1L),
                new TrafficEdge("A", "D", 15, 1L),
                new TrafficEdge("B", "E", 15, 1L),
                new TrafficEdge("B", "F", 15, 1L),
                new TrafficEdge("C", "E", 15, 1L),
                new TrafficEdge("C", "F", 15, 1L),
                new TrafficEdge("D", "F", 15, 1L),
                new TrafficEdge("E", "G", 15, 1L),
                new TrafficEdge("E", "H", 15, 1L),
                new TrafficEdge("F", "H", 15, 1L),
                new TrafficEdge("G", "T", 15, 1L),
                new TrafficEdge("H", "T", 15, 1L)
        );
        int maxFlow = fordFulkersonService.computeMaxFlow(edges);
        assertTrue(maxFlow > 0, "Max flow should be greater than 0 with max capacity");
    }

    @Test
    void testMeasureTime_returnsNonNegative() {
        List<TrafficEdge> edges = buildSampleGraph();
        long time = fordFulkersonService.measureTime(edges);
        assertTrue(time >= 0, "Time should be non negative");
    }

    @Test
    void testComputeMaxFlow_consistentResults() {
        List<TrafficEdge> edges = buildSampleGraph();
        int firstRun = fordFulkersonService.computeMaxFlow(edges);
        int secondRun = fordFulkersonService.computeMaxFlow(edges);
        assertEquals(firstRun, secondRun, "Max flow should be consistent across runs");
    }
}