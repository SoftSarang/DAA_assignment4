package com.aitu.graph.topo;

import com.aitu.core.DirectedGraph;
import com.aitu.core.Edge;
import com.aitu.utils.Metrics;

import java.util.*;

public class KahnTopologicalSort {
    private Metrics metrics;

    public KahnTopologicalSort() {
        this.metrics = new Metrics("Kahn-TS");
    }

    public TopologicalSortResult sort(DirectedGraph graph) {
        int n = graph.getN();
        int[] inDegree = graph.getInDegrees();
        List<Integer> order = new ArrayList<>();
        Queue<Integer> queue = new LinkedList<>();

        metrics.reset();
        metrics.startTimer();

        for (int v = 0; v < n; v++) {
            if (inDegree[v] == 0) {
                queue.add(v);
                metrics.incrementQueueOperation();
            }
        }

        while (!queue.isEmpty()) {
            int u = queue.poll();
            order.add(u);
            metrics.incrementQueueOperation();

            for (Edge edge : graph.getAdjacent(u)) {
                int v = edge.getTo();
                inDegree[v]--;
                metrics.incrementInDegreeUpdate();

                if (inDegree[v] == 0) {
                    queue.add(v);
                    metrics.incrementQueueOperation();
                }
            }
        }

        metrics.stopTimer();

        boolean isDAG = (order.size() == n);
        return new TopologicalSortResult(order, isDAG);
    }

    public Metrics getMetrics() {
        return metrics;
    }
}

