package pl.graph;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class Main {

    private static final int DEFAULT_ITERATIONS = 200;
    private static final double DEFAULT_SIZE = 800.0;

    public static void main(String[] args) {
        if (args.length < 2) {
            printUsage();
            System.exit(ExitCodes.ARGUMENTS_ERROR.code());
        }

        String inputPath = args[0];
        String outputPath = args[1];
        boolean binaryOutput = false;
        String algorithm = "fruchterman";
        int iterations = DEFAULT_ITERATIONS;
        double size = DEFAULT_SIZE;

        for (int i = 2; i < args.length; i++) {
            switch (args[i]) {
                case "-a", "--algorithm" -> {
                    if (i + 1 >= args.length) {
                        System.err.println("Missing algorithm after " + args[i]);
                        printUsage();
                        System.exit(ExitCodes.ARGUMENTS_ERROR.code());
                    }
                    algorithm = args[++i].toLowerCase(Locale.ROOT);
                    if (!algorithm.equals("fruchterman") && !algorithm.equals("tutte")) {
                        System.err.println("Unsupported algorithm: " + algorithm);
                        printUsage();
                        System.exit(ExitCodes.UNKNOWN_ARGUMENT.code());
                    }
                }
                case "-i", "--iterations" -> {
                    if (i + 1 >= args.length) {
                        System.err.println("Missing iteration count after " + args[i]);
                        printUsage();
                        System.exit(ExitCodes.ARGUMENTS_ERROR.code());
                    }
                    iterations = parsePositiveInt(args[++i], "iterations");
                }
                case "-s", "--size" -> {
                    if (i + 1 >= args.length) {
                        System.err.println("Missing size after " + args[i]);
                        printUsage();
                        System.exit(ExitCodes.ARGUMENTS_ERROR.code());
                    }
                    size = parsePositiveDouble(args[++i], "size");
                }
                case "-b", "--binary" ->
                    binaryOutput = true;
                default -> {
                    System.err.println("Unknown argument: " + args[i]);
                    printUsage();
                    System.exit(ExitCodes.UNKNOWN_ARGUMENT.code());
                }
            }
        }

        Graph graph;
        try {
            graph = Graph.loadGraph(inputPath);
        } catch (IOException | IllegalArgumentException e) {
            System.err.println("Error: " + e.getMessage());
            System.exit(ExitCodes.FILE_ERROR.code());
            return;
        }

        ExitCodes result;
        if (algorithm.equals("tutte")) {
            result = Tutte.runTutte(graph, size, iterations);
            if (result != ExitCodes.SUCCESS) {
                System.err.println("Algorithm failed: " + result);
                System.exit(result.code());
            }
            result = binaryOutput ? Graph.saveGraphAsBinary(graph, outputPath) : Graph.saveGraphAsText(graph, outputPath);
        } else {
            Fruchterman.Graph fruchtermanGraph = convertToFruchtermanGraph(graph);
            Fruchterman fruchterman = new Fruchterman(size, iterations, fruchtermanGraph);
            fruchterman.layout(fruchtermanGraph);
            result = binaryOutput ? Graph.saveGraphAsBinary(convertToPlainGraph(fruchtermanGraph), outputPath) : saveText(fruchtermanGraph, outputPath);
        }

        if (result != ExitCodes.SUCCESS) {
            System.exit(result.code());
        }
    }

    private static int parsePositiveInt(String value, String name) {
        try {
            int result = Integer.parseInt(value);
            if (result <= 0) {
                throw new NumberFormatException();
            }
            return result;
        } catch (NumberFormatException e) {
            System.err.println("Invalid " + name + ": " + value);
            System.exit(ExitCodes.ARGUMENTS_ERROR.code());
            return -1;
        }
    }

    private static double parsePositiveDouble(String value, String name) {
        try {
            double result = Double.parseDouble(value);
            if (result <= 0) {
                throw new NumberFormatException();
            }
            return result;
        } catch (NumberFormatException e) {
            System.err.println("Invalid " + name + ": " + value);
            System.exit(ExitCodes.ARGUMENTS_ERROR.code());
            return -1;
        }
    }

    private static Fruchterman.Graph convertToFruchtermanGraph(Graph graph) {
        Fruchterman.Graph result = new Fruchterman.Graph();
        Map<Integer, String> indexToLabel = new HashMap<>();
        for (Node node : graph.nodes) {
            String label = String.valueOf(node.id);
            indexToLabel.put(node.id, label);
            result.addNode(label);
        }
        for (Edge edge : graph.edges) {
            String sourceLabel = indexToLabel.get(graph.nodes[edge.firstNodeIndex].id);
            String targetLabel = indexToLabel.get(graph.nodes[edge.secondNodeIndex].id);
            result.addEdge(sourceLabel, targetLabel);
        }
        return result;
    }

    private static Graph convertToPlainGraph(Fruchterman.Graph fruchtermanGraph) {
        int nodeCount = fruchtermanGraph.getNodes().size();
        Graph output = new Graph(nodeCount, 0);
        output.nodes = new Node[nodeCount];
        output.edges = new Edge[0];
        output.nodesCount = nodeCount;
        output.edgesCount = 0;

        for (int i = 0; i < nodeCount; i++) {
            Fruchterman.Node fNode = fruchtermanGraph.getNodes().get(i);
            int nodeId = parseNodeId(fNode.getLabel());
            output.nodes[i] = new Node(nodeId, fNode.getX(), fNode.getY());
        }
        return output;
    }

    private static int parseNodeId(String label) {
        try {
            return Integer.parseInt(label);
        } catch (NumberFormatException e) {
            return label.hashCode();
        }
    }

    private static ExitCodes saveText(Fruchterman.Graph fruchtermanGraph, String outputPath) {
        Path output = Path.of(outputPath);
        try (PrintWriter writer = new PrintWriter(Files.newBufferedWriter(output, StandardCharsets.UTF_8))) {
            for (Fruchterman.Node node : fruchtermanGraph.getNodes()) {
                writer.printf(Locale.ROOT, "%s %.6f %.6f%n", node.getLabel(), node.getX(), node.getY());
            }
            return ExitCodes.SUCCESS;
        } catch (IOException e) {
            System.err.println("Error: Cannot write output file: " + outputPath);
            return ExitCodes.OUTPUT_WRITE_ERROR;
        }
    }

    private static void printUsage() {
        System.out.println("Usage: java pl.graph.Main <input-file> <output-file> [options]");
        System.out.println("Options:");
        System.out.println("  -a, --algorithm <fruchterman|tutte>   choose layout algorithm (default: fruchterman)");
        System.out.println("  -i, --iterations <count>              iteration count for layout (default: 200)");
        System.out.println("  -s, --size <pixels>                   square layout edge length for Fruchterman and Tutte (default: 800)");
        System.out.println("  -b, --binary                          write binary output instead of text");
    }
}
