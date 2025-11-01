package com.aitu.graph.scc;

import com.aitu.core.DirectedGraph;
import com.aitu.core.Edge;
import com.aitu.utils.Metrics;

import java.util.*;

/**
 * Tarjan's algorithm for finding strongly connected components (SCC) in O(V+E) time.
 * Uses DFS with a stack and low-link values to identify SCCs.
 */
public class TarjanSCC {
    private int[] disc;
    private int[] low;
    private boolean[] onStack;
    private Stack<Integer> stack;
    private int time;
    private List<List<Integer>> components;
    private int[] componentId;
    private Metrics metrics;

    public TarjanSCC() {
        this.metrics = new Metrics("Tarjan-SCC");
    }

    /**
     * Finds all strongly connected components in the graph.
     * Time complexity: O(V + E)
     */
    public SCCResult findSCC(DirectedGraph graph) {
        if (graph == null) {
            throw new IllegalArgumentException("Graph cannot be null");
        }
        int n = graph.getN();
        disc = new int[n];
        low = new int[n];
        onStack = new boolean[n];
        stack = new Stack<>();
        time = 0;
        components = new ArrayList<>();
        componentId = new int[n];
        Arrays.fill(disc, -1);
        Arrays.fill(componentId, -1);

        metrics.reset();
        metrics.startTimer();

        // Visit all unvisited vertices
        for (int v = 0; v < n; v++) {
            if (disc[v] == -1) {
                dfs(v, graph);
            }
        }

        metrics.stopTimer();

        return new SCCResult(components, componentId);
    }

    /**
     * DFS traversal: discovers SCCs using low-link values.
     * When low[u] == disc[u], u is an SCC root.
     */
    private void dfs(int u, DirectedGraph graph) {
        disc[u] = low[u] = time++;
        stack.push(u);
        onStack[u] = true;
        metrics.incrementDFSVisit();
        metrics.incrementStackOperation();

        // Explore adjacent vertices
        for (Edge edge : graph.getAdjacent(u)) {
            int v = edge.getTo();
            metrics.incrementEdgeExploration();

            if (disc[v] == -1) {
                // Forward edge: recurse
                dfs(v, graph);
                low[u] = Math.min(low[u], low[v]);
                metrics.incrementLowLinkUpdate();
            } else if (onStack[v]) {
                // Back edge: update low value (only if v is on stack)
                low[u] = Math.min(low[u], disc[v]);
                metrics.incrementLowLinkUpdate();
            }
        }

        // u is a root of SCC: pop all vertices in this SCC from stack
        if (low[u] == disc[u]) {
            List<Integer> component = new ArrayList<>();
            int componentIdx = components.size();

            int v;
            do {
                v = stack.pop();
                onStack[v] = false;
                component.add(v);
                componentId[v] = componentIdx;
                metrics.incrementStackOperation();
            } while (v != u);

            components.add(component);
        }
    }

    public Metrics getMetrics() {
        return metrics;
    }
}
