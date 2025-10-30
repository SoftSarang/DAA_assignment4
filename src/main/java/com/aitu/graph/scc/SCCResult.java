package com.aitu.graph.scc;

import java.util.List;

public class SCCResult {
    private final List<List<Integer>> components;
    private final int[] componentId;

    public SCCResult(List<List<Integer>> components, int[] componentId) {
        this.components = components;
        this.componentId = componentId;
    }

    public List<List<Integer>> getComponents() {
        return components;
    }

    public int[] getComponentId() {
        return componentId;
    }

    public int getNumComponents() {
        return components.size();
    }
}
