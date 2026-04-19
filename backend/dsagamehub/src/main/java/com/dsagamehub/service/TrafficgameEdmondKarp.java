package com.dsagamehub.service;

import java.util.*;

public class TrafficgameEdmondKarp {

    public static int maxFlow(Map<String, Map<String, Integer>> graph, String source, String sink) {

        Map<String, Map<String, Integer>> residual = new HashMap<>();
        for (String u : graph.keySet()) {
            residual.computeIfAbsent(u, k -> new HashMap<>());
            for (Map.Entry<String, Integer> entry : graph.get(u).entrySet()) {
                String v = entry.getKey();
                int cap = entry.getValue();
                residual.get(u).put(v, residual.get(u).getOrDefault(v, 0) + cap);
                residual.computeIfAbsent(v, k -> new HashMap<>());
                residual.get(v).putIfAbsent(u, 0);
            }
        }

        int totalFlow = 0;

        while (true) {

            Map<String, String> parent = new HashMap<>();
            int augmented = bfs(residual, source, sink, parent);
            if (augmented == 0) break;
            totalFlow += augmented;


            String v = sink;
            while (!v.equals(source)) {
                String u = parent.get(v);
                int cap = residual.get(u).get(v);
                residual.get(u).put(v, cap - augmented);
                residual.computeIfAbsent(v, k -> new HashMap<>());
                residual.get(v).put(u, residual.get(v).getOrDefault(u, 0) + augmented);
                v = u;
            }
        }

        return totalFlow;
    }

    private static int bfs(Map<String, Map<String, Integer>> residual,
                           String source, String sink,
                           Map<String, String> parent) {
        Set<String> visited = new HashSet<>();
        Queue<String> queue = new LinkedList<>();
        Map<String, Integer> flowTo = new HashMap<>();

        queue.add(source);
        visited.add(source);
        flowTo.put(source, Integer.MAX_VALUE);

        while (!queue.isEmpty()) {
            String u = queue.poll();

            for (Map.Entry<String, Integer> entry : residual.getOrDefault(u, new HashMap<>()).entrySet()) {
                String v = entry.getKey();
                int cap = entry.getValue();

                if (!visited.contains(v) && cap > 0) {
                    visited.add(v);
                    parent.put(v, u);
                    int newFlow = Math.min(flowTo.get(u), cap);
                    flowTo.put(v, newFlow);

                    if (v.equals(sink)) {
                        return newFlow;
                    }

                    queue.add(v);
                }
            }
        }

        return 0;
    }
}