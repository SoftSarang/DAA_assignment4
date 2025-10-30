package com.aitu.utils;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

public class Metrics {
    private long startTime;
    private double executionTimeMs = 0.0;
    private String algorithmName;

    // SCC-specific metrics (Tarjan)
    private long dfsVisits = 0;
    private long edgeExplorations = 0;
    private long stackOperations = 0;
    private long lowLinkUpdates = 0;

    // Topological Sort metrics (Kahn)
    private long queueOperations = 0;
    private long inDegreeUpdates = 0;

    // DAG Shortest/Longest Path metrics
    private long relaxations = 0;
    private long distanceUpdates = 0;
    private long comparisons = 0;


    public Metrics(String algorithmName) {
        this.algorithmName = algorithmName;
    }

    public void startTimer() {
        startTime = System.nanoTime();
    }

    public void stopTimer() {
        long endTime = System.nanoTime();
        executionTimeMs = (endTime - startTime) / 1_000_000.0;
    }

    public void incrementDFSVisit() {
        dfsVisits++;
    }

    public void incrementEdgeExploration() {
        edgeExplorations++;
    }

    public void incrementStackOperation() {
        stackOperations++;
    }

    public void incrementLowLinkUpdate() {
        lowLinkUpdates++;
    }

    public void incrementQueueOperation() {
        queueOperations++;
    }

    public void incrementInDegreeUpdate() {
        inDegreeUpdates++;
    }

    public void incrementRelaxation() {
        relaxations++;
    }

    public void incrementDistanceUpdate() {
        distanceUpdates++;
    }

    public void incrementComparison() {
        comparisons++;
    }

    public long getDFSVisits() {
        return dfsVisits;
    }

    public long getEdgeExplorations() {
        return edgeExplorations;
    }

    public long getStackOperations() {
        return stackOperations;
    }

    public long getLowLinkUpdates() {
        return lowLinkUpdates;
    }

    public long getQueueOperations() {
        return queueOperations;
    }

    public long getInDegreeUpdates() {
        return inDegreeUpdates;
    }

    public long getRelaxations() {
        return relaxations;
    }

    public long getDistanceUpdates() {
        return distanceUpdates;
    }

    public long getComparisons() {
        return comparisons;
    }

    public double getExecutionTimeMs() {
        return executionTimeMs;
    }

    public String getAlgorithmName() {
        return algorithmName;
    }

    public long getTotalOperations() {
        if (algorithmName.contains("SCC") || algorithmName.contains("Tarjan")) {
            return dfsVisits + edgeExplorations + stackOperations + lowLinkUpdates;
        } else if (algorithmName.contains("Topo") || algorithmName.contains("Kahn")) {
            return queueOperations + inDegreeUpdates;
        } else if (algorithmName.contains("Path") || algorithmName.contains("DAG")) {
            return relaxations + distanceUpdates + comparisons;
        }
        return 0;
    }

    public void reset() {
        dfsVisits = 0;
        edgeExplorations = 0;
        stackOperations = 0;
        lowLinkUpdates = 0;
        queueOperations = 0;
        inDegreeUpdates = 0;
        relaxations = 0;
        distanceUpdates = 0;
        comparisons = 0;
        executionTimeMs = 0.0;
    }

    public static void writeCsv(String filePath, String[][] data, boolean append) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath, append))) {
            if (!append) {
                writer.write("graph_id;vertices;edges;density;variant;algorithm;total_operations_count;total_execution_time_ms;path_length\n");
            }
            for (String[] row : data) {
                writer.write(String.join(";", row) + "\n");
            }
        }
    }
}

