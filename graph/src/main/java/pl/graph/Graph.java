package pl.graph;

import java.io.*;
import java.util.*;

public class Graph {
    public Node[] nodes;
    public int nodesCount;
    public Edge[] edges;
    public int edgesCount;

    private record ParsedEdge(String label, int firstNodeId, int secondNodeId, double weight) {
    }

    public Graph(int nodesCount, int edgesCount) {
        this.nodesCount = nodesCount;
        this.edgesCount = edgesCount;
        this.nodes = new Node[nodesCount];
        this.edges = new Edge[edgesCount];
    }

    public static Graph loadGraph(String path) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(path))) {
            String line;
            Set<Integer> uniqueNodeIds = new LinkedHashSet<>();
            List<ParsedEdge> edges = new ArrayList<>();

            // Przejście po pliku: zbieramy unikalne ID węzłów oraz listę krawędzi
            while ((line = reader.readLine()) != null) {
                line = line.trim();

                // Pomijanie białych linii i komentarzy
                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }

                int commentIndex = line.indexOf('#');
                if (commentIndex >= 0) {
                    line = line.substring(0, commentIndex).trim();
                }

                if (line.isEmpty()) {
                    continue;
                }

                String[] parts = line.split("\\s+");
                if (parts.length < 3) {
                    continue;
                }

                try {
                    String label = parts[0];
                    if (label.length() > 32) {
                        label = label.substring(0, 32);
                    }

                    int firstNode = Integer.parseInt(parts[1]);
                    int secondNode = Integer.parseInt(parts[2]);
                    if (firstNode < 0 || secondNode < 0) {
                        continue;
                    }

                    double weight = 1.0;
                    if (parts.length >= 4) {
                        try {
                            weight = Double.parseDouble(parts[3]);
                        } catch (NumberFormatException ignored) {
                            // domyślna waga
                        }
                    }

                    uniqueNodeIds.add(firstNode);
                    uniqueNodeIds.add(secondNode);
                    edges.add(new ParsedEdge(label, firstNode, secondNode, weight));
                } catch (NumberFormatException e) {
                    // ignoruj linię
                }
            }

            int nodesCount = uniqueNodeIds.size();
            int edgesCount = edges.size();

            if (nodesCount == 0 || edgesCount == 0) {
                throw new IllegalArgumentException("Error: Graph must have at least one node and one edge.");
            }

            Graph graph = new Graph(nodesCount, edgesCount);

            // Tworzenie tablica wierzchołków i sortowanie po ID
            int idx = 0;
            for (int nodeId : uniqueNodeIds) {
                graph.nodes[idx] = new Node(nodeId);
                idx++;
            }
            Arrays.sort(graph.nodes);

            Map<Integer, Integer> nodeIdToIndex = new HashMap<>(Math.max(16, graph.nodesCount * 2));
            for (int i = 0; i < graph.nodesCount; i++) {
                nodeIdToIndex.put(graph.nodes[i].id, i);
            }

            // Mapowanie krawędzi na indeksy w tablicy wierzchołków.
            for (int i = 0; i < edges.size(); i++) {
                ParsedEdge edge = edges.get(i);
                Integer firstIndex = nodeIdToIndex.get(edge.firstNodeId);
                Integer secondIndex = nodeIdToIndex.get(edge.secondNodeId);
                if (firstIndex == null || secondIndex == null) {
                    throw new IllegalArgumentException("Error: Node ID not found");
                }

                graph.edges[i] = new Edge(firstIndex, secondIndex, edge.weight, edge.label);
            }

            return graph;
        } catch (FileNotFoundException e) {
            throw new IOException("Error: Cannot open input file: " + path, e);
        } catch (IOException e) {
            throw new IOException("Error: Cannot read file: " + path, e);
        }
    }

    public static ExitCodes saveGraphAsText(Graph graph, String path) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(path))) {
            for (int i = 0; i < graph.nodesCount; i++) {
                writer.printf(Locale.ROOT, "%d %.6f %.6f%n", graph.nodes[i].id, graph.nodes[i].x, graph.nodes[i].y);
            }
            return ExitCodes.SUCCESS;
        } catch (IOException e) {
            System.err.println("Error: Cannot write to file: " + path);
            return ExitCodes.OUTPUT_WRITE_ERROR;
        }
    }

    public static ExitCodes saveGraphAsBinary(Graph graph, String path) {
        try (DataOutputStream dos = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(path)))) {
            dos.writeInt(Integer.reverseBytes(graph.nodesCount));
            for (int i = 0; i < graph.nodesCount; i++) {
                dos.writeInt(Integer.reverseBytes(graph.nodes[i].id));
                dos.writeLong(Long.reverseBytes(Double.doubleToLongBits(graph.nodes[i].x)));
                dos.writeLong(Long.reverseBytes(Double.doubleToLongBits(graph.nodes[i].y)));
            }
            return ExitCodes.SUCCESS;
        } catch (IOException e) {
            System.err.println("Error: Cannot write to file: " + path);
            return ExitCodes.OUTPUT_WRITE_ERROR;
        }
    }

    public static int getNodeIndex(Graph graph, int nodeId) {
        Node key = new Node(nodeId);
        int result = Arrays.binarySearch(graph.nodes, key);
        return result < 0 ? -1 : result;
    }
}
