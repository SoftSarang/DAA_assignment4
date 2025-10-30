package com.aitu;

import com.aitu.core.DirectedGraph;
import com.aitu.core.Edge;
import com.aitu.graph.topo.KahnTopologicalSort;
import com.aitu.graph.topo.TopologicalSortResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class TopologicalSortTest {

    private DirectedGraph dag;
    private DirectedGraph graphWithCycle;
    private DirectedGraph disconnectedDAG;

    @BeforeEach
    public void setUp() {
        dag = new DirectedGraph(5);
        dag.addEdge(0, 1, 1.0);
        dag.addEdge(1, 2, 2.0);
        dag.addEdge(2, 3, 3.0);
        dag.addEdge(3, 4, 1.0);
        dag.addEdge(0, 2, 4.0);

        graphWithCycle = new DirectedGraph(4);
        graphWithCycle.addEdge(0, 1, 1.0);
        graphWithCycle.addEdge(1, 2, 1.0);
        graphWithCycle.addEdge(2, 0, 1.0);
        graphWithCycle.addEdge(2, 3, 1.0);

        disconnectedDAG = new DirectedGraph(4);
        disconnectedDAG.addEdge(0, 1, 1.0);
        disconnectedDAG.addEdge(2, 3, 1.0);
    }

    @Test
    public void testTopologicalSort_RecognizeGraphType() {
        KahnTopologicalSort ts = new KahnTopologicalSort();

        TopologicalSortResult dagResult = ts.sort(dag);
        assertTrue(dagResult.isDAG(), "Should recognize as DAG");

        TopologicalSortResult cycleResult = ts.sort(graphWithCycle);
        assertFalse(cycleResult.isDAG(), "Should recognize cycle");
        assertTrue(cycleResult.getOrder().size() <= graphWithCycle.getN(),
                "Order size should be <= vertices when there's a cycle");
    }

    @Test
    public void testTopologicalSort_OrderValidity() {
        KahnTopologicalSort ts = new KahnTopologicalSort();
        TopologicalSortResult result = ts.sort(dag);

        List<Integer> order = result.getOrder();
        assertEquals(5, order.size(), "Should sort all 5 vertices");

        assertEquals(order.size(), order.stream().distinct().count(),
                "Should have no duplicate vertices");

        int[] position = new int[5];
        for (int i = 0; i < order.size(); i++) {
            position[order.get(i)] = i;
        }

        for (Edge e : dag.getAllEdges()) {
            assertTrue(position[e.getFrom()] < position[e.getTo()],
                    "Topological order violated for edge " + e.getFrom() + "->" + e.getTo());
        }
    }

    @Test
    public void testTopologicalSort_SpecialCasesAndMetrics() {
        KahnTopologicalSort ts = new KahnTopologicalSort();

        TopologicalSortResult disconnectedResult = ts.sort(disconnectedDAG);
        assertTrue(disconnectedResult.isDAG(), "Disconnected DAG should be recognized");
        assertEquals(4, disconnectedResult.getOrder().size(), "Should include all vertices");

        ts.sort(dag);
        assertNotNull(ts.getMetrics(), "Metrics should not be null");
        assertTrue(ts.getMetrics().getTotalOperations() > 0, "Should count operations");
    }
}
