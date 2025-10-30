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

public class InputReader {
    public static List<GraphData> loadAllGraphs(String filepath) throws IOException {
        Gson gson = new Gson();
        JsonObject root = gson.fromJson(new FileReader(filepath), JsonObject.class);
        JsonArray graphsArray = root.getAsJsonArray("graphs");

        List<GraphData> graphsList = new ArrayList<>();

        for (JsonElement element : graphsArray) {
            JsonObject graphJson = element.getAsJsonObject();

            int id = graphJson.get("id").getAsInt();
            int n = graphJson.get("n").getAsInt();
            int source = graphJson.has("source") ? graphJson.get("source").getAsInt() : 0;
            String density = graphJson.has("density") ? graphJson.get("density").getAsString() : "unknown";
            String variant = graphJson.has("variant") ? graphJson.get("variant").getAsString() : "unknown";

            DirectedGraph graph = new DirectedGraph(n);
            JsonArray edges = graphJson.getAsJsonArray("edges");

            for (int i = 0; i < edges.size(); i++) {
                JsonObject edge = edges.get(i).getAsJsonObject();
                int u = edge.get("u").getAsInt();
                int v = edge.get("v").getAsInt();
                double w = edge.get("w").getAsDouble();
                graph.addEdge(u, v, w);
            }

            graphsList.add(new GraphData(id, graph, source, density, variant));
        }

        return graphsList;
    }

    public static GraphData loadGraphById(String filepath, int targetId) throws IOException {
        List<GraphData> allGraphs = loadAllGraphs(filepath);
        for (GraphData gd : allGraphs) {
            if (gd.getId() == targetId) {
                return gd;
            }
        }
        throw new IOException("Graph with ID " + targetId + " not found in " + filepath);
    }

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

        public String getExpectedSCCs() {
            switch (variant) {
                case "pure_dag":
                    return String.valueOf(graph.getN()); // Each vertex is an SCC
                case "one_cycle":
                    return "1";
                case "two_cycles":
                    return "2";
                case "mixed":
                    return "3-7";
                case "many_sccs":
                    return "5-10";
                default:
                    return "unknown";
            }
        }

        public String getDescription() {
            switch (variant) {
                case "pure_dag":
                    return "Pure DAG (no cycles)";
                case "one_cycle":
                    return "One large cycle";
                case "two_cycles":
                    return "Two separate cycles";
                case "mixed":
                    return "Mixed structure (several SCCs)";
                case "many_sccs":
                    return "Many SCCs";
                default:
                    return "Unknown structure";
            }
        }

        @Override
        public String toString() {
            return String.format("Graph #%d (%s, %s): %d vertices, %d edges, source=%d",
                    id, density, variant, graph.getN(), graph.getAllEdges().size(), source);
        }
    }
}

