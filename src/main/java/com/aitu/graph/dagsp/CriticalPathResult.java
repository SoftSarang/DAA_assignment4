package com.aitu.graph.dagsp;

import java.util.List;

public class CriticalPathResult {
    private final PathResult pathResult;
    private final int endVertex;
    private final double length;

    public CriticalPathResult(PathResult pathResult, int endVertex, double length) {
        this.pathResult = pathResult;
        this.endVertex = endVertex;
        this.length = length;
    }

    public List<Integer> getCriticalPath() {
        if (pathResult == null) return null;
        return pathResult.getPath(endVertex);
    }

    public double getLength() {
        return length;
    }

    public int getEndVertex() {
        return endVertex;
    }
}

