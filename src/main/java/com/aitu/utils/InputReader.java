package com.aitu.utils;

import com.aitu.core.DirectedGraph;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Parses JSON input files containing graph definitions.
 */
public class InputReader {

    /**
     * Loads all graphs from a JSON file with validation.
     */
    public static List<GraphData> loadAllGraphs(String filepath) throws IOException {
        Gson gson = new Gson();
        try (FileReader reader = new FileReader(filepath)) {
            JsonObject root = gson.fromJson(reader, JsonObject.class);
            if (root == null) throw new IOException("Invalid JSON format");

            JsonArray graphsArray = root.getAsJsonArray("graphs");
            if (graphsArray == null) throw new IOException("No 'graphs' array in JSON");

            List<GraphData> graphsList = new ArrayList<>();

            // Parse each graph
            for (JsonElement element : graphsArray) {
                JsonObject graphJson = element.getAsJsonObject();

                int id = graphJson.get("id").getAsInt();
                int n = graphJson.get("n").getAsInt();

                if (n <= 0) throw new IllegalArgumentException("Vertices must be > 0");

                int source = graphJson.has("source") ? graphJson.get("source").getAsInt() : 0;
                if (source < 0 || source >= n) {
                    throw new IllegalArgumentException("Source out of bounds");
                }

                String density = graphJson.has("density") ? graphJson.get("density").getAsString() : "unknown";
                String variant = graphJson.has("variant") ? graphJson.get("variant").getAsString() : "unknown";

                DirectedGraph graph = new DirectedGraph(n);

                // Parse edges
                JsonArray edges = graphJson.getAsJsonArray("edges");
                if (edges != null) {
                    for (JsonElement edgeElem : edges) {
                        JsonObject edge = edgeElem.getAsJsonObject();
                        int u = edge.get("u").getAsInt();
                        int v = edge.get("v").getAsInt();
                        double w = edge.get("w").getAsDouble();

                        if (u < 0 || u >= n || v < 0 || v >= n) {
                            throw new IllegalArgumentException("Edge vertex out of bounds");
                        }

                        graph.addEdge(u, v, w);
                    }
                }

                graphsList.add(new GraphData(id, graph, source, density, variant));
            }

            return graphsList;
        } catch (IOException e) {
            System.err.println("Error reading JSON: " + e.getMessage());
            throw e;
        }
    }

    /**
     * Inner class holding parsed graph metadata and structure.
     */
    public static class GraphData {
        private final int id;
        private final DirectedGraph graph;
        private final int source;
        private final String density;  // "sparse" or "dense"
        private final String variant;  // "pure_dag", "one_cycle", "two_cycles", "mixed", "many_sccs"

        public GraphData(int id, DirectedGraph graph, int source, String density, String variant) {
            this.id = id;
            this.graph = graph;
            this.source = source;
            this.density = density;
            this.variant = variant;
        }

        public int getId() {
            return id;
        }

        public DirectedGraph getGraph() {
            return graph;
        }

        public int getSource() {
            return source;
        }

        public String getDensity() {
            return density;
        }

        public String getVariant() {
            return variant;
        }
    }
}

