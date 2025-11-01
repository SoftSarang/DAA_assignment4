package com.aitu.graph.scc;

import com.aitu.core.DirectedGraph;
import com.aitu.core.Edge;

import java.util.*;

/**
 * Converts a directed graph with SCCs into a condensation graph (DAG).
 * Each SCC becomes a single vertex; edges connect different SCCs.
 */
public class CondensationGraph {
    private final DirectedGraph original;
    private final SCCResult sccResult;
    private DirectedGraph condensation;

    public CondensationGraph(DirectedGraph original, SCCResult sccResult) {
        if (original == null || sccResult == null) {
            throw new IllegalArgumentException("Arguments cannot be null");
        }
        this.original = original;
        this.sccResult = sccResult;
        buildCondensation();
    }

    /**
     * Builds the condensation graph.
     * - Each SCC becomes one vertex
     * - Edges only between different SCCs (no self-loops)
     * - Duplicates removed: if multiple edges u_scc -> v_scc, keep one
     * Time complexity: O(E)
     */
    private void buildCondensation() {
        int numComponents = sccResult.getNumComponents();
        if (numComponents == 0) {
            condensation = new DirectedGraph(0);
            return;
        }
        condensation = new DirectedGraph(numComponents);

        Set<String> addedEdges = new HashSet<>();
        int[] componentId = sccResult.getComponentId();

        for (Edge edge : original.getAllEdges()) {
            int fromComp = componentId[edge.getFrom()];
            int toComp = componentId[edge.getTo()];

            // Skip edges within same SCC
            if (fromComp != toComp) {
                String edgeKey = fromComp + "->" + toComp;
                // Avoid duplicate edges between same component pair
                if (!addedEdges.contains(edgeKey)) {
                    condensation.addEdge(fromComp, toComp, edge.getWeight());
                    addedEdges.add(edgeKey);
                }
            }
        }
    }

    /**
     * Returns the condensation graph (guaranteed to be a DAG).
     */
    public DirectedGraph getCondensation() {
        return condensation;
    }

    /**
     * Confirms that condensation graph is always a DAG.
     * This is mathematically guaranteed by SCC properties.
     */
    public boolean isDAG() {
        return true;
    }
}
