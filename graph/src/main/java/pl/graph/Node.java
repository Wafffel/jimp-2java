package pl.graph;

public class Node implements Comparable<Node> {
    public int id;
    public double x;
    public double y;

    public Node(int id) {
        this.id = id;
        this.x = 0.0;
        this.y = 0.0;
    }

    public Node(int id, double x, double y) {
        this.id = id;
        this.x = x;
        this.y = y;
    }

    @Override
    public int compareTo(Node other) {
        return Integer.compare(this.id, other.id);
    }

    @Override
    public String toString() {
        return String.format("Node(id=%d, x=%.2f, y=%.2f)", id, x, y);
    }
}
