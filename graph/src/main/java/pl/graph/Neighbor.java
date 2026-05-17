package pl.graph;

public class Neighbor {
    public int nodeIndex;
    public double weight;
    public Neighbor next;

    public Neighbor(int nodeIndex, double weight) {
        this.nodeIndex = nodeIndex;
        this.weight = weight;
        this.next = null;
    }

    @Override
    public String toString() {
        return String.format("Neighbor(nodeIndex=%d, weight=%.2f)", nodeIndex, weight);
    }
}
