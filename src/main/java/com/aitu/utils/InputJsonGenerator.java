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
        this.random = new Random(42);
        this.gson = new GsonBuilder().setPrettyPrinting().create();
    }

    /**
     * Generates a single graph with specified structure and density.
     * Sparse: nodeCount * 1.8 edges
     * Dense: close to complete graph
     */
    private JsonObject generateGraph(int id, int nodeCount, String variant, boolean isDense) {
        Set<String> edgeSet = new HashSet<>();
        JsonArray edges = new JsonArray();

        int targetEdges;
        if (isDense) {
            targetEdges = nodeCount * (nodeCount - 1) / 2;
        } else {
            targetEdges = (int)(nodeCount * 1.5);
        }

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
                generateMixed(nodeCount, targetEdges, edges, edgeSet, isDense);
                break;
            case "many_sccs":
                generateManySCCs(nodeCount, targetEdges, edges, edgeSet, isDense);
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

        for (int i = 0; i < n; i++) {
            level[i] = (i * numLevels) / n;
        }

        for (int i = 0; i < n - 1; i++) {
            for (int j = i + 1; j < n; j++) {
                if (level[j] > level[i]) {
                    addEdge(i, j, edges, edgeSet);
                    break;
                }
            }
        }

        int attempts = 0;
        int maxAttempts = targetEdges * 10;
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
        for (int i = 0; i < n - 1; i++) {
            addEdge(i, i + 1, edges, edgeSet);
        }
        addEdge(n - 1, 0, edges, edgeSet);

        int attempts = 0;
        int maxAttempts = targetEdges * 10;
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

        for (int i = 0; i < split - 1; i++) {
            addEdge(i, i + 1, edges, edgeSet);
        }
        addEdge(split - 1, 0, edges, edgeSet);

        for (int i = split; i < n - 1; i++) {
            addEdge(i, i + 1, edges, edgeSet);
        }
        addEdge(n - 1, split, edges, edgeSet);

        addEdge(random.nextInt(split), split + random.nextInt(n - split), edges, edgeSet);

        int attempts = 0;
        int maxAttempts = targetEdges * 10;
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
     * Generates mixed graph
     */
    private void generateMixed(int n, int targetEdges, JsonArray edges, Set<String> edgeSet, boolean isDense) {

        int minSingletons = Math.max(2, n / 8);
        int minCyclicVertices = Math.max(4, n / 3);

        List<List<Integer>> components = new ArrayList<>();
        int current = 0;

        for (int i = 0; i < minSingletons && current < n; i++) {
            List<Integer> singleton = new ArrayList<>();
            singleton.add(current++);
            components.add(singleton);
        }

        if (current < n) {
            int cyclicSize = Math.min(Math.max(3, minCyclicVertices / 2), n - current);
            List<Integer> cyclicComp = new ArrayList<>();
            for (int i = 0; i < cyclicSize && current < n; i++) {
                cyclicComp.add(current++);
            }
            if (!cyclicComp.isEmpty()) {
                components.add(cyclicComp);
            }
        }

        if (current < n) {
            int remaining = n - current;
            int numRemainingComps = Math.max(1, remaining / 3);

            List<List<Integer>> remainingComps = partitionVertices(remaining, numRemainingComps);
            for (List<Integer> comp : remainingComps) {
                List<Integer> adjustedComp = new ArrayList<>();
                for (int v : comp) {
                    adjustedComp.add(v + current);
                }
                components.add(adjustedComp);
            }
        }

        int cyclicCreated = 0;
        int acyclicCreated = 0;

        for (int i = 0; i < components.size(); i++) {
            List<Integer> comp = components.get(i);

            if (comp.size() == 1) {
                acyclicCreated++;
                continue;
            }

            boolean shouldBeCyclic;
            if (i < 2) {
                shouldBeCyclic = (i == 1);
            } else {
                shouldBeCyclic = (i % 2 == 1);
            }

            if (shouldBeCyclic) {
                if (comp.size() >= 4) {
                    createComplexSCC(comp, edges, edgeSet, isDense);
                } else {
                    createSimpleCycle(comp, edges, edgeSet);
                }
                cyclicCreated++;
            } else {
                createStrictlyAcyclicComponent(comp, edges, edgeSet, isDense);
                acyclicCreated++;
            }
        }

        connectComponentsAsDAG(components, edges, edgeSet);

        fillRemainingEdgesMixed(components, targetEdges, edges, edgeSet, isDense);
    }

    private void fillRemainingEdgesMixed(List<List<Integer>> components, int targetEdges,
                                         JsonArray edges, Set<String> edgeSet, boolean isDense) {
        int attempts = 0;
        int maxAttempts = targetEdges * 20;

        while (edges.size() < targetEdges && attempts < maxAttempts) {
            if (random.nextDouble() < 0.9) {
                addEdgeWithinComponentsMixed(components, edges, edgeSet);
            } else {
                addEdgeBetweenComponentsDAG(components, edges, edgeSet);
            }
            attempts++;
        }
    }

    private void addEdgeWithinComponentsMixed(List<List<Integer>> components, JsonArray edges, Set<String> edgeSet) {
        List<List<Integer>> multiVertexComponents = new ArrayList<>();
        for (List<Integer> comp : components) {
            if (comp.size() >= 2) {
                multiVertexComponents.add(comp);
            }
        }

        if (multiVertexComponents.isEmpty()) return;

        List<Integer> component = multiVertexComponents.get(random.nextInt(multiVertexComponents.size()));
        int u = component.get(random.nextInt(component.size()));
        int v = component.get(random.nextInt(component.size()));
        if (u != v) {
            addEdge(u, v, edges, edgeSet);
        }
    }

    /**
     * Generates many SCCs
     */
    private void generateManySCCs(int n, int targetEdges, JsonArray edges, Set<String> edgeSet, boolean isDense) {
        int numSCCs = Math.max(5, 5 + random.nextInt(6));
        numSCCs = Math.min(numSCCs, n / 2);

        List<List<Integer>> sccs = partitionVertices(n, numSCCs);

        for (List<Integer> scc : sccs) {
            if (scc.size() == 1) {
                addEdge(scc.get(0), scc.get(0), edges, edgeSet);
            } else if (scc.size() <= 3) {
                createSimpleCycle(scc, edges, edgeSet);
            } else {
                createComplexSCC(scc, edges, edgeSet, isDense);
            }
        }

        connectComponentsAsDAG(sccs, edges, edgeSet);

        fillRemainingEdgesSafely(sccs, targetEdges, edges, edgeSet, isDense);
    }

    private List<List<Integer>> partitionVertices(int n, int numComponents) {
        List<List<Integer>> components = new ArrayList<>();
        int verticesPerComponent = n / numComponents;
        int remainder = n % numComponents;
        int current = 0;

        for (int i = 0; i < numComponents; i++) {
            int size = verticesPerComponent + (i < remainder ? 1 : 0);
            size = Math.max(1, size);
            List<Integer> component = new ArrayList<>();
            for (int j = 0; j < size && current < n; j++) {
                component.add(current++);
            }
            if (!component.isEmpty()) {
                components.add(component);
            }
        }
        return components;
    }

    private void createSimpleCycle(List<Integer> vertices, JsonArray edges, Set<String> edgeSet) {
        if (vertices.isEmpty()) return;

        for (int i = 0; i < vertices.size() - 1; i++) {
            addEdge(vertices.get(i), vertices.get(i + 1), edges, edgeSet);
        }
        addEdge(vertices.get(vertices.size() - 1), vertices.get(0), edges, edgeSet);
    }

    private void createComplexSCC(List<Integer> vertices, JsonArray edges, Set<String> edgeSet, boolean isDense) {
        createSimpleCycle(vertices, edges, edgeSet);

        int maxPossible = vertices.size() * (vertices.size() - 1);
        int additionalEdges;

        if (isDense) {
            additionalEdges = Math.min(maxPossible / 2, vertices.size() * 2);
        } else {
            additionalEdges = Math.min(maxPossible / 4, vertices.size() / 2 + 1);
        }

        int attempts = 0;
        int maxAttempts = additionalEdges * 10;
        while (attempts < maxAttempts && additionalEdges > 0) {
            int u = vertices.get(random.nextInt(vertices.size()));
            int v = vertices.get(random.nextInt(vertices.size()));
            if (u != v) {
                if (addEdge(u, v, edges, edgeSet)) {
                    additionalEdges--;
                }
            }
            attempts++;
        }
    }

    private void createStrictlyAcyclicComponent(List<Integer> vertices, JsonArray edges, Set<String> edgeSet, boolean isDense) {
        if (vertices.isEmpty()) return;

        List<Integer> sorted = new ArrayList<>(vertices);
        Collections.sort(sorted);

        if (sorted.size() == 1) {
            return;
        }

        for (int i = 0; i < sorted.size() - 1; i++) {
            addEdge(sorted.get(i), sorted.get(i + 1), edges, edgeSet);
        }

        if (sorted.size() > 2) {
            if (isDense) {
                for (int i = 0; i < sorted.size() - 2; i++) {
                    for (int j = i + 2; j < sorted.size(); j++) {
                        if (random.nextDouble() < 0.4) {
                            addEdge(sorted.get(i), sorted.get(j), edges, edgeSet);
                        }
                    }
                }
            } else {
                for (int i = 0; i < sorted.size() - 2; i++) {
                    if (random.nextDouble() < 0.3) {
                        int j = i + 2 + random.nextInt(sorted.size() - i - 2);
                        addEdge(sorted.get(i), sorted.get(j), edges, edgeSet);
                    }
                }
            }
        }
    }

    private void connectComponentsAsDAG(List<List<Integer>> components, JsonArray edges, Set<String> edgeSet) {
        for (int i = 0; i < components.size() - 1; i++) {
            List<Integer> from = components.get(i);
            List<Integer> to = components.get(i + 1);

            if (!from.isEmpty() && !to.isEmpty()) {
                int u = from.get(random.nextInt(from.size()));
                int v = to.get(random.nextInt(to.size()));
                addEdge(u, v, edges, edgeSet);
            }
        }

        for (int i = 0; i < components.size() - 1; i++) {
            List<Integer> from = components.get(i);
            for (int j = i + 2; j < components.size(); j++) {
                List<Integer> to = components.get(j);
                if (random.nextDouble() < 0.3 && !from.isEmpty() && !to.isEmpty()) {
                    int u = from.get(random.nextInt(from.size()));
                    int v = to.get(random.nextInt(to.size()));
                    addEdge(u, v, edges, edgeSet);
                }
            }
        }
    }

    private void fillRemainingEdgesSafely(List<List<Integer>> components, int targetEdges,
                                          JsonArray edges, Set<String> edgeSet, boolean isDense) {
        int attempts = 0;
        int maxAttempts = targetEdges * 20;

        while (edges.size() < targetEdges && attempts < maxAttempts) {
            if (random.nextDouble() < 0.9) {
                addEdgeWithinComponents(components, edges, edgeSet);
            } else {
                addEdgeBetweenComponentsDAG(components, edges, edgeSet);
            }
            attempts++;
        }
    }

    private void addEdgeWithinComponents(List<List<Integer>> components, JsonArray edges, Set<String> edgeSet) {
        List<Integer> component = components.get(random.nextInt(components.size()));

        if (component.size() >= 2) {
            int u = component.get(random.nextInt(component.size()));
            int v = component.get(random.nextInt(component.size()));
            if (u != v) {
                addEdge(u, v, edges, edgeSet);
            }
        } else if (component.size() == 1) {
            addEdge(component.get(0), component.get(0), edges, edgeSet);
        }
    }

    private void addEdgeBetweenComponentsDAG(List<List<Integer>> components, JsonArray edges, Set<String> edgeSet) {
        if (components.size() < 2) return;

        int fromIdx = random.nextInt(components.size());
        int toIdx = random.nextInt(components.size());

        if (fromIdx < toIdx) {
            List<Integer> fromComp = components.get(fromIdx);
            List<Integer> toComp = components.get(toIdx);

            if (!fromComp.isEmpty() && !toComp.isEmpty()) {
                int u = fromComp.get(random.nextInt(fromComp.size()));
                int v = toComp.get(random.nextInt(toComp.size()));
                addEdge(u, v, edges, edgeSet);
            }
        }
    }

    /**
     * Adds edge with random weight (1.0 to 10.0) if not duplicate
     * Returns true if edge was added, false if it was duplicate
     */
    private boolean addEdge(int u, int v, JsonArray edges, Set<String> edgeSet) {
        String key = u + "->" + v;
        if (!edgeSet.contains(key)) {
            JsonObject edge = new JsonObject();
            edge.addProperty("u", u);
            edge.addProperty("v", v);
            edge.addProperty("w", Math.round((1 + random.nextDouble() * 9) * 10) / 10.0);
            edges.add(edge);
            edgeSet.add(key);
            return true;
        }
        return false;
    }

    /**
     * Generates and saves both sparse and dense variants
     */
    private void generateAndSave(String filename, boolean isDense) throws IOException {
        JsonObject root = new JsonObject();
        JsonArray graphs = new JsonArray();
        int id = 1;

        graphs.add(generateGraph(id++, 6, "pure_dag", isDense));
        graphs.add(generateGraph(id++, 8, "one_cycle", isDense));
        graphs.add(generateGraph(id++, 10, "two_cycles", isDense));

        graphs.add(generateGraph(id++, 12, "mixed", isDense));
        graphs.add(generateGraph(id++, 16, "mixed", isDense));
        graphs.add(generateGraph(id++, 20, "mixed", isDense));

        graphs.add(generateGraph(id++, 25, "many_sccs", isDense));
        graphs.add(generateGraph(id++, 35, "pure_dag", isDense));
        graphs.add(generateGraph(id++, 50, "many_sccs", isDense));

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
