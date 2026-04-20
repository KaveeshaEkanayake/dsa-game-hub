package com.dsagamehub.service;

import com.dsagamehub.dto.MinimumCostRoundResponse;
import com.dsagamehub.model.MinimumCostRound;
import com.dsagamehub.repository.MinimumCostRoundRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class MinimumCostGameService {

    private static final int MIN_TASKS = 50;
    private static final int MAX_TASKS = 100;
    private static final int MIN_COST = 20;
    private static final int MAX_COST = 200;

    private static final String HUNGARIAN_NAME = "HUNGARIAN";
    private static final String MIN_COST_FLOW_NAME = "MIN_COST_MAX_FLOW";
    private static final String TIE = "TIE";

    private static final String HUNGARIAN_LOGIC =
            "Hungarian algorithm (exact optimal assignment, O(n^3))";
    private static final String MIN_COST_FLOW_LOGIC =
            "Min-Cost Max-Flow using successive shortest augmenting path (exact optimal assignment)";

    private final MinimumCostRoundRepository roundRepository;

    public MinimumCostGameService(MinimumCostRoundRepository roundRepository) {
        this.roundRepository = roundRepository;
    }

    public MinimumCostRoundResponse runRandomRound() {
        int randomTaskCount = ThreadLocalRandom.current().nextInt(MIN_TASKS, MAX_TASKS + 1);
        return runRoundInternal(randomTaskCount, "RANDOM");
    }

    public MinimumCostRoundResponse runRound(int taskCount) {
        return runRoundInternal(taskCount, "MANUAL");
    }

    public List<MinimumCostRound> getHistory() {
        return roundRepository.findAllByOrderByCreatedAtDesc();
    }

    private MinimumCostRoundResponse runRoundInternal(int taskCount, String mode) {
        validateTaskCount(taskCount);

        int roundNumber = roundRepository.findTopByOrderByRoundNumberDesc()
                .map(round -> round.getRoundNumber() + 1)
                .orElse(1);

        int[][] costMatrix = generateCostMatrix(taskCount);
        MinimumCostRoundResponse.CostMatrixSummary matrixSummary = summarizeCostMatrix(costMatrix);

        long hungarianStart = System.nanoTime();
        AssignmentResult hungarianResult = solveUsingHungarian(copyMatrix(costMatrix));
        long hungarianTimeMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - hungarianStart);

        long flowStart = System.nanoTime();
        AssignmentResult minCostFlowResult = solveUsingMinCostMaxFlow(copyMatrix(costMatrix));
        long minCostFlowTimeMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - flowStart);

        boolean sameMinimumCost = hungarianResult.minimumTotalCost == minCostFlowResult.minimumTotalCost;
        String fasterAlgorithm = determineFasterAlgorithm(hungarianTimeMs, minCostFlowTimeMs);
        String comparisonMessage = buildComparisonMessage(
                sameMinimumCost,
                hungarianTimeMs,
                minCostFlowTimeMs,
                fasterAlgorithm
        );

        List<MinimumCostRoundResponse.AssignmentItem> hungarianAssignments =
                toAssignmentItems(hungarianResult.assignmentByTask, costMatrix);

        List<MinimumCostRoundResponse.AssignmentItem> minCostFlowAssignments =
                toAssignmentItems(minCostFlowResult.assignmentByTask, costMatrix);

        MinimumCostRound savedRound = roundRepository.save(
                new MinimumCostRound(
                        roundNumber,
                        taskCount,
                        hungarianResult.minimumTotalCost,
                        hungarianTimeMs,
                        buildAssignmentSummary(hungarianAssignments),
                        minCostFlowResult.minimumTotalCost,
                        minCostFlowTimeMs,
                        buildAssignmentSummary(minCostFlowAssignments),
                        sameMinimumCost,
                        fasterAlgorithm,
                        comparisonMessage,
                        buildCostMatrixSummaryText(matrixSummary)
                )
        );

        return new MinimumCostRoundResponse(
                savedRound.getId(),
                roundNumber,
                taskCount,
                mode,
                matrixSummary,
                new MinimumCostRoundResponse.AlgorithmResult(
                        HUNGARIAN_NAME,
                        HUNGARIAN_LOGIC,
                        hungarianResult.minimumTotalCost,
                        hungarianTimeMs,
                        hungarianResult.validAssignment,
                        hungarianAssignments
                ),
                new MinimumCostRoundResponse.AlgorithmResult(
                        MIN_COST_FLOW_NAME,
                        MIN_COST_FLOW_LOGIC,
                        minCostFlowResult.minimumTotalCost,
                        minCostFlowTimeMs,
                        minCostFlowResult.validAssignment,
                        minCostFlowAssignments
                ),
                sameMinimumCost,
                fasterAlgorithm,
                comparisonMessage,
                savedRound.getCreatedAt() != null ? savedRound.getCreatedAt() : LocalDateTime.now()
        );
    }

    private void validateTaskCount(int taskCount) {
        if (taskCount < MIN_TASKS || taskCount > MAX_TASKS) {
            throw new IllegalArgumentException("Task count must be between 50 and 100.");
        }
    }

    private int[][] generateCostMatrix(int taskCount) {
        int[][] matrix = new int[taskCount][taskCount];
        ThreadLocalRandom random = ThreadLocalRandom.current();

        for (int i = 0; i < taskCount; i++) {
            for (int j = 0; j < taskCount; j++) {
                matrix[i][j] = random.nextInt(MIN_COST, MAX_COST + 1);
            }
        }

        return matrix;
    }

    private MinimumCostRoundResponse.CostMatrixSummary summarizeCostMatrix(int[][] matrix) {
        int min = Integer.MAX_VALUE;
        int max = Integer.MIN_VALUE;
        long total = 0;
        int count = 0;

        for (int[] row : matrix) {
            for (int value : row) {
                min = Math.min(min, value);
                max = Math.max(max, value);
                total += value;
                count++;
            }
        }

        double average = count == 0 ? 0 : Math.round((total / (double) count) * 100.0) / 100.0;
        return new MinimumCostRoundResponse.CostMatrixSummary(min, max, average);
    }

    private String determineFasterAlgorithm(long hungarianTimeMs, long minCostFlowTimeMs) {
        if (hungarianTimeMs < minCostFlowTimeMs) {
            return HUNGARIAN_NAME;
        }
        if (minCostFlowTimeMs < hungarianTimeMs) {
            return MIN_COST_FLOW_NAME;
        }
        return TIE;
    }

    private String buildComparisonMessage(boolean sameMinimumCost,
                                          long hungarianTimeMs,
                                          long minCostFlowTimeMs,
                                          String fasterAlgorithm) {
        String costText = sameMinimumCost
                ? "Both algorithms produced the same optimal minimum cost."
                : "Warning: the minimum costs were different. Recheck the implementation.";

        if (TIE.equals(fasterAlgorithm)) {
            return costText + " Both algorithms took the same time for this round.";
        }

        return costText + " Faster algorithm: " + fasterAlgorithm
                + " (Hungarian: " + hungarianTimeMs + " ms, Min-Cost Max-Flow: "
                + minCostFlowTimeMs + " ms).";
    }

    private List<MinimumCostRoundResponse.AssignmentItem> toAssignmentItems(int[] assignmentByTask, int[][] costMatrix) {
        List<MinimumCostRoundResponse.AssignmentItem> items = new ArrayList<>();

        for (int taskIndex = 0; taskIndex < assignmentByTask.length; taskIndex++) {
            int employeeIndex = assignmentByTask[taskIndex];
            items.add(new MinimumCostRoundResponse.AssignmentItem(
                    taskIndex + 1,
                    employeeIndex + 1,
                    costMatrix[taskIndex][employeeIndex]
            ));
        }

        return items;
    }

    private String buildAssignmentSummary(List<MinimumCostRoundResponse.AssignmentItem> assignments) {
        return assignments.stream()
                .map(item -> "T" + item.getTaskNumber()
                        + "->E" + item.getEmployeeNumber()
                        + "($" + item.getCost() + ")")
                .collect(Collectors.joining(", "));
    }

    private String buildCostMatrixSummaryText(MinimumCostRoundResponse.CostMatrixSummary summary) {
        return "min=" + summary.getMinimumValue()
                + ", max=" + summary.getMaximumValue()
                + ", avg=" + summary.getAverageValue();
    }

    private int[][] copyMatrix(int[][] matrix) {
        int[][] copy = new int[matrix.length][matrix.length];
        for (int i = 0; i < matrix.length; i++) {
            System.arraycopy(matrix[i], 0, copy[i], 0, matrix[i].length);
        }
        return copy;
    }

    private boolean isValidAssignment(int[] assignment) {
        boolean[] usedEmployees = new boolean[assignment.length];

        for (int employee : assignment) {
            if (employee < 0 || employee >= assignment.length) {
                return false;
            }
            if (usedEmployees[employee]) {
                return false;
            }
            usedEmployees[employee] = true;
        }

        return true;
    }

    private AssignmentResult solveUsingHungarian(int[][] cost) {
        int n = cost.length;
        int[] u = new int[n + 1];
        int[] v = new int[n + 1];
        int[] p = new int[n + 1];
        int[] way = new int[n + 1];

        for (int i = 1; i <= n; i++) {
            p[0] = i;
            int j0 = 0;
            int[] minv = new int[n + 1];
            boolean[] used = new boolean[n + 1];
            Arrays.fill(minv, Integer.MAX_VALUE);

            do {
                used[j0] = true;
                int i0 = p[j0];
                int delta = Integer.MAX_VALUE;
                int j1 = 0;

                for (int j = 1; j <= n; j++) {
                    if (!used[j]) {
                        int cur = cost[i0 - 1][j - 1] - u[i0] - v[j];
                        if (cur < minv[j]) {
                            minv[j] = cur;
                            way[j] = j0;
                        }
                        if (minv[j] < delta) {
                            delta = minv[j];
                            j1 = j;
                        }
                    }
                }

                for (int j = 0; j <= n; j++) {
                    if (used[j]) {
                        u[p[j]] += delta;
                        v[j] -= delta;
                    } else {
                        minv[j] -= delta;
                    }
                }

                j0 = j1;
            } while (p[j0] != 0);

            do {
                int j1 = way[j0];
                p[j0] = p[j1];
                j0 = j1;
            } while (j0 != 0);
        }

        int[] assignment = new int[n];
        for (int j = 1; j <= n; j++) {
            assignment[p[j] - 1] = j - 1;
        }

        long totalCost = 0;
        for (int i = 0; i < n; i++) {
            totalCost += cost[i][assignment[i]];
        }

        return new AssignmentResult(assignment, totalCost, isValidAssignment(assignment));
    }

    private AssignmentResult solveUsingMinCostMaxFlow(int[][] costMatrix) {
        int n = costMatrix.length;
        int source = 0;
        int firstTaskNode = 1;
        int firstEmployeeNode = 1 + n;
        int sink = 1 + (2 * n);
        int nodeCount = sink + 1;

        @SuppressWarnings("unchecked")
        List<Edge>[] graph = new ArrayList[nodeCount];
        for (int i = 0; i < nodeCount; i++) {
            graph[i] = new ArrayList<>();
        }

        for (int task = 0; task < n; task++) {
            addEdge(graph, source, firstTaskNode + task, 1, 0);
        }

        for (int task = 0; task < n; task++) {
            for (int employee = 0; employee < n; employee++) {
                addEdge(graph, firstTaskNode + task, firstEmployeeNode + employee, 1, costMatrix[task][employee]);
            }
        }

        for (int employee = 0; employee < n; employee++) {
            addEdge(graph, firstEmployeeNode + employee, sink, 1, 0);
        }

        long totalCost = 0;
        int flow = 0;
        long[] potential = new long[nodeCount];

        while (flow < n) {
            long[] dist = new long[nodeCount];
            Arrays.fill(dist, Long.MAX_VALUE / 4);
            dist[source] = 0;

            int[] parentNode = new int[nodeCount];
            int[] parentEdgeIndex = new int[nodeCount];
            Arrays.fill(parentNode, -1);

            PriorityQueue<NodeState> pq = new PriorityQueue<>(Comparator.comparingLong(a -> a.distance));
            pq.offer(new NodeState(source, 0));

            while (!pq.isEmpty()) {
                NodeState current = pq.poll();
                if (current.distance != dist[current.node]) {
                    continue;
                }

                for (int edgeIndex = 0; edgeIndex < graph[current.node].size(); edgeIndex++) {
                    Edge edge = graph[current.node].get(edgeIndex);
                    if (edge.capacity <= 0) {
                        continue;
                    }

                    long nextDistance = dist[current.node] + edge.cost + potential[current.node] - potential[edge.to];
                    if (nextDistance < dist[edge.to]) {
                        dist[edge.to] = nextDistance;
                        parentNode[edge.to] = current.node;
                        parentEdgeIndex[edge.to] = edgeIndex;
                        pq.offer(new NodeState(edge.to, nextDistance));
                    }
                }
            }

            if (parentNode[sink] == -1) {
                throw new IllegalStateException("Unable to build a complete assignment.");
            }

            for (int i = 0; i < nodeCount; i++) {
                if (dist[i] < Long.MAX_VALUE / 4) {
                    potential[i] += dist[i];
                }
            }

            int current = sink;
            long pathCost = 0;

            while (current != source) {
                int prev = parentNode[current];
                Edge edge = graph[prev].get(parentEdgeIndex[current]);
                pathCost += edge.cost;
                edge.capacity -= 1;
                graph[current].get(edge.reverseIndex).capacity += 1;
                current = prev;
            }

            totalCost += pathCost;
            flow++;
        }

        int[] assignment = new int[n];
        Arrays.fill(assignment, -1);

        for (int task = 0; task < n; task++) {
            int taskNode = firstTaskNode + task;

            for (Edge edge : graph[taskNode]) {
                if (edge.to >= firstEmployeeNode && edge.to < firstEmployeeNode + n && edge.originalCapacity == 1 && edge.capacity == 0) {
                    assignment[task] = edge.to - firstEmployeeNode;
                    break;
                }
            }
        }

        return new AssignmentResult(assignment, totalCost, isValidAssignment(assignment));
    }

    private void addEdge(List<Edge>[] graph, int from, int to, int capacity, int cost) {
        Edge forward = new Edge(to, graph[to].size(), capacity, cost, capacity);
        Edge backward = new Edge(from, graph[from].size(), 0, -cost, 0);
        graph[from].add(forward);
        graph[to].add(backward);
    }

    private static class AssignmentResult {
        private final int[] assignmentByTask;
        private final long minimumTotalCost;
        private final boolean validAssignment;

        private AssignmentResult(int[] assignmentByTask, long minimumTotalCost, boolean validAssignment) {
            this.assignmentByTask = assignmentByTask;
            this.minimumTotalCost = minimumTotalCost;
            this.validAssignment = validAssignment;
        }
    }

    private static class Edge {
        private final int to;
        private final int reverseIndex;
        private int capacity;
        private final int cost;
        private final int originalCapacity;

        private Edge(int to, int reverseIndex, int capacity, int cost, int originalCapacity) {
            this.to = to;
            this.reverseIndex = reverseIndex;
            this.capacity = capacity;
            this.cost = cost;
            this.originalCapacity = originalCapacity;
        }
    }

    private static class NodeState {
        private final int node;
        private final long distance;

        private NodeState(int node, long distance) {
            this.node = node;
            this.distance = distance;
        }
    }
}