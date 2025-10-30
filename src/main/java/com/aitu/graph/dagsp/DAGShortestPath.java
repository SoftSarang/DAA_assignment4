package com.aitu.graph.dagsp;

import com.aitu.core.DirectedGraph;
import com.aitu.core.Edge;
import com.aitu.graph.topo.KahnTopologicalSort;
import com.aitu.graph.topo.TopologicalSortResult;
import com.aitu.utils.Metrics;

import java.util.Arrays;
import java.util.List;

public class DAGShortestPath {
    private Metrics metrics;

    public DAGShortestPath() {
        this.metrics = new Metrics("DAG-ShortestPath");
    }

    public PathResult computeShortestPaths(DirectedGraph graph, int source) {
        int n = graph.getN();
        double[] dist = new double[n];
        int[] parent = new int[n];
        Arrays.fill(dist, Double.POSITIVE_INFINITY);
        Arrays.fill(parent, -1);
        dist[source] = 0;

        metrics.reset();
        metrics.startTimer();

        KahnTopologicalSort TS = new KahnTopologicalSort();
        TopologicalSortResult TSResult = TS.sort(graph);

        if (!TSResult.isDAG()) {
            metrics.stopTimer();
            return null;
        }

        List<Integer> order = TSResult.getOrder();

        for (int u : order) {
            if (dist[u] != Double.POSITIVE_INFINITY) {
                for (Edge edge : graph.getAdjacent(u)) {
                    int v = edge.getTo();
                    double newDist = dist[u] + edge.getWeight();
                    metrics.incrementRelaxation();
                    metrics.incrementComparison();

                    if (newDist < dist[v]) {
                        dist[v] = newDist;
                        parent[v] = u;
                        metrics.incrementDistanceUpdate();
                    }
                }
            }
        }

        metrics.stopTimer();
        return new PathResult(dist, parent, source);
    }

    public Metrics getMetrics() {
        return metrics;
    }
}

