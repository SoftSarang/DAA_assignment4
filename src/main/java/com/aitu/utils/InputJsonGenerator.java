package com.aitu.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

/**
 * Generates test graphs with various structures for performance evaluation.
 * Creates both sparse and dense variants for comparison.
 */
public class InputJsonGenerator {
    private final Random random;
    private final Gson gson;

    public InputJsonGenerator() {
        this.random = new Random();
        this.gson = new GsonBuilder().setPrettyPrinting().create();
    }

    /**
     * Generates a single graph with specified structure and density.
     * Sparse: nodeCount * 1.8 edges
     * Dense: nodeCount * 4 edges (or close to complete graph)
     */
    private JsonObject generateGraph(int id, int nodeCount, String variant, boolean isDense) {
        Set<String> edgeSet = new HashSet<>();
        JsonArray edges = new JsonArray();

        int targetEdges;
        if (isDense) {
            targetEdges = Math.min(nodeCount * 4, nodeCount * (nodeCount - 1) / 3);
        } else {
            targetEdges = (int)(nodeCount * 1.8);
        }

        // Generate based on variant
        switch (variant) {
            case "pure_dag":
                generatePureDAG(nodeCount, targetEdges, edges, edgeSet);
                break;
            case "one_cycle":
                generateOneCycle(nodeCount, targetEdges, edges, edgeSet);
                break;
            case "two_cycles":
                generateTwoCycles(nodeCount, targetEdges, edges, edgeSet);
                break;
            case "mixed":
                generateSeveralSCCs(nodeCount, targetEdges, edges, edgeSet, 3, 5);
                break;
            case "many_sccs":
                generateSeveralSCCs(nodeCount, targetEdges, edges, edgeSet, 5, 10);
                break;
        }

        int source = getSmartSource(nodeCount, variant);

        JsonObject graph = new JsonObject();
        graph.addProperty("id", id);
        graph.addProperty("directed", true);
        graph.addProperty("n", nodeCount);
        graph.add("edges", edges);
        graph.addProperty("source", source);
        graph.addProperty("weight_model", "edge");
        graph.addProperty("density", isDense ? "dense" : "sparse");
        graph.addProperty("variant", variant);
        return graph;
    }

    private int getSmartSource(int nodeCount, String variant) {
        switch (variant) {
            case "pure_dag":
                return 0;
            case "one_cycle":
                return 0;
            case "two_cycles":
                return 0;
            case "mixed":
                return 0;
            case "many_sccs":
                return Math.max(0, nodeCount / 3);
            default:
                return 0;
        }
    }

    /**
     * Generates DAG by assigning levels and ensuring u < v implies level[u] < level[v]
     */
    private void generatePureDAG(int n, int targetEdges, JsonArray edges, Set<String> edgeSet) {
        int[] level = new int[n];
        int numLevels = Math.max(3, (int)Math.sqrt(n));

        // Assign levels: vertices with lower indices get lower levels
        for (int i = 0; i < n; i++) {
            level[i] = (i * numLevels) / n;
        }

        // Add edges respecting levels (ensures acyclic)
        for (int i = 0; i < n - 1; i++) {
            for (int j = i + 1; j < n; j++) {
                if (level[j] > level[i]) {
                    addEdge(i, j, edges, edgeSet);
                    break;
                }
            }
        }

        // Fill remaining edges randomly while respecting DAG property
        int attempts = 0;
        int maxAttempts = 10000;
        while (edges.size() < targetEdges && attempts < maxAttempts) {
            int u = random.nextInt(n);
            int v = random.nextInt(n);
            if (u < v && level[u] < level[v]) {
                addEdge(u, v, edges, edgeSet);
            }
            attempts++;
        }
    }

    /**
     * Generates single cycle: 0->1->2->...->n-1->0
     */
    private void generateOneCycle(int n, int targetEdges, JsonArray edges, Set<String> edgeSet) {
        // Create cycle
        for (int i = 0; i < n - 1; i++) {
            addEdge(i, i + 1, edges, edgeSet);
        }
        addEdge(n - 1, 0, edges, edgeSet);

        // Add random edges to reach target
        int attempts = 0;
        int maxAttempts = 10000;
        while (edges.size() < targetEdges && attempts < maxAttempts) {
            int u = random.nextInt(n);
            int v = random.nextInt(n);
            if (u != v) {
                addEdge(u, v, edges, edgeSet);
            }
            attempts++;
        }
    }

    /**
     * Generates two separate cycles and connects them
     */
    private void generateTwoCycles(int n, int targetEdges, JsonArray edges, Set<String> edgeSet) {
        int split = n / 2;

        // First cycle: 0->1->...->split-1->0
        for (int i = 0; i < split - 1; i++) {
            addEdge(i, i + 1, edges, edgeSet);
        }
        addEdge(split - 1, 0, edges, edgeSet);

        // Second cycle: split->...->n-1->split
        for (int i = split; i < n - 1; i++) {
            addEdge(i, i + 1, edges, edgeSet);
        }
        addEdge(n - 1, split, edges, edgeSet);

        // Connect cycles
        addEdge(random.nextInt(split), split + random.nextInt(n - split), edges, edgeSet);

        // Fill remaining edges
        int attempts = 0;
        int maxAttempts = 10000;
        while (edges.size() < targetEdges && attempts < maxAttempts) {
            int u = random.nextInt(n);
            int v = random.nextInt(n);
            boolean sameGroup = (u < split && v < split) || (u >= split && v >= split);
            if (u != v && sameGroup) {
                addEdge(u, v, edges, edgeSet);
            }
            attempts++;
        }
    }

