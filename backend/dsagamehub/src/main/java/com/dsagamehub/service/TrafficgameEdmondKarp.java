package com.dsagamehub.service;

import java.util.*;

public class TrafficgameEdmondKarp {

    public static int maxFlow(Map<String, Map<String, Integer>> graph,
                              String source,
                              String sink) {

        Map<String, Map<String, Integer>> residual = buildResidual(graph);
        int maxFlow = 0;

        while (true) {

            Map<String, String> parent = new HashMap<>();

            int flow = bfs(residual, source, sink, parent);
            if (flow == 0) break;

            maxFlow += flow;

            String v = sink;
            while (!v.equals(source)) {
                String u = parent.get(v);

                residual.get(u).put(v,
                        residual.get(u).get(v) - flow);

                residual.get(v).put(u,
                        residual.get(v).getOrDefault(u, 0) + flow);

                v = u;
            }
        }

        return maxFlow;
    }

    private static int bfs(Map<String, Map<String, Integer>> res,
                           String s, String t,
                           Map<String, String> parent) {

        Queue<String> q = new LinkedList<>();
        Map<String, Integer> flow = new HashMap<>();
        Set<String> vis = new HashSet<>();

        q.add(s);
        vis.add(s);
        flow.put(s, Integer.MAX_VALUE);

        while (!q.isEmpty()) {

            String u = q.poll();

            for (var e : res.getOrDefault(u, Map.of()).entrySet()) {

                String v = e.getKey();
                int cap = e.getValue();

                if (!vis.contains(v) && cap > 0) {

                    parent.put(v, u);
                    vis.add(v);

                    flow.put(v, Math.min(flow.get(u), cap));

                    if (v.equals(t)) return flow.get(v);

                    q.add(v);
                }
            }
        }

        return 0;
    }

    private static Map<String, Map<String, Integer>> buildResidual(
            Map<String, Map<String, Integer>> graph) {

        Map<String, Map<String, Integer>> res = new HashMap<>();

        for (String u : graph.keySet()) {
            res.putIfAbsent(u, new HashMap<>());

            for (String v : graph.get(u).keySet()) {
                res.get(u).put(v, graph.get(u).get(v));

                res.putIfAbsent(v, new HashMap<>());
                res.get(v).putIfAbsent(u, 0);
            }
        }

        return res;
    }
}