package com.dsagamehub.service;

//import java.util.HashMap;
//import java.util.HashSet;
//import java.util.Map;
//import java.util.Set;

import java.util.*;

public class TrafficgameFordFulkerson {

        public static int maxFlow(Map<String, Map<String, Integer>> graph,
                                  String source,
                                  String sink) {

            // Build residual graph (deep copy)
            Map<String, Map<String, Integer>> residual = buildResidualGraph(graph);

            int maxFlow = 0;

            while (true) {

                Set<String> visited = new HashSet<>();

                int flow = dfs(residual, source, sink,
                        Integer.MAX_VALUE, visited);

                // no more augmenting paths
                if (flow == 0) break;

                maxFlow += flow;
            }

            return maxFlow;
        }

        private static int dfs(Map<String, Map<String, Integer>> residual,
                               String u,
                               String sink,
                               int flow,
                               Set<String> visited) {

            // reached sink → return bottleneck flow
            if (u.equals(sink)) return flow;

            visited.add(u);

            Map<String, Integer> neighbors =
                    residual.getOrDefault(u, new HashMap<>());

            for (Map.Entry<String, Integer> entry : neighbors.entrySet()) {

                String v = entry.getKey();
                int capacity = entry.getValue();

                // skip visited or zero capacity
                if (visited.contains(v) || capacity <= 0) continue;

                int minFlow = dfs(residual, v, sink,
                        Math.min(flow, capacity), visited);

                if (minFlow > 0) {

                    // reduce forward edge
                    residual.get(u).put(v, capacity - minFlow);

                    // add reverse edge
                    residual.putIfAbsent(v, new HashMap<>());
                    residual.get(v).put(u,
                            residual.get(v).getOrDefault(u, 0) + minFlow);

                    return minFlow;
                }
            }

            return 0;
        }

        private static Map<String, Map<String, Integer>> buildResidualGraph(
                Map<String, Map<String, Integer>> graph) {

            Map<String, Map<String, Integer>> residual = new HashMap<>();

            for (String u : graph.keySet()) {
                residual.put(u, new HashMap<>(graph.get(u)));
            }

            // ensure reverse edges exist
            for (String u : graph.keySet()) {
                for (String v : graph.get(u).keySet()) {

                    residual.putIfAbsent(v, new HashMap<>());
                    residual.get(v).putIfAbsent(u, 0);
                }
            }

            return residual;
        }
    }
