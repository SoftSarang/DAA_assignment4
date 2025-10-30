package com.aitu;

import com.aitu.core.DirectedGraph;
import com.aitu.graph.dagsp.DAGLongestPath;
import com.aitu.graph.dagsp.CriticalPathResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class DAGLongestPathTest {

    private DirectedGraph simplePath;
    private DirectedGraph complexDAG;

    @BeforeEach
    public void setUp() {
        simplePath = new DirectedGraph(3);
        simplePath.addEdge(0, 1, 5.0);
        simplePath.addEdge(1, 2, 3.0);

        complexDAG = new DirectedGraph(5);
        complexDAG.addEdge(0, 1, 1.0);
        complexDAG.addEdge(1, 2, 2.0);
        complexDAG.addEdge(2, 3, 3.0);
        complexDAG.addEdge(3, 4, 1.0);
        complexDAG.addEdge(0, 2, 10.0);
        complexDAG.addEdge(1, 4, 2.0);
    }

    @Test
    public void testLongestPath_FindCriticalPath() {
        DAGLongestPath lp = new DAGLongestPath();

        CriticalPathResult result1 = lp.findCriticalPath(simplePath);
        assertNotNull(result1, "Critical path result should not be null");
        assertEquals(8.0, result1.getLength(), "Critical path should be 5+3=8");

        List<Integer> path1 = result1.getCriticalPath();
        assertNotNull(path1, "Critical path should not be null");
        assertTrue(path1.size() > 0, "Critical path should contain vertices");

        CriticalPathResult result2 = lp.findCriticalPath(complexDAG);
        assertNotNull(result2, "Result should not be null");
        assertTrue(result2.getLength() > 0, "Critical path length should be positive");

        assertEquals(14.0, result2.getLength(), "Should find longest path 0->2->3->4");
    }

    @Test
    public void testLongestPath_ValidPathStructure() {
        DAGLongestPath lp = new DAGLongestPath();
        CriticalPathResult result = lp.findCriticalPath(complexDAG);

        List<Integer> path = result.getCriticalPath();

        for (int i = 0; i < path.size() - 1; i++) {
            int u = path.get(i);
            int v = path.get(i + 1);

            boolean edgeExists = complexDAG.getAdjacent(u).stream()
                    .anyMatch(e -> e.getTo() == v);
            assertTrue(edgeExists, "Edge " + u + "->" + v + " should exist in path");
        }

        assertNotNull(lp.getMetrics(), "Metrics should not be null");
        assertTrue(lp.getMetrics().getTotalOperations() > 0, "Should count operations");
    }
}
