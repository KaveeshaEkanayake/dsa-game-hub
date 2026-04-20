package com.dsagamehub.service;

import com.dsagamehub.model.TrafficEdge;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class TrafficEdmondsKarpService {

    private Map<String, Map<String, Integer>> residualGraph;
    private final List<String> NODES = Arrays.asList("A", "B", "C", "D", "E", "F", "G", "H", "T");
    private final String SOURCE = "A";
    private final String SINK = "T";

    public int computeMaxFlow(List<TrafficEdge> edges) {
        buildResidualGraph(edges);
        int maxFlow = 0;
        List<String> path;
        while ((path = bfsPath()) != null) {
            int pathFlow = getPathFlow(path);
            updateResidualGraph(path, pathFlow);
            maxFlow += pathFlow;
        }
        return maxFlow;
    }

    public long measureTime(List<TrafficEdge> edges) {
        long start = System.currentTimeMillis();
        computeMaxFlow(edges);
        return System.currentTimeMillis() - start;
    }

    private void buildResidualGraph(List<TrafficEdge> edges) {
        residualGraph = new HashMap<>();
        for (String node : NODES) {
            residualGraph.put(node, new HashMap<>());
        }
        for (TrafficEdge edge : edges) {
            residualGraph.get(edge.getSource()).put(edge.getDestination(), edge.getCapacity());
            residualGraph.get(edge.getDestination()).putIfAbsent(edge.getSource(), 0);
        }
    }

    private List<String> bfsPath() {
        Set<String> visited = new HashSet<>();
        Map<String, String> parent = new HashMap<>();
        Queue<String> queue = new LinkedList<>();
        queue.add(SOURCE);
        visited.add(SOURCE);
        while (!queue.isEmpty()) {
            String current = queue.poll();
            if (current.equals(SINK)) return buildPath(parent);
            for (Map.Entry<String, Integer> neighbor : residualGraph.get(current).entrySet()) {
                if (!visited.contains(neighbor.getKey()) && neighbor.getValue() > 0) {
                    visited.add(neighbor.getKey());
                    parent.put(neighbor.getKey(), current);
                    queue.add(neighbor.getKey());
                }
            }
        }
        return null;
    }

    private List<String> buildPath(Map<String, String> parent) {
        List<String> path = new ArrayList<>();
        String current = SINK;
        while (current != null) {
            path.add(0, current);
            current = parent.get(current);
        }
        return path;
    }

    private int getPathFlow(List<String> path) {
        int flow = Integer.MAX_VALUE;
        for (int i = 0; i < path.size() - 1; i++) {
            flow = Math.min(flow, residualGraph.get(path.get(i)).get(path.get(i + 1)));
        }
        return flow;
    }

    private void updateResidualGraph(List<String> path, int pathFlow) {
        for (int i = 0; i < path.size() - 1; i++) {
            String u = path.get(i);
            String v = path.get(i + 1);
            residualGraph.get(u).put(v, residualGraph.get(u).get(v) - pathFlow);
            residualGraph.get(v).put(u, residualGraph.get(v).get(u) + pathFlow);
        }
    }
}