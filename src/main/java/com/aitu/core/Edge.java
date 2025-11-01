package com.aitu.core;

/**
 * Represents a directed edge in a graph.
 */
public class Edge {
    private final int from;
    private final int to;
    private final double weight;

    /**
     * Constructs an edge from source to destination with given weight.
     */
    public Edge(int from, int to, double weight) {
        this.from = from;
        this.to = to;
        this.weight = weight;
    }

    public int getFrom() {
        return from;
    }

    public int getTo() {
        return to;
    }

    public double getWeight() {
        return weight;
    }
}