    /**
     * Generates multiple SCCs connected in a DAG structure
     */
    private int generateSeveralSCCs(int n, int targetEdges, JsonArray edges,
                                    Set<String> edgeSet, int minSCCs, int maxSCCs) {
        int numSCCs = minSCCs + random.nextInt(maxSCCs - minSCCs + 1);
        numSCCs = Math.min(numSCCs, n / 2);

        List<List<Integer>> sccs = new ArrayList<>();
        int verticesPerSCC = n / numSCCs;
        int remainder = n % numSCCs;
        int current = 0;

        for (int i = 0; i < numSCCs; i++) {
            int size = verticesPerSCC + (i < remainder ? 1 : 0);
            List<Integer> scc = new ArrayList<>();
            for (int j = 0; j < size; j++) {
                scc.add(current++);
            }
            sccs.add(scc);
        }

        for (List<Integer> scc : sccs) {
            if (scc.size() == 1) continue;
            for (int i = 0; i < scc.size() - 1; i++) {
                addEdge(scc.get(i), scc.get(i + 1), edges, edgeSet);
            }
            addEdge(scc.get(scc.size() - 1), scc.get(0), edges, edgeSet);
        }

        for (int i = 0; i < numSCCs - 1; i++) {
            List<Integer> from = sccs.get(i);
            List<Integer> to = sccs.get(i + 1);
            int u = from.get(random.nextInt(from.size()));
            int v = to.get(random.nextInt(to.size()));
            addEdge(u, v, edges, edgeSet);
        }

        for (int i = 0; i < numSCCs - 1; i++) {
            List<Integer> from = sccs.get(i);
            for (int j = i + 2; j < numSCCs && edges.size() < targetEdges * 0.8; j++) {
                List<Integer> to = sccs.get(j);
                if (random.nextDouble() < 0.5) {
                    int u = from.get(random.nextInt(from.size()));
                    int v = to.get(random.nextInt(to.size()));
                    addEdge(u, v, edges, edgeSet);
                }
            }
        }

        int attempts = 0;
        int maxAttempts = 10000;

        while (edges.size() < targetEdges && attempts < maxAttempts) {
            int sccIdx = random.nextInt(numSCCs);
            List<Integer> scc = sccs.get(sccIdx);
            if (scc.size() > 1) {
                int u = scc.get(random.nextInt(scc.size()));
                int v = scc.get(random.nextInt(scc.size()));
                if (u != v) {
                    addEdge(u, v, edges, edgeSet);
                }
            }
            attempts++;
        }

        return numSCCs;
    }

    /**
     * Adds edge with random weight (1.0 to 10.0) if not duplicate
     */
    private void addEdge(int u, int v, JsonArray edges, Set<String> edgeSet) {
        String key = u + "->" + v;
        if (!edgeSet.contains(key)) {
            JsonObject edge = new JsonObject();
            edge.addProperty("u", u);
            edge.addProperty("v", v);
            edge.addProperty("w", Math.round((1 + random.nextDouble() * 9) * 10) / 10.0);
            edges.add(edge);
            edgeSet.add(key);
        }
    }

    /**
     * Generates and saves both sparse and dense variants
     */
    private void generateAndSave(String filename, boolean isDense) throws IOException {
        JsonObject root = new JsonObject();
        JsonArray graphs = new JsonArray();
        int id = 1;

        if (!isDense) {
            graphs.add(generateGraph(id++, 6, "pure_dag", false));
            graphs.add(generateGraph(id++, 8, "one_cycle", false));
            graphs.add(generateGraph(id++, 10, "two_cycles", false));
            graphs.add(generateGraph(id++, 12, "mixed", false));
            graphs.add(generateGraph(id++, 16, "mixed", false));
            graphs.add(generateGraph(id++, 20, "mixed", false));
            graphs.add(generateGraph(id++, 25, "many_sccs", false));
            graphs.add(generateGraph(id++, 35, "pure_dag", false));
            graphs.add(generateGraph(id++, 50, "many_sccs", false));
        } else {
            graphs.add(generateGraph(id++, 6, "pure_dag", true));
            graphs.add(generateGraph(id++, 8, "one_cycle", true));
            graphs.add(generateGraph(id++, 10, "two_cycles", true));
            graphs.add(generateGraph(id++, 12, "mixed", true));
            graphs.add(generateGraph(id++, 16, "mixed", true));
            graphs.add(generateGraph(id++, 20, "mixed", true));
            graphs.add(generateGraph(id++, 25, "many_sccs", true));
            graphs.add(generateGraph(id++, 35, "pure_dag", true));
            graphs.add(generateGraph(id++, 50, "many_sccs", true));
        }

        root.add("graphs", graphs);
        File dir = new File("data");
        if (!dir.exists()) dir.mkdirs();
        try (FileWriter file = new FileWriter("data/" + filename)) {
            gson.toJson(root, file);
        }
    }


    public void generateAll() throws IOException {
        generateAndSave("input_sparse.json", false);
        generateAndSave("input_dense.json", true);
    }

    /**
     * Main entry point: generates both input_sparse.json and input_dense.json
     */
    public static void main(String[] args) {
        InputJsonGenerator generator = new InputJsonGenerator();
        try {
            generator.generateAll();
        } catch (IOException e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
