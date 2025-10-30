package com.aitu;

import com.aitu.core.DirectedGraph;
import com.aitu.graph.dagsp.DAGShortestPath;
import com.aitu.graph.dagsp.PathResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class DAGShortestPathTest {

    private DirectedGraph simplePath;
    private DirectedGraph complexDAG;

    @BeforeEach
    public void setUp() {
        simplePath = new DirectedGraph(5);
        simplePath.addEdge(0, 1, 1.0);
        simplePath.addEdge(1, 2, 2.0);
        simplePath.addEdge(2, 3, 3.0);
        simplePath.addEdge(3, 4, 1.0);

        complexDAG = new DirectedGraph(5);
        complexDAG.addEdge(0, 1, 1.0);
        complexDAG.addEdge(1, 2, 2.0);
        complexDAG.addEdge(2, 3, 3.0);
        complexDAG.addEdge(3, 4, 1.0);
        complexDAG.addEdge(0, 2, 4.0);
        complexDAG.addEdge(1, 4, 10.0);
    }

    @Test
    public void testShortestPath_ComputeAndChoose() {
        DAGShortestPath sp1 = new DAGShortestPath();
        PathResult result1 = sp1.computeShortestPaths(simplePath, 0);
        assertNotNull(result1, "Path result should not be null");
        double[] distances1 = result1.getDistances();

        assertEquals(0.0, distances1[0], "Distance to source should be 0");
        assertEquals(1.0, distances1[1], "Distance 0->1 should be 1.0");
        assertEquals(3.0, distances1[2], "Distance 0->1->2 should be 3.0");
        assertEquals(6.0, distances1[3], "Distance 0->1->2->3 should be 6.0");
        assertEquals(7.0, distances1[4], "Distance 0->1->2->3->4 should be 7.0");

        DAGShortestPath sp2 = new DAGShortestPath();
        PathResult result2 = sp2.computeShortestPaths(complexDAG, 0);
        double[] distances2 = result2.getDistances();

        assertEquals(3.0, distances2[2], "Should choose shortest path 0->1->2 (3.0) over 0->2 (4.0)");
        assertEquals(7.0, distances2[4], "Should choose shortest path 0->1->2->3->4 (10.0)");
    }


    @Test
    public void testShortestPath_StructureAndReachability() {
        DAGShortestPath sp = new DAGShortestPath();

        PathResult result1 = sp.computeShortestPaths(simplePath, 0);
        assertNotNull(result1, "Result should not be null");
        assertNotNull(result1.getDistances(), "Distances should not be null");
        assertNotNull(result1.getParent(), "Parent should not be null");
        assertEquals(5, result1.getDistances().length, "Should have distances for all vertices");

        DirectedGraph disconnected = new DirectedGraph(4);
        disconnected.addEdge(0, 1, 1.0);
        disconnected.addEdge(2, 3, 1.0);

        PathResult result2 = sp.computeShortestPaths(disconnected, 0);
        double[] distances2 = result2.getDistances();

        assertEquals(Double.POSITIVE_INFINITY, distances2[2], "Unreachable vertex should be infinity");
        assertEquals(Double.POSITIVE_INFINITY, distances2[3], "Unreachable vertex should be infinity");

        assertNotNull(sp.getMetrics(), "Metrics should not be null");
        assertTrue(sp.getMetrics().getTotalOperations() > 0, "Should count operations");
    }
}
