package pl.graph;

import java.util.Random;

public class Fruchterman {

    private final double size;
    private final double area;
    private final double k;
    private final int iterations;
    private final double initialTemperature;
    private double temperature;
    private final Random random = new Random();

    public Fruchterman(double size, int iterations, Graph graph) {
        this.size = size;
        this.area = size * size;
        this.iterations = Math.max(iterations, 1);
        int nodeCount = graph == null ? 0 : graph.nodesCount;
        this.k = Math.sqrt(area / Math.max(1, nodeCount));
        this.initialTemperature = size / 10.0;
        this.temperature = initialTemperature;
    }

    public void layout(Graph graph) {
        if (graph == null || graph.nodesCount <= 0) {
            return;
        }

        temperature = initialTemperature;
        initializeRandomPositions(graph);

        double[] displacementX = new double[graph.nodesCount];
        double[] displacementY = new double[graph.nodesCount];

        for (int iteration = 0; iteration < iterations; iteration++) {
            for (int i = 0; i < graph.nodesCount; i++) {
                displacementX[i] = 0.0;
                displacementY[i] = 0.0;
            }

            for (int i = 0; i < graph.nodesCount; i++) {
                Node nodeA = graph.nodes[i];
                for (int j = i + 1; j < graph.nodesCount; j++) {
                    Node nodeB = graph.nodes[j];
                    applyRepulsiveForce(nodeA, nodeB, displacementX, displacementY, i, j);
                }
            }

            for (int i = 0; i < graph.edgesCount; i++) {
                applyAttractiveForce(graph, graph.edges[i], displacementX, displacementY);
            }

            for (int i = 0; i < graph.nodesCount; i++) {
                updatePosition(graph.nodes[i], displacementX[i], displacementY[i]);
            }

            temperature = cool(iteration);
        }
    }

    private void initializeRandomPositions(Graph graph) {
        for (int i = 0; i < graph.nodesCount; i++) {
            Node node = graph.nodes[i];
            node.x = random.nextDouble() * size;
            node.y = random.nextDouble() * size;
        }
    }

    private void applyRepulsiveForce(Node a, Node b, double[] displacementX, double[] displacementY,
                                     int indexA, int indexB) {
        double dx = a.x - b.x;
        double dy = a.y - b.y;
        double distance = Math.hypot(dx, dy);

        if (distance < 0.01) {
            dx = (random.nextDouble() - 0.5) * 0.1;
            dy = (random.nextDouble() - 0.5) * 0.1;
            distance = Math.hypot(dx, dy);
        }

        double force = repulsiveForce(distance);
        double deltaX = (dx / distance) * force;
        double deltaY = (dy / distance) * force;

        displacementX[indexA] += deltaX;
        displacementY[indexA] += deltaY;
        displacementX[indexB] -= deltaX;
        displacementY[indexB] -= deltaY;
    }

    private void applyAttractiveForce(Graph graph, Edge edge,
                                      double[] displacementX, double[] displacementY) {
        int sourceIndex = edge.firstNodeIndex;
        int targetIndex = edge.secondNodeIndex;
        if (sourceIndex < 0 || targetIndex < 0
                || sourceIndex >= graph.nodesCount || targetIndex >= graph.nodesCount) {
            return;
        }

        Node source = graph.nodes[sourceIndex];
        Node target = graph.nodes[targetIndex];

        double dx = source.x - target.x;
        double dy = source.y - target.y;
        double distance = Math.hypot(dx, dy);
        distance = Math.max(distance, 0.01);

        double force = attractiveForce(distance) * edge.weight;
        double deltaX = (dx / distance) * force;
        double deltaY = (dy / distance) * force;

        displacementX[sourceIndex] -= deltaX;
        displacementY[sourceIndex] -= deltaY;
        displacementX[targetIndex] += deltaX;
        displacementY[targetIndex] += deltaY;
    }

    private void updatePosition(Node node, double dx, double dy) {
        double distance = Math.hypot(dx, dy);

        if (distance > 0) {
            double limitedDistance = Math.min(distance, temperature);
            node.x += (dx / distance) * limitedDistance;
            node.y += (dy / distance) * limitedDistance;
        }

        node.x = Math.min(size, Math.max(0, node.x));
        node.y = Math.min(size, Math.max(0, node.y));
    }

    private double repulsiveForce(double distance) {
        return (k * k) / distance;
    }

    private double attractiveForce(double distance) {
        return (distance * distance) / k;
    }

    private double cool(int iteration) {
        double cooled = initialTemperature * (1.0 - (double) iteration / iterations);
        return Math.max(0.0, cooled);
    }
}
