package com.aitu;

import com.aitu.core.DirectedGraph;
import com.aitu.graph.scc.*;
import com.aitu.graph.topo.*;
import com.aitu.graph.dagsp.*;
import com.aitu.utils.InputReader;
import com.aitu.utils.InputReader.GraphData;
import com.aitu.utils.Metrics;
import com.aitu.core.Edge;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Main {

    public static void main(String[] args) {
        try {
            List<String[]> sccCsvData = new ArrayList<>();
            List<String[]> topoCsvData = new ArrayList<>();
            List<String[]> spCsvData = new ArrayList<>();
            List<String[]> cpCsvData = new ArrayList<>();
            List<String[]> summaryCsvData = new ArrayList<>();

            JsonArray sparseResults = new JsonArray();
            JsonArray denseResults = new JsonArray();

            warmupJIT();


            List<GraphData> sparseGraphs = InputReader.loadAllGraphs("data/input_sparse.json");
            for (GraphData gd : sparseGraphs) {
                JsonObject graphResult = processGraph(gd, sccCsvData, topoCsvData, spCsvData, cpCsvData, summaryCsvData);
                sparseResults.add(graphResult);
            }

            List<GraphData> denseGraphs = InputReader.loadAllGraphs("data/input_dense.json");
            for (GraphData gd : denseGraphs) {
                JsonObject graphResult = processGraph(gd, sccCsvData, topoCsvData, spCsvData, cpCsvData, summaryCsvData);
                denseResults.add(graphResult);
            }

            writeTaskCsv("data/output_scc.csv", sccCsvData,
                "graph_id;vertices;edges;density;variant;operations_count;num_scc;execution_time_ms");
            writeTaskCsv("data/output_topo.csv", topoCsvData,
                "graph_id;vertices;edges;density;variant;operations_count;execution_time_ms");
            writeTaskCsv("data/output_short_path.csv", spCsvData,
                "graph_id;vertices;edges;density;variant;operations_count;path_length;execution_time_ms");
            writeTaskCsv("data/output_critical_path.csv", cpCsvData,
                "graph_id;vertices;edges;density;variant;operations_count;path_length;execution_time_ms");
            writeTaskCsv("data/output_summary.csv", summaryCsvData,
                "graph_id;vertices;edges;density;variant;num_sccs;shortest_path_length;critical_path_length;total_operations_count;total_execution_time_ms");

            writeJson("data/output_sparse.json", sparseResults);
            writeJson("data/output_dense.json", denseResults);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static JsonObject processGraph(GraphData graphData,
                                         List<String[]> sccCsvData,
                                         List<String[]> topoCsvData,
                                         List<String[]> spCsvData,
                                         List<String[]> cpCsvData,
                                         List<String[]> summaryCsvData) {

        DirectedGraph graph = graphData.getGraph();
        int id = graphData.getId();
        int source = graphData.getSource();

        GraphResults results = new GraphResults();
        results.id = id;
        results.graph = graph;
        results.graphData = graphData;
        results.source = source;

        cleanMemory();
        TarjanSCC tarjan = new TarjanSCC();
        results.sccResult = tarjan.findSCC(graph);
        results.tarjanMetrics = tarjan.getMetrics();

        cleanMemory();
        CondensationGraph condensation = new CondensationGraph(graph, results.sccResult);
        results.dag = condensation.getCondensation();

        cleanMemory();
        KahnTopologicalSort topoSort = new KahnTopologicalSort();
        results.topoResult = topoSort.sort(results.dag);
        results.topoMetrics = topoSort.getMetrics();

        cleanMemory();
        DAGShortestPath shortestPath = new DAGShortestPath();
        int dagSource = results.sccResult.getComponentId()[source];
        results.spResult = shortestPath.computeShortestPaths(results.dag, dagSource);
        results.spMetrics = shortestPath.getMetrics();

        cleanMemory();
        DAGLongestPath longestPath = new DAGLongestPath();
        results.cpResult = longestPath.findCriticalPath(results.dag);
        results.lpMetrics = longestPath.getMetrics();

        collectTaskSpecificCsvData(sccCsvData, topoCsvData, spCsvData, cpCsvData, summaryCsvData, results);

        return buildJsonResult(results);
    }

    private static void collectTaskSpecificCsvData(List<String[]> sccCsvData,
                                                 List<String[]> topoCsvData,
                                                 List<String[]> spCsvData,
                                                 List<String[]> cpCsvData,
                                                 List<String[]> summaryCsvData,
                                                 GraphResults r) {

        int vertices = r.graph.getN();
        int edges = r.graph.getAllEdges().size();
        String density = r.graphData.getDensity();
        String variant = r.graphData.getVariant();
        int graphId = r.id;

        sccCsvData.add(new String[]{
            String.valueOf(graphId),
            String.valueOf(vertices),
            String.valueOf(edges),
            density,
            variant,
            String.valueOf(r.tarjanMetrics.getTotalOperations()),
            String.valueOf(r.sccResult.getNumComponents()),
            String.format("%.3f", r.tarjanMetrics.getExecutionTimeMs())
        });

        topoCsvData.add(new String[]{
            String.valueOf(graphId),
            String.valueOf(vertices),
            String.valueOf(edges),
            density,
            variant,
            String.valueOf(r.topoMetrics.getTotalOperations()),
            String.format("%.3f", r.topoMetrics.getExecutionTimeMs())
        });

        double spLength = 0;
        if (r.spResult != null) {
            int dagSource = r.sccResult.getComponentId()[r.source];
            List<Integer> spPath = buildPathFromSource(r.spResult, dagSource);
            spLength = calculatePathLength(r.dag, spPath);
        }

        spCsvData.add(new String[]{
            String.valueOf(graphId),
            String.valueOf(vertices),
            String.valueOf(edges),
            density,
            variant,
            String.valueOf(r.spMetrics.getTotalOperations()),
            String.format("%.2f", spLength),
            String.format("%.3f", r.spMetrics.getExecutionTimeMs())
        });

        double cpLength = r.cpResult != null ? r.cpResult.getLength() : 0;

        cpCsvData.add(new String[]{
            String.valueOf(graphId),
            String.valueOf(vertices),
            String.valueOf(edges),
            density,
            variant,
            String.valueOf(r.lpMetrics.getTotalOperations()),
            String.format("%.2f", cpLength),
            String.format("%.3f", r.lpMetrics.getExecutionTimeMs())
        });

        long totalOps = r.tarjanMetrics.getTotalOperations() +
                       r.topoMetrics.getTotalOperations() +
                       r.spMetrics.getTotalOperations() +
                       r.lpMetrics.getTotalOperations();

        double totalTime = r.tarjanMetrics.getExecutionTimeMs() +
                          r.topoMetrics.getExecutionTimeMs() +
                          r.spMetrics.getExecutionTimeMs() +
                          r.lpMetrics.getExecutionTimeMs();

        summaryCsvData.add(new String[]{
            String.valueOf(graphId),
            String.valueOf(vertices),
            String.valueOf(edges),
            density,
            variant,
            String.valueOf(r.sccResult.getNumComponents()),
            String.format("%.2f", spLength),
            String.format("%.2f", cpLength),
            String.valueOf(totalOps),
            String.format("%.3f", totalTime)
        });
    }

    private static void writeTaskCsv(String filepath, List<String[]> csvData, String header) throws IOException {
        try (FileWriter writer = new FileWriter(filepath)) {
            writer.write(header + "\n");

            for (String[] row : csvData) {
                writer.write(String.join(";", row) + "\n");
            }
        }
    }


    private static void cleanMemory() {
        System.gc();
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private static void warmupJIT() {
        try {
            List<GraphData> warmupGraphs = InputReader.loadAllGraphs("data/input_dense.json");

            for (int iteration = 0; iteration < 3; iteration++) {

                for (GraphData gd : warmupGraphs) {
                    DirectedGraph graph = gd.getGraph();
                    int source = gd.getSource();

                    TarjanSCC tarjan = new TarjanSCC();
                    SCCResult sccResult = tarjan.findSCC(graph);

                    CondensationGraph condensation = new CondensationGraph(graph, sccResult);
                    DirectedGraph dag = condensation.getCondensation();

                    KahnTopologicalSort topoSort = new KahnTopologicalSort();
                    TopologicalSortResult topoResult = topoSort.sort(dag);

                    DAGShortestPath shortestPath = new DAGShortestPath();
                    int dagSource = sccResult.getComponentId()[source];
                    shortestPath.computeShortestPaths(dag, dagSource);

                    DAGLongestPath longestPath = new DAGLongestPath();
                    longestPath.findCriticalPath(dag);
                }
            }
        } catch (Exception e) {
            System.err.println("Warmup failed: " + e.getMessage());
        }
    }

    private static void collectCsvData(List<String[]> csvData, GraphResults r) {
        int id = r.id;

        int vertices = r.graph.getN();
        int edges = r.graph.getAllEdges().size();

        String density = r.graphData.getDensity();
        String variant = r.graphData.getVariant();

        long spTotalOps = r.tarjanMetrics.getTotalOperations() +
                r.topoMetrics.getTotalOperations() +
                r.spMetrics.getTotalOperations();
        double spTotalTime = r.tarjanMetrics.getExecutionTimeMs() +
                r.topoMetrics.getExecutionTimeMs() +
                r.spMetrics.getExecutionTimeMs();

        long lpTotalOps = r.tarjanMetrics.getTotalOperations() +
                r.topoMetrics.getTotalOperations() +
                r.lpMetrics.getTotalOperations();
        double lpTotalTime = r.tarjanMetrics.getExecutionTimeMs() +
                r.topoMetrics.getExecutionTimeMs() +
                r.lpMetrics.getExecutionTimeMs();

        if (r.spResult != null) {
            int dagSource = r.sccResult.getComponentId()[r.source];
            List<Integer> spPath = buildPathFromSource(r.spResult, dagSource);
            double spLength = calculatePathLength(r.dag, spPath);

            csvData.add(new String[]{
                    String.valueOf(id),
                    String.valueOf(vertices),
                    String.valueOf(edges),
                    density,
                    variant,
                    "DAG-ShortestPath",
                    String.valueOf(spTotalOps),
                    String.format("%.2f", spLength),
                    String.format("%.3f", spTotalTime)
            });
        }

        csvData.add(new String[]{
                String.valueOf(id),
                String.valueOf(vertices),
                String.valueOf(edges),
                density,
                variant,
                "DAG-LongestPath",
                String.valueOf(lpTotalOps),
                String.format("%.2f", r.cpResult.getLength()),
                String.format("%.3f", lpTotalTime)

        });
    }

    private static JsonObject buildJsonResult(GraphResults r) {
        JsonObject graphJson = new JsonObject();
        graphJson.addProperty("graph_id", r.id);

        JsonObject inputStats = new JsonObject();
        inputStats.addProperty("vertices", r.graph.getN());
        inputStats.addProperty("edges", r.graph.getAllEdges().size());
        inputStats.addProperty("density", r.graphData.getDensity());
        inputStats.addProperty("variant", r.graphData.getVariant());
        inputStats.addProperty("source", r.source);
        graphJson.add("input_stats", inputStats);

        JsonObject tarjanJson = new JsonObject();
        tarjanJson.addProperty("num_sccs", r.sccResult.getNumComponents());
        JsonArray sccsArray = new JsonArray();
        JsonArray sizesArray = new JsonArray();
        for (List<Integer> scc : r.sccResult.getComponents()) {
            JsonArray sccArray = new JsonArray();
            for (Integer v : scc) sccArray.add(v);
            sccsArray.add(sccArray);
            sizesArray.add(scc.size());
        }
        tarjanJson.add("sccs", sccsArray);
        tarjanJson.add("sizes", sizesArray);
        tarjanJson.addProperty("operations_count", r.tarjanMetrics.getTotalOperations());
        tarjanJson.addProperty("execution_time_ms", r.tarjanMetrics.getExecutionTimeMs());
        graphJson.add("tarjan_scc", tarjanJson);

        JsonObject condensationJson = new JsonObject();
        condensationJson.addProperty("vertices", r.dag.getN());
        condensationJson.addProperty("edges", r.dag.getAllEdges().size());
        graphJson.add("condensation_graph", condensationJson);

        JsonObject topoJson = new JsonObject();
        JsonArray componentOrder = new JsonArray();
        JsonArray vertexOrder = new JsonArray();
        List<List<Integer>> sccs = r.sccResult.getComponents();

        for (Integer v : r.topoResult.getOrder()){
            componentOrder.add(v);
            for (Integer vertex : sccs.get(v)) {
                vertexOrder.add(vertex);
            }
        }
        topoJson.add("component_order", componentOrder);
        topoJson.add("vertex_order", vertexOrder);
        topoJson.addProperty("operations_count", r.topoMetrics.getTotalOperations());
        topoJson.addProperty("execution_time_ms", r.topoMetrics.getExecutionTimeMs());
        graphJson.add("topological_sort", topoJson);

        if (r.spResult != null) {
            JsonObject spJson = new JsonObject();
            spJson.addProperty("source", r.source);
            int dagSource = r.sccResult.getComponentId()[r.source];
            spJson.addProperty("source_scc", dagSource);
            List<Integer> spPath = buildPathFromSource(r.spResult, dagSource);
            if (!spPath.isEmpty()) {
                int targetSCC = spPath.get(spPath.size() - 1);
                spJson.addProperty("target_scc", targetSCC);
            }
            JsonArray spPathArray = new JsonArray();
            for (Integer v : spPath) spPathArray.add(v);
            spJson.add("path", spPathArray);

            double pathLength = 0;
            JsonArray spEdges = new JsonArray();
            for (int i = 0; i < spPath.size() - 1; i++) {
                int u = spPath.get(i);
                int v = spPath.get(i + 1);
                for (Edge e : r.dag.getAdjacent(u)) {
                    if (e.getTo() == v) {
                        JsonObject edgeObj = new JsonObject();
                        edgeObj.addProperty("u", u);
                        edgeObj.addProperty("v", v);
                        edgeObj.addProperty("w", e.getWeight());
                        spEdges.add(edgeObj);
                        pathLength += e.getWeight();
                        break;
                    }
                }
            }
            spJson.add("edges", spEdges);
            spJson.addProperty("path_length", pathLength);
            spJson.addProperty("operations_count", r.spMetrics.getTotalOperations());
            spJson.addProperty("execution_time_ms", r.spMetrics.getExecutionTimeMs());

            long totalOps = r.tarjanMetrics.getTotalOperations() +
                    r.topoMetrics.getTotalOperations() +
                    r.spMetrics.getTotalOperations();
            double totalTime = r.tarjanMetrics.getExecutionTimeMs() +
                    r.topoMetrics.getExecutionTimeMs() +
                    r.spMetrics.getExecutionTimeMs();
            spJson.addProperty("total_operations_count", totalOps);
            spJson.addProperty("total_execution_time_ms", totalTime);

            graphJson.add("shortest_path", spJson);
        }

        JsonObject lpJson = new JsonObject();
        lpJson.addProperty("critical_path_length", r.cpResult.getLength());

        List<Integer> cpPath = r.cpResult.getCriticalPath();
        if (cpPath != null) {
            JsonArray cpArray = new JsonArray();
            for (Integer v : cpPath) cpArray.add(v);
            lpJson.add("critical_path", cpArray);

            JsonArray lpEdges = new JsonArray();
            for (int i = 0; i < cpPath.size() - 1; i++) {
                int u = cpPath.get(i);
                int v = cpPath.get(i + 1);
                for (Edge e : r.dag.getAdjacent(u)) {
                    if (e.getTo() == v) {
                        JsonObject edgeObj = new JsonObject();
                        edgeObj.addProperty("u", u);
                        edgeObj.addProperty("v", v);
                        edgeObj.addProperty("w", e.getWeight());
                        lpEdges.add(edgeObj);
                        break;
                    }
                }
            }
            lpJson.add("edges", lpEdges);
        }

        lpJson.addProperty("operations_count", r.lpMetrics.getTotalOperations());
        lpJson.addProperty("execution_time_ms", r.lpMetrics.getExecutionTimeMs());

        long totalOps = r.tarjanMetrics.getTotalOperations() +
                r.topoMetrics.getTotalOperations() +
                r.lpMetrics.getTotalOperations();
        double totalTime = r.tarjanMetrics.getExecutionTimeMs() +
                r.topoMetrics.getExecutionTimeMs() +
                r.lpMetrics.getExecutionTimeMs();
        lpJson.addProperty("total_operations_count", totalOps);
        lpJson.addProperty("total_execution_time_ms", totalTime);

        graphJson.add("longest_path", lpJson);

        return graphJson;
    }

    private static List<Integer> buildPathFromSource(PathResult pathResult, int source) {
        List<Integer> path = new ArrayList<>();
        double[] distances = pathResult.getDistances();
        int[] parent = pathResult.getParent();

        for (int i = 0; i < distances.length; i++) {
            if (distances[i] != Double.POSITIVE_INFINITY && distances[i] != 0) {
                List<Integer> toVertex = new ArrayList<>();
                for (int v = i; v != -1; v = parent[v]) {
                    toVertex.add(v);
                }
                java.util.Collections.reverse(toVertex);
                if (toVertex.size() > path.size()) {
                    path = toVertex;
                }
            }
        }

        if (path.isEmpty()) {
            path.add(source);
        }

        return path;
    }

    private static double calculatePathLength(DirectedGraph dag, List<Integer> path) {
        double length = 0;
        for (int i = 0; i < path.size() - 1; i++) {
            int u = path.get(i);
            int v = path.get(i + 1);
            for (Edge e : dag.getAdjacent(u)) {
                if (e.getTo() == v) {
                    length += e.getWeight();
                    break;
                }
            }
        }
        return length;
    }

    private static void writeJson(String filepath, JsonArray results) throws IOException {
        JsonObject root = new JsonObject();
        root.add("results", results);
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        try (FileWriter writer = new FileWriter(filepath)) {
            gson.toJson(root, writer);
        }
    }

    private static class GraphResults {
        int id;
        DirectedGraph graph;
        GraphData graphData;
        int source;
        SCCResult sccResult;
        DirectedGraph dag;
        TopologicalSortResult topoResult;
        PathResult spResult;
        CriticalPathResult cpResult;
        Metrics tarjanMetrics;
        Metrics topoMetrics;
        Metrics spMetrics;
        Metrics lpMetrics;
    }
}
