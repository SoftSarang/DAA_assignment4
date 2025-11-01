package com.aitu.core;

import java.util.*;

/**
 * Represents a weighted directed graph using adjacency list representation.
 */
public class DirectedGraph {
    private final int n;
    private final List<List<Edge>> adj; // Adjacency list
    private final List<Edge> allEdges; // All edges in graph

    /**
     * Constructs a directed graph with n vertices.
     */
    public DirectedGraph(int n) {
        this.n = n;
        this.adj = new ArrayList<>(n);
        this.allEdges = new ArrayList<>();

        // Initialize adjacency list for each vertex
        for (int i = 0; i < n; i++) {
            adj.add(new ArrayList<>());
        }
    }

    /**
     * Adds a weighted edge from source to destination.
     * @throws IllegalArgumentException if vertex is out of bounds
     */
    public void addEdge(int from, int to, double weight) {
        if (from < 0 || from >= n || to < 0 || to >= n) {
            throw new IllegalArgumentException("Vertex out of bounds");
        }
        Edge edge = new Edge(from, to, weight);
        adj.get(from).add(edge);
        allEdges.add(edge);
    }

    /**
     * Returns the adjacency list for vertex v.
     */
    public List<Edge> getAdjacent(int v) {
        return new ArrayList<>(adj.get(v));
    }

    /**
     * Returns the number of vertices.
     */
    public int getN() {
        return n;
    }

    /**
     * Returns all edges in the graph.
     */
    public List<Edge> getAllEdges() {
        return new ArrayList<>(allEdges);
    }

    /**
     * Returns in-degrees for all vertices (used in Kahn's algorithm).
     */
    public int[] getInDegrees() {
        int[] inDegree = new int[n];
        for (Edge e : allEdges) {
            inDegree[e.getTo()]++;
        }
        return inDegree;
    }

}
