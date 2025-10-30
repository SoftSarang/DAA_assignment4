package com.aitu.core;

import java.util.*;

public class DirectedGraph {
    private final int n;
    private final List<List<Edge>> adj;
    private final List<Edge> allEdges;

    public DirectedGraph(int n) {
        this.n = n;
        this.adj = new ArrayList<>(n);
        this.allEdges = new ArrayList<>();

        for (int i = 0; i < n; i++) {
            adj.add(new ArrayList<>());
        }
    }

    public void addEdge(int from, int to, double weight) {
        if (from < 0 || from >= n || to < 0 || to >= n) {
            throw new IllegalArgumentException("Vertex out of bounds");
        }
        Edge edge = new Edge(from, to, weight);
        adj.get(from).add(edge);
        allEdges.add(edge);
    }

    public List<Edge> getAdjacent(int v) {
        return adj.get(v);
    }

    public int getN() {
        return n;
    }

    public List<Edge> getAllEdges() {
        return allEdges;
    }

    public DirectedGraph getReversed() {
        DirectedGraph reversed = new DirectedGraph(n);
        for (Edge e : allEdges) {
            reversed.addEdge(e.getTo(), e.getFrom(), e.getWeight());
        }
        return reversed;
    }

    public int[] getInDegrees() {
        int[] inDegree = new int[n];
        for (Edge e : allEdges) {
            inDegree[e.getTo()]++;
        }
        return inDegree;
    }

}
