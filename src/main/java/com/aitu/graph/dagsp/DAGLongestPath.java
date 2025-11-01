package com.aitu.graph.dagsp;

import com.aitu.core.DirectedGraph;
import com.aitu.core.Edge;
import com.aitu.graph.topo.KahnTopologicalSort;
import com.aitu.graph.topo.TopologicalSortResult;
import com.aitu.utils.Metrics;

import java.util.Arrays;
import java.util.List;

/**
 * Computes longest paths (critical path) in a DAG using topological sort + relaxation.
 * Uses negative initialization (NEGATIVE_INFINITY) for longest path computation.
 */
public class DAGLongestPath {
    private Metrics metrics;

    public DAGLongestPath() {
        this.metrics = new Metrics("DAG-LongestPath");
    }

    /**
     * Computes longest paths from source to all vertices.
     * Similar to shortest path but uses max comparison instead of min.
     * Returns null if graph is not a DAG.
     * Time complexity: O(V + E)
     */
    public PathResult computeLongestPaths(DirectedGraph graph, int source) {
        if (graph == null) {
            throw new IllegalArgumentException("Graph cannot be null");
        }
        if (source < 0 || source >= graph.getN()) {
            throw new IllegalArgumentException("Source out of bounds");
        }
        int n = graph.getN();
        double[] dist = new double[n];
        int[] parent = new int[n];
        Arrays.fill(dist, Double.NEGATIVE_INFINITY);
        Arrays.fill(parent, -1);
        dist[source] = 0;

        // Verify graph is DAG
        KahnTopologicalSort TS = new KahnTopologicalSort();
        TopologicalSortResult TSResult = TS.sort(graph);
        if (!TSResult.isDAG()) {
            metrics.stopTimer();
            return null;
        }

        // Relax edges in topological order (maximize instead of minimize)
        List<Integer> order = TSResult.getOrder();
        for (int u : order) {
            // Only relax from vertices reachable from source
            if (dist[u] != Double.NEGATIVE_INFINITY) {
                for (Edge edge : graph.getAdjacent(u)) {
                    int v = edge.getTo();
                    double newDist = dist[u] + edge.getWeight();
                    metrics.incrementRelaxation();
                    metrics.incrementComparison();

                    // Update if found longer path
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

    /**
     * Finds the critical path (longest path among all source-sink pairs).
     * Tries all vertices as sources and tracks the maximum distance found.
     * Time complexity: O(V * (V + E))
     */
    public CriticalPathResult findCriticalPath(DirectedGraph graph) {
        int n = graph.getN();
        double maxLength = Double.NEGATIVE_INFINITY;
        int endVertex = -1;
        PathResult bestResult = null;

        metrics.reset();
        metrics.startTimer();

        // Try each vertex as a source and find longest path
        for (int source = 0; source < n; source++) {
            PathResult result = computeLongestPaths(graph, source);
            if (result != null) {
                // Track the maximum distance found
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

        metrics.stopTimer();

        return new CriticalPathResult(bestResult, endVertex, maxLength);
    }

    public Metrics getMetrics() {
        return metrics;
    }
}

