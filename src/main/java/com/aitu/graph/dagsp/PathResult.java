package com.aitu.graph.dagsp;


import java.util.List;

public class PathResult {
    private final double[] dist;
    private final int[] parent;
    private final int source;

    public PathResult(double[] dist, int[] parent, int source) {
        this.dist = dist;
        this.parent = parent;
        this.source = source;
    }

    public double[] getDistances() {
        return dist;
    }

    public int[] getParent() {
        return parent;
    }

    public int getSource() {
        return source;
    }

    public List<Integer> getPath(int target) {
        if (dist[target] == Double.POSITIVE_INFINITY ||
                dist[target] == Double.NEGATIVE_INFINITY) {
            return null;
        }

        List<Integer> path = new java.util.ArrayList<>();
        for (int v = target; v != -1; v = parent[v]) {
            path.add(v);
        }
        java.util.Collections.reverse(path);
        return path;
    }
}
