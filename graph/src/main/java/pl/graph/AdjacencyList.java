package pl.graph;

public class AdjacencyList {
    public Neighbor[] adjacencyList;
    public int nodesCount;
    public int[] degrees;
    public int[] nodeIds;

    public AdjacencyList(int nodesCount) {
        this.nodesCount = nodesCount;
        this.adjacencyList = new Neighbor[nodesCount];
        this.degrees = new int[nodesCount];
        this.nodeIds = new int[nodesCount];
    }

    public static AdjacencyList createAdjacencyList(Graph graph) {
        AdjacencyList adjList = new AdjacencyList(graph.nodesCount);

        for (int i = 0; i < graph.nodesCount; i++) {
            adjList.nodeIds[i] = graph.nodes[i].id;
        }

        // Dla każdej krawędzi dodajemy obie relacje 
        for (int i = 0; i < graph.edgesCount; i++) {
            int firstIndex = graph.edges[i].firstNodeIndex;
            int secondIndex = graph.edges[i].secondNodeIndex;
            double weight = graph.edges[i].weight;

            // Dodaj drugą stronę do pierwszego wierzchołka
            Neighbor neighbor1 = new Neighbor(secondIndex, weight);
            neighbor1.next = adjList.adjacencyList[firstIndex];
            adjList.adjacencyList[firstIndex] = neighbor1;

            // Dodaj pierwszą stronę do drugiego wierzchołka
            Neighbor neighbor2 = new Neighbor(firstIndex, weight);
            neighbor2.next = adjList.adjacencyList[secondIndex];
            adjList.adjacencyList[secondIndex] = neighbor2;

            adjList.degrees[firstIndex]++;
            adjList.degrees[secondIndex]++;
        }

        return adjList;
    }
}
