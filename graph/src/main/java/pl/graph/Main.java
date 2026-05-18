package pl.graph;

import java.io.IOException;
import java.util.Locale;

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
            Fruchterman fruchterman = new Fruchterman(size, iterations, graph);
            fruchterman.layout(graph);
            result = binaryOutput ? Graph.saveGraphAsBinary(graph, outputPath) : Graph.saveGraphAsText(graph, outputPath);
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

    private static void printUsage() {
        System.out.println("Usage: java pl.graph.Main <input-file> <output-file> [options]");
        System.out.println("Options:");
        System.out.println("  -a, --algorithm <fruchterman|tutte>   choose layout algorithm (default: fruchterman)");
        System.out.println("  -i, --iterations <count>              iteration count for layout (default: 200)");
        System.out.println("  -s, --size <pixels>                   square layout edge length for Fruchterman and Tutte (default: 800)");
        System.out.println("  -b, --binary                          write binary output instead of text");
    }
}
