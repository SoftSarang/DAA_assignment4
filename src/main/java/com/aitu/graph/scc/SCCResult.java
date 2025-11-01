package com.aitu.graph.scc;

import java.util.List;

/**
 * Stores strongly connected components (SCC) result for a directed graph.
 */
public class SCCResult {
    private final List<List<Integer>> components;
    private final int[] componentId;

    public SCCResult(List<List<Integer>> components, int[] componentId) {
        this.components = components;
        this.componentId = componentId;
    }

    /**
     * Returns all SCCs as a list of vertex lists.
     */
    public List<List<Integer>> getComponents() {
        return components;
    }

    /**
     * Returns the component ID for each vertex.
     */
    public int[] getComponentId() {
        return componentId;
    }

    /**
     * Returns the total number of SCCs found.
     */
    public int getNumComponents() {
        return components.size();
    }
}
