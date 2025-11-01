package com.aitu;

import com.aitu.core.DirectedGraph;
import com.aitu.graph.scc.TarjanSCC;
import com.aitu.graph.scc.SCCResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;


public class TarjanSCCTest {

    private DirectedGraph graphWithCycle;
    private DirectedGraph pureDAG;
    private DirectedGraph singleCycleGraph;

    @BeforeEach
    public void setUp() {
        graphWithCycle = new DirectedGraph(5);
        graphWithCycle.addEdge(0, 1, 1.0);
        graphWithCycle.addEdge(1, 2, 1.0);
        graphWithCycle.addEdge(2, 0, 1.0);
        graphWithCycle.addEdge(2, 3, 1.0);
        graphWithCycle.addEdge(3, 4, 1.0);

        pureDAG = new DirectedGraph(5);
        pureDAG.addEdge(0, 1, 1.0);
        pureDAG.addEdge(1, 2, 2.0);
        pureDAG.addEdge(2, 3, 3.0);
        pureDAG.addEdge(3, 4, 1.0);
        pureDAG.addEdge(0, 2, 4.0);

        singleCycleGraph = new DirectedGraph(4);
        singleCycleGraph.addEdge(0, 1, 1.0);
        singleCycleGraph.addEdge(1, 2, 1.0);
        singleCycleGraph.addEdge(2, 3, 1.0);
        singleCycleGraph.addEdge(3, 0, 1.0);
    }

    @Test
    public void testTarjanSCC_FindCorrectSCCs() {
        TarjanSCC tarjan = new TarjanSCC();

        SCCResult result1 = tarjan.findSCC(graphWithCycle);
        assertEquals(3, result1.getNumComponents(), "Graph with cycle should have 3 SCCs");
        assertNotNull(result1.getComponents(), "Components should not be null");

        SCCResult result2 = tarjan.findSCC(pureDAG);
        assertEquals(5, result2.getNumComponents(), "Pure DAG should have 5 SCCs");

        SCCResult result3 = tarjan.findSCC(singleCycleGraph);
        assertEquals(1, result3.getNumComponents(), "Single cycle should be 1 SCC");
    }

    @Test
    public void testTarjanSCC_ComponentMappingAndValidation() {
        TarjanSCC tarjan = new TarjanSCC();
        SCCResult result = tarjan.findSCC(graphWithCycle);

        int[] componentId = result.getComponentId();
        assertNotNull(componentId, "Component ID mapping should not be null");
        assertEquals(5, componentId.length, "Component ID should map all vertices");

        assertEquals(componentId[0], componentId[1], "0 and 1 should be in same SCC");
        assertEquals(componentId[1], componentId[2], "1 and 2 should be in same SCC");

        assertNotEquals(componentId[2], componentId[3], "2 and 3 should be in different SCC");
    }

    @Test
    public void testTarjanSCC_MetricsAndEdgeCases() {
        TarjanSCC tarjan = new TarjanSCC();

        // Normal graph metrics
        tarjan.findSCC(graphWithCycle);
        assertNotNull(tarjan.getMetrics(), "Metrics should not be null");
        assertTrue(tarjan.getMetrics().getTotalOperations() > 0, "Should count operations");
        assertTrue(tarjan.getMetrics().getExecutionTimeMs() >= 0, "Execution time should be non-negative");

        // Empty graph
        DirectedGraph empty = new DirectedGraph(0);
        SCCResult emptyResult = tarjan.findSCC(empty);
        assertEquals(0, emptyResult.getNumComponents(), "Empty graph should have 0 SCCs");

        // Single vertex
        DirectedGraph single = new DirectedGraph(1);
        SCCResult singleResult = tarjan.findSCC(single);
        assertEquals(1, singleResult.getNumComponents(), "Single vertex should be 1 SCC");
    }
}
