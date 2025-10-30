package com.aitu;

import com.aitu.core.DirectedGraph;
import com.aitu.graph.scc.*;
import com.aitu.graph.topo.*;
import com.aitu.graph.dagsp.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class IntegrationTest {

    private DirectedGraph graphWithCycle;
    private DirectedGraph testGraph;

    @BeforeEach
    public void setUp() {
        graphWithCycle = new DirectedGraph(5);
        graphWithCycle.addEdge(0, 1, 1.0);
        graphWithCycle.addEdge(1, 2, 1.0);
        graphWithCycle.addEdge(2, 0, 1.0);
        graphWithCycle.addEdge(2, 3, 2.0);
        graphWithCycle.addEdge(3, 4, 3.0);

        testGraph = new DirectedGraph(5);
        testGraph.addEdge(0, 1, 1.0);
        testGraph.addEdge(1, 2, 2.0);
        testGraph.addEdge(2, 0, 3.0);
        testGraph.addEdge(2, 3, 4.0);
        testGraph.addEdge(3, 4, 5.0);
    }

    @Test
    public void testFullPipeline_FunctionalityAndCorrectness() {
        TarjanSCC tarjan = new TarjanSCC();
        SCCResult sccResult = tarjan.findSCC(graphWithCycle);
        assertTrue(sccResult.getNumComponents() > 0, "Should find SCCs");

        CondensationGraph cg = new CondensationGraph(graphWithCycle, sccResult);
        DirectedGraph dag = cg.getCondensation();
        assertNotNull(dag, "DAG should be created");
        assertEquals(3, dag.getN(), "DAG should have 3 vertices");

        KahnTopologicalSort ts = new KahnTopologicalSort();
        TopologicalSortResult topoResult = ts.sort(dag);
        assertTrue(topoResult.isDAG(), "Condensation should be a DAG");
        assertEquals(3, topoResult.getOrder().size(), "Should sort 3 vertices");

        DAGShortestPath sp = new DAGShortestPath();
        int dagSource = sccResult.getComponentId()[0];
        PathResult pathResult = sp.computeShortestPaths(dag, dagSource);
        assertNotNull(pathResult, "Path should be computed");

        DAGLongestPath lp = new DAGLongestPath();
        CriticalPathResult cpResult = lp.findCriticalPath(dag);
        assertNotNull(cpResult, "Critical path should be found");
        assertTrue(cpResult.getLength() >= 0, "Critical path length should be non-negative");

        assertDoesNotThrow(() -> {
            TarjanSCC t = new TarjanSCC();
            SCCResult s = t.findSCC(graphWithCycle);
            CondensationGraph c = new CondensationGraph(graphWithCycle, s);
            DirectedGraph d = c.getCondensation();
            new KahnTopologicalSort().sort(d);
            new DAGShortestPath().computeShortestPaths(d, s.getComponentId()[0]);
            new DAGLongestPath().findCriticalPath(d);
        }, "Should execute complete pipeline without throwing");
    }

    @Test
    public void testMetrics_AllAlgorithmsNonNegativeAndValid() {
        TarjanSCC tarjan = new TarjanSCC();
        tarjan.findSCC(testGraph);
        assertTrue(tarjan.getMetrics().getExecutionTimeMs() >= 0);
        assertTrue(tarjan.getMetrics().getTotalOperations() > 0);

        SCCResult sccResult = tarjan.findSCC(testGraph);
        CondensationGraph cg = new CondensationGraph(testGraph, sccResult);
        DirectedGraph dag = cg.getCondensation();

        KahnTopologicalSort ts = new KahnTopologicalSort();
        ts.sort(dag);
        assertTrue(ts.getMetrics().getExecutionTimeMs() >= 0);
        assertTrue(ts.getMetrics().getTotalOperations() >= 0);

        DAGShortestPath sp = new DAGShortestPath();
        sp.computeShortestPaths(dag, sccResult.getComponentId()[0]);
        assertTrue(sp.getMetrics().getExecutionTimeMs() >= 0);
        assertTrue(sp.getMetrics().getTotalOperations() >= 0);

        DAGLongestPath lp = new DAGLongestPath();
        lp.findCriticalPath(dag);
        assertTrue(lp.getMetrics().getExecutionTimeMs() >= 0);
    }

    @Test
    public void testReproducibility_ConsistencyAndStructure() {
        TarjanSCC t1 = new TarjanSCC();
        SCCResult r1 = t1.findSCC(testGraph);
        TarjanSCC t2 = new TarjanSCC();
        SCCResult r2 = t2.findSCC(testGraph);

        assertEquals(r1.getNumComponents(), r2.getNumComponents());
        int[] id1 = r1.getComponentId();
        int[] id2 = r2.getComponentId();
        for (int i = 0; i < id1.length; i++) {
            assertEquals(id1[i], id2[i]);
        }

        assertTrue(r1.getNumComponents() > 0);
        assertTrue(r1.getNumComponents() <= testGraph.getN());
        assertEquals(r1.getNumComponents(), r1.getComponents().size());

        long ops1 = t1.getMetrics().getTotalOperations();
        long ops2 = t2.getMetrics().getTotalOperations();
        assertEquals(ops1, ops2);
    }

    @Test
    public void testCondensation_DAGValidityAndArrayConsistency() {
        TarjanSCC tarjan = new TarjanSCC();
        SCCResult sccResult = tarjan.findSCC(testGraph);
        CondensationGraph cg = new CondensationGraph(testGraph, sccResult);
        DirectedGraph dag = cg.getCondensation();

        KahnTopologicalSort ts = new KahnTopologicalSort();
        TopologicalSortResult topoResult = ts.sort(dag);
        assertTrue(topoResult.isDAG(), "Condensation must be DAG");
        assertEquals(dag.getN(), topoResult.getOrder().size());

        DAGShortestPath sp = new DAGShortestPath();
        PathResult pathResult = sp.computeShortestPaths(dag, sccResult.getComponentId()[0]);
        assertEquals(dag.getN(), pathResult.getDistances().length);
        assertEquals(dag.getN(), pathResult.getParent().length);
    }

    @Test
    public void testPipeline_IntegrationWithEdgeCases() {
        TarjanSCC tarjan = new TarjanSCC();
        SCCResult sccResult = tarjan.findSCC(graphWithCycle);
        CondensationGraph cg = new CondensationGraph(graphWithCycle, sccResult);
        DirectedGraph dag = cg.getCondensation();

        assertTrue(dag.getN() <= graphWithCycle.getN());

        DAGShortestPath sp = new DAGShortestPath();
        PathResult spResult = sp.computeShortestPaths(dag, sccResult.getComponentId()[0]);
        assertNotNull(spResult.getDistances());
        assertNotNull(spResult.getParent());

        DAGLongestPath lp = new DAGLongestPath();
        CriticalPathResult lpResult = lp.findCriticalPath(dag);
        assertTrue(lpResult.getLength() >= 0);
    }
}
