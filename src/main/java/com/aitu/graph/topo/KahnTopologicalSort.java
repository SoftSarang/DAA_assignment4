package com.aitu.graph.topo;

import com.aitu.core.DirectedGraph;
import com.aitu.core.Edge;
import com.aitu.utils.Metrics;

import java.util.*;

/**
 * Kahn's algorithm for topological sorting in O(V+E) time.
 * Detects cycles by checking if all vertices are sorted.
 */
public class KahnTopologicalSort {
    private Metrics metrics;

    public KahnTopologicalSort() {
        this.metrics = new Metrics("Kahn-TS");
    }

    /**
     * Performs topological sort using Kahn's algorithm with in-degree approach.
     * Detects cycles: if sorted size < vertices, graph contains cycle.
     * Time complexity: O(V + E)
     */
    public TopologicalSortResult sort(DirectedGraph graph) {
        if (graph == null) {
            throw new IllegalArgumentException("Graph cannot be null");
        }
        int n = graph.getN();
        if (n == 0) {
            metrics.stopTimer();
            return new TopologicalSortResult(new ArrayList<>(), true);
        }
        int[] inDegree = graph.getInDegrees();
        List<Integer> order = new ArrayList<>();
        Queue<Integer> queue = new LinkedList<>();

        metrics.reset();
        metrics.startTimer();

        // Add all vertices with in-degree 0 to queue
        for (int v = 0; v < n; v++) {
            if (inDegree[v] == 0) {
                queue.add(v);
                metrics.incrementQueueOperation();
            }
        }

        // Process vertices with no dependencies
        while (!queue.isEmpty()) {
            int u = queue.poll();
            order.add(u);
            metrics.incrementQueueOperation();

            // Reduce in-degree for neighbors
            for (Edge edge : graph.getAdjacent(u)) {
                int v = edge.getTo();
                inDegree[v]--;
                metrics.incrementInDegreeUpdate();

                // Add v to queue when in-degree becomes 0
                if (inDegree[v] == 0) {
                    queue.add(v);
                    metrics.incrementQueueOperation();
                }
            }
        }

        metrics.stopTimer();

        // If all vertices are sorted, graph is a DAG
        boolean isDAG = (order.size() == n);
        return new TopologicalSortResult(order, isDAG);
    }

    public Metrics getMetrics() {
        return metrics;
    }
}

