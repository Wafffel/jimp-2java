package pl.graph;

public class Edge {
    public int firstNodeIndex;
    public int secondNodeIndex;
    public double weight;
    public String label;

    public Edge(int firstNodeIndex, int secondNodeIndex, double weight, String label) {
        this.firstNodeIndex = firstNodeIndex;
        this.secondNodeIndex = secondNodeIndex;
        this.weight = weight;
        this.label = label != null ? label : "";
    }

    @Override
    public String toString() {
        return String.format("Edge(%d -> %d, weight=%.2f, label=%s)", 
            firstNodeIndex, secondNodeIndex, weight, label);
    }
}
