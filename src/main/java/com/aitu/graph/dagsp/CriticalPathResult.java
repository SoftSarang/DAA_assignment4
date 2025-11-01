package com.aitu.graph.dagsp;

import java.util.List;

/**
 * Represents the result of critical path (longest path) computation in a DAG.
 * Stores the path, end vertex, and total length.
 */
public class CriticalPathResult {
    private final PathResult pathResult;
    private final int endVertex;
    private final double length;

    public CriticalPathResult(PathResult pathResult, int endVertex, double length) {
        this.pathResult = pathResult;
        this.endVertex = endVertex;
        this.length = length;
    }

    /**
     * Returns the critical path as a list of vertices (source -> endVertex).
     * Returns null if pathResult is null (no path exists).
     */
    public List<Integer> getCriticalPath() {
        if (pathResult == null) return null;
        return pathResult.getPath(endVertex);
    }

    /**
     * Returns the total length of the critical path.
     */
    public double getLength() {
        return length;
    }
}

