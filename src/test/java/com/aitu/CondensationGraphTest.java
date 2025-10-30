package com.aitu;

import com.aitu.core.DirectedGraph;
import com.aitu.graph.scc.TarjanSCC;
import com.aitu.graph.scc.SCCResult;
import com.aitu.graph.scc.CondensationGraph;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class CondensationGraphTest {

    private DirectedGraph graphWithCycle;
    private DirectedGraph pureDAG;

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
    }

    @Test
    public void testCondensationGraph_StructureAndSize() {
        TarjanSCC tarjan = new TarjanSCC();
        SCCResult sccResult = tarjan.findSCC(graphWithCycle);

        CondensationGraph cg = new CondensationGraph(graphWithCycle, sccResult);
        DirectedGraph condensed = cg.getCondensation();

        assertNotNull(condensed, "Condensation should not be null");
        assertTrue(condensed.getN() > 0, "Condensation should have vertices");

        assertEquals(3, condensed.getN(), "Condensation should have 3 vertices");

        assertTrue(condensed.getAllEdges().size() <= graphWithCycle.getAllEdges().size(),
                "Condensation should have <= edges than original");
    }

    @Test
    public void testCondensationGraph_PreservesDAG() {
        TarjanSCC tarjan = new TarjanSCC();
        SCCResult sccResult = tarjan.findSCC(pureDAG);

        CondensationGraph cg = new CondensationGraph(pureDAG, sccResult);
        DirectedGraph condensed = cg.getCondensation();

        assertEquals(pureDAG.getN(), condensed.getN(), "DAG vertices should match");
        assertEquals(pureDAG.getAllEdges().size(), condensed.getAllEdges().size(),
                "DAG edges should match");
    }
}
