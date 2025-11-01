package com.aitu.graph.topo;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents the result of topological sorting on a directed graph.
 * Contains the topological order (if DAG) and a flag indicating whether graph is acyclic.
 */
public class TopologicalSortResult {
    private final List<Integer> order;
    private final boolean isDAG;

    public TopologicalSortResult(List<Integer> order, boolean isDAG) {
        if (order == null) {
            throw new IllegalArgumentException("Order list cannot be null");
        }
        this.order = order;
        this.isDAG = isDAG;
    }

    /**
     * Returns the topological order of vertices.
     */
    public List<Integer> getOrder() {
        return new ArrayList<>(order);
    }

    /**
     * Checks if graph is a DAG (acyclic).
     */
    public boolean isDAG() {
        return isDAG;
    }
}

