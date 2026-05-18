package pl.graph;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Fruchterman {

    private final double size;
    private final double area;
    private final double k;
    private final int iterations;
    private final double initialTemperature;
    private double temperature;

    public Fruchterman(double size, int iterations, Graph graph) {
        this.size = size;
        this.area = size * size;
        this.iterations = Math.max(iterations, 1);
        this.k = Math.sqrt(area / Math.max(1, graph.getNodes().size()));
        this.initialTemperature = size / 10.0;
        this.temperature = initialTemperature;
    }

    public void layout(Graph graph) {
        graph.initializeRandomPositions(size, size);

        for (int iteration = 0; iteration < iterations; iteration++) {
            for (Node node : graph.getNodes()) {
                node.resetDisplacement();
            }

            for (int i = 0; i < graph.getNodes().size(); i++) {
                Node nodeA = graph.getNodes().get(i);
                for (int j = i + 1; j < graph.getNodes().size(); j++) {
                    Node nodeB = graph.getNodes().get(j);
                    applyRepulsiveForce(nodeA, nodeB);
                }
            }

            for (Edge edge : graph.getEdges()) {
                applyAttractiveForce(edge);
            }

            for (Node node : graph.getNodes()) {
                updatePosition(node);
            }

            temperature = cool(iteration);
        }
    }

    private void applyRepulsiveForce(Node a, Node b) {
        double dx = a.getX() - b.getX();
        double dy = a.getY() - b.getY();
        double distance = Math.hypot(dx, dy);

        if (distance < 0.01) {
            dx = (new Random().nextDouble() - 0.5) * 0.1;
            dy = (new Random().nextDouble() - 0.5) * 0.1;
            distance = Math.hypot(dx, dy);
        }

        double force = repulsiveForce(distance);
        double deltaX = (dx / distance) * force;
        double deltaY = (dy / distance) * force;

        a.addDisplacement(deltaX, deltaY);
        b.addDisplacement(-deltaX, -deltaY);
    }

    private void applyAttractiveForce(Edge edge) {
        Node source = edge.getSource();
        Node target = edge.getTarget();

        double dx = source.getX() - target.getX();
        double dy = source.getY() - target.getY();
        double distance = Math.hypot(dx, dy);
        distance = Math.max(distance, 0.01);

        double force = attractiveForce(distance);
        double deltaX = (dx / distance) * force;
        double deltaY = (dy / distance) * force;

        source.addDisplacement(-deltaX, -deltaY);
        target.addDisplacement(deltaX, deltaY);
    }

    private void updatePosition(Node node) {
        double dx = node.getDx();
        double dy = node.getDy();
        double distance = Math.hypot(dx, dy);

        if (distance > 0) {
            double limitedDistance = Math.min(distance, temperature);
            node.setX(node.getX() + (dx / distance) * limitedDistance);
            node.setY(node.getY() + (dy / distance) * limitedDistance);
        }

        node.setX(Math.min(size, Math.max(0, node.getX())));
        node.setY(Math.min(size, Math.max(0, node.getY())));
    }

    private double repulsiveForce(double distance) {
        return (k * k) / distance;
    }

    private double attractiveForce(double distance) {
        return (distance * distance) / k;
    }

    private double cool(int iteration) {
        return initialTemperature * (1.0 - (double) iteration / iterations);
    }

    public static class Graph {

        private final List<Node> nodes = new ArrayList<>();
        private final List<Edge> edges = new ArrayList<>();
        private final Random random = new Random();

        public Node addNode(String label) {
            Node node = new Node(label);
            nodes.add(node);
            return node;
        }

        public void addEdge(String sourceLabel, String targetLabel) {
            Node source = getNodeByLabel(sourceLabel);
            Node target = getNodeByLabel(targetLabel);

            if (source == null || target == null) {
                throw new IllegalArgumentException("Both nodes must exist before adding an edge.");
            }

            edges.add(new Edge(source, target));
        }

        public Node getNodeByLabel(String label) {
            for (Node node : nodes) {
                if (node.getLabel().equals(label)) {
                    return node;
                }
            }
            return null;
        }

        public List<Node> getNodes() {
            return nodes;
        }

        public List<Edge> getEdges() {
            return edges;
        }

        public void initializeRandomPositions(double width, double height) {
            for (Node node : nodes) {
                node.setX(random.nextDouble() * width);
                node.setY(random.nextDouble() * height);
                node.resetDisplacement();
            }
        }
    }

    public static class Node {

        private final String label;
        private double x;
        private double y;
        private double dx;
        private double dy;

        public Node(String label) {
            this.label = label;
        }

        public String getLabel() {
            return label;
        }

        public double getX() {
            return x;
        }

        public void setX(double x) {
            this.x = x;
        }

        public double getY() {
            return y;
        }

        public void setY(double y) {
            this.y = y;
        }

        public double getDx() {
            return dx;
        }

        public double getDy() {
            return dy;
        }

        public void resetDisplacement() {
            dx = 0;
            dy = 0;
        }

        public void addDisplacement(double dx, double dy) {
            this.dx += dx;
            this.dy += dy;
        }
    }

    public static class Edge {

        private final Node source;
        private final Node target;

        public Edge(Node source, Node target) {
            this.source = source;
            this.target = target;
        }

        public Node getSource() {
            return source;
        }

        public Node getTarget() {
            return target;
        }
    }
}
