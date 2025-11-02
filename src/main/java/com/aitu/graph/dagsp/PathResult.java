package com.aitu.graph.dagsp;


import java.util.List;
import com.aitu.core.*;
/**
 * Stores shortest/longest path computation results for a single source.
 * Contains distances, parent pointers, and methods to reconstruct paths.
 */
public class PathResult {
    private final double[] dist;
    private final int[] parent;
    private final int source;

    public PathResult(double[] dist, int[] parent, int source) {
        if (dist == null || parent == null) {
            throw new IllegalArgumentException("Arrays cannot be null");
        }
        if (dist.length != parent.length) {
            throw new IllegalArgumentException("Array sizes must match");
        }
        if (source < 0 || source >= dist.length) {
            throw new IllegalArgumentException("Source out of bounds");
        }
        this.dist = dist;
        this.parent = parent;
        this.source = source;
    }

    /**
     * Returns distance array: dist[v] = shortest/longest distance from source to v.
     */
    public double[] getDistances() {
        return dist;
    }

    /**
     * Returns parent array for path reconstruction.
     * parent[v] = predecessor of v on shortest/longest path (or -1 if source).
     */
    public int[] getParent() {
        return parent;
    }

    /**
     * Returns the source vertex for this computation.
     */
    public int getSource() {
        return source;
    }

    /**
     * Reconstructs the path from source to target vertex.
     * Returns null if target is unreachable.
     * Trace backward from target using parent array, then reverse.
     */
    public List<Integer> getPath(int target) {
        if (target < 0 || target >= dist.length) {
            throw new IllegalArgumentException("Target out of bounds");
        }
        // Check if target is reachable
        if (dist[target] == Double.POSITIVE_INFINITY ||
                dist[target] == Double.NEGATIVE_INFINITY) {
            return null;
        }

        // Trace backward from target to source using parent pointers
        List<Integer> path = new java.util.ArrayList<>();
        for (int v = target; v != -1; v = parent[v]) {
            path.add(v);
        }

        // Reverse to get source -> target order
        java.util.Collections.reverse(path);
        return path;
    }
}
