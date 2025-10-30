package com.aitu.scc;

import com.aitu.core.DirectedGraph;
import com.aitu.core.Edge;

import java.util.*;

public class CondensationGraph {
    private final DirectedGraph original;
    private final SCCResult sccResult;
    private DirectedGraph condensation;

    public CondensationGraph(DirectedGraph original, SCCResult sccResult) {
        this.original = original;
        this.sccResult = sccResult;
        buildCondensation();
    }

    private void buildCondensation() {
        int numComponents = sccResult.getNumComponents();
        condensation = new DirectedGraph(numComponents);

        Set<String> addedEdges = new HashSet<>();
        int[] componentId = sccResult.getComponentId();

        for (Edge edge : original.getAllEdges()) {
            int fromComp = componentId[edge.getFrom()];
            int toComp = componentId[edge.getTo()];

            if (fromComp != toComp) {
                String edgeKey = fromComp + "->" + toComp;
                if (!addedEdges.contains(edgeKey)) {
                    condensation.addEdge(fromComp, toComp, edge.getWeight());
                    addedEdges.add(edgeKey);
                }
            }
        }
    }

    public DirectedGraph getCondensation() {
        return condensation;
    }

    public boolean isDAG() {
        return true;
    }
}
