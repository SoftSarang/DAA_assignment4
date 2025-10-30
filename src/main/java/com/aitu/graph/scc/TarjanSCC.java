package com.aitu.graph.scc;

import com.aitu.core.DirectedGraph;
import com.aitu.core.Edge;
import com.aitu.utils.Metrics;

import java.util.*;

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

    public SCCResult findSCC(DirectedGraph graph) {
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

        for (int v = 0; v < n; v++) {
            if (disc[v] == -1) {
                dfs(v, graph);
            }
        }

        metrics.stopTimer();

        return new SCCResult(components, componentId);
    }

    private void dfs(int u, DirectedGraph graph) {
        disc[u] = low[u] = time++;
        stack.push(u);
        onStack[u] = true;
        metrics.incrementDFSVisit();
        metrics.incrementStackOperation();

        for (Edge edge : graph.getAdjacent(u)) {
            int v = edge.getTo();
            metrics.incrementEdgeExploration();

            if (disc[v] == -1) {
                dfs(v, graph);
                low[u] = Math.min(low[u], low[v]);
                metrics.incrementLowLinkUpdate();
            } else if (onStack[v]) {
                low[u] = Math.min(low[u], disc[v]);
                metrics.incrementLowLinkUpdate();
            }
        }

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

            Collections.sort(component);
            components.add(component);
        }
    }

    public Metrics getMetrics() {
        return metrics;
    }
}
