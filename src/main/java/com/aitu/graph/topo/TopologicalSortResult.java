package com.aitu.graph.topo;

import java.util.List;


public class TopologicalSortResult {
    private final List<Integer> order;
    private final boolean isDAG;

    public TopologicalSortResult(List<Integer> order, boolean isDAG) {
        this.order = order;
        this.isDAG = isDAG;
    }

    public List<Integer> getOrder() {
        return order;
    }

    public boolean isDAG() {
        return isDAG;
    }
}

