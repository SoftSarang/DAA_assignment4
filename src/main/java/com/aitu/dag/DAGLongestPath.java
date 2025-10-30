package com.aitu.dag;

import com.aitu.core.DirectedGraph;
import com.aitu.core.Edge;
import com.aitu.ts.KahnTopologicalSort;
import com.aitu.ts.TopologicalSortResult;
import com.aitu.utils.Metrics;

import java.util.Arrays;
import java.util.List;

public class DAGLongestPath {
    private Metrics metrics;

    public DAGLongestPath() {
        this.metrics = new Metrics("DAG-LongestPath");
    }

    public PathResult computeLongestPaths(DirectedGraph graph, int source) {
        int n = graph.getN();
        double[] dist = new double[n];
        int[] parent = new int[n];
        Arrays.fill(dist, Double.NEGATIVE_INFINITY);
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
            if (dist[u] != Double.NEGATIVE_INFINITY) {
                for (Edge edge : graph.getAdjacent(u)) {
                    int v = edge.getTo();
                    double newDist = dist[u] + edge.getWeight();
                    metrics.incrementRelaxation();
                    metrics.incrementComparison();

                    if (newDist > dist[v]) {
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

    public CriticalPathResult findCriticalPath(DirectedGraph graph) {
        int n = graph.getN();
        double maxLength = Double.NEGATIVE_INFINITY;
        int endVertex = -1;
        PathResult bestResult = null;

        for (int source = 0; source < n; source++) {
            PathResult result = computeLongestPaths(graph, source);
            if (result != null) {
                for (int v = 0; v < n; v++) {
                    if (result.getDistances()[v] > maxLength &&
                            result.getDistances()[v] != Double.NEGATIVE_INFINITY) {
                        maxLength = result.getDistances()[v];
                        endVertex = v;
                        bestResult = result;
                    }
                }
            }
        }

        return new CriticalPathResult(bestResult, endVertex, maxLength);
    }

    public Metrics getMetrics() {
        return metrics;
    }
}

