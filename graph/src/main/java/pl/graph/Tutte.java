package pl.graph;

import java.util.Arrays;

public class Tutte {

    public static ExitCodes runTutte(Graph graph, double size, int maxIterations) {
        if (graph == null || graph.nodesCount <= 0) {
            return ExitCodes.SUCCESS;
        }

        // Dla bardzo małych grafów unikamy przypadków brzegowych.
        if (graph.nodesCount < 4) {
            placeSmallGraph(graph);
            scaleGraph(graph, size);
            return ExitCodes.SUCCESS;
        }

        AdjacencyList adjList = AdjacencyList.createAdjacencyList(graph);

        int[] boundary = findBoundaryCycle(adjList);
        if (boundary == null || boundary.length < 3) {
            return ExitCodes.TUTTE_ASSUMPTIONS_ERROR;
        }

        placeBoundaryOnUnitCircle(graph, boundary);

        boolean[] isBoundary = new boolean[graph.nodesCount];
        for (int idx : boundary) {
            isBoundary[idx] = true;
        }

        int[] internalIndex = new int[graph.nodesCount];
        Arrays.fill(internalIndex, -1);
        int internalCount = 0;
        for (int i = 0; i < graph.nodesCount; i++) {
            if (!isBoundary[i]) {
                internalIndex[i] = internalCount++;
            }
        }

        if (internalCount == 0) {
            scaleGraph(graph, size);
            return ExitCodes.SUCCESS;
        }

        double[][] A = new double[internalCount][internalCount];
        double[] bx = new double[internalCount];
        double[] by = new double[internalCount];

        // Budowa zredukowanej macierzy Laplace'a (LI) i wektorów wyrazów wolnych.
        for (int v = 0; v < graph.nodesCount; v++) {
            if (isBoundary[v]) {
                continue;
            }
            int row = internalIndex[v];

            int degree = 0;
            for (Neighbor neighbor = adjList.adjacencyList[v]; neighbor != null; neighbor = neighbor.next) {
                int u = neighbor.nodeIndex;
                degree++;
                if (isBoundary[u]) {
                    bx[row] += graph.nodes[u].x;
                    by[row] += graph.nodes[u].y;
                } else {
                    int col = internalIndex[u];
                    if (col >= 0) {
                        A[row][col] -= 1.0;
                    }
                }
            }

            if (degree <= 0) {
                return ExitCodes.TUTTE_ASSUMPTIONS_ERROR;
            }

            A[row][row] += degree;
        }

        double[] x = gaussianElimination(deepCopy(A), bx);
        double[] y = gaussianElimination(deepCopy(A), by);
        if (x == null || y == null) {
            return ExitCodes.NUMERICAL_ERROR;
        }

        for (int v = 0; v < graph.nodesCount; v++) {
            int row = internalIndex[v];
            if (row >= 0) {
                graph.nodes[v].x = x[row];
                graph.nodes[v].y = y[row];
            }
        }

        scaleGraph(graph, size);
        return ExitCodes.SUCCESS;
    }

    // Heurystyczne szukanie cyklu brzegowego z zapasowym wyborem wierzcholkow.
    private static int[] findBoundaryCycle(AdjacencyList adjList) {
        int[] cycle = findBoundaryCyclePerimeter(adjList);
        if (cycle != null) {
            return cycle;
        }
        return findBoundaryCycleBackup(adjList);
    }

    // Heurystyczne szukanie cyklu brzegowego (zewnetrznej sciany).
    private static int[] findBoundaryCyclePerimeter(AdjacencyList adjList) {
        int n = adjList.nodesCount;
        int startNode = 0;
        int minDeg = adjList.degrees[0];
        for (int i = 1; i < n; i++) {
            int deg = adjList.degrees[i];
            if (deg < minDeg) {
                minDeg = deg;
                startNode = i;
            }
        }

        int[] path = new int[n];
        int current = startNode;
        int previous = -1;
        int count = 0;
        path[count++] = current;

        while (count < n) {
            int bestNext = -1;
            int minCommon = Integer.MAX_VALUE;
            int minDegVal = Integer.MAX_VALUE;
            boolean found = false;

            // zamknięcie cyklu
            if (count > 2) {
                for (Neighbor check = adjList.adjacencyList[current]; check != null; check = check.next) {
                    if (check.nodeIndex == startNode) {
                        return Arrays.copyOf(path, count);
                    }
                }
            }

            for (Neighbor neighbor = adjList.adjacencyList[current]; neighbor != null; neighbor = neighbor.next) {
                int v = neighbor.nodeIndex;
                if (v == previous) {
                    continue;
                }

                boolean visited = false;
                for (int j = 0; j < count; j++) {
                    if (path[j] == v) {
                        visited = true;
                        break;
                    }
                }
                if (visited) {
                    continue;
                }

                int common = countCommonNeighbors(current, v, adjList);
                int deg = adjList.degrees[v];
                if (common < minCommon || (common == minCommon && deg < minDegVal)) {
                    minCommon = common;
                    minDegVal = deg;
                    bestNext = v;
                    found = true;
                }
            }

            if (!found) {
                break;
            }

            previous = current;
            current = bestNext;
            path[count++] = current;
        }

        return null;
    }

    // Fallback: jesli nie mozna znalezc cyklu brzegowego, wybieramy 4 wierzcholki o najwyzszych stopniach.
    private static int[] findBoundaryCycleBackup(AdjacencyList adjList) {
        int n = adjList.nodesCount;
        if (n < 4) {
            return null;
        }

        int[] maxIndices = new int[] {0, 1, 2, 3};
        for (int i = 4; i < n; i++) {
            int minIdx = 0;
            for (int j = 1; j < 4; j++) {
                if (adjList.degrees[maxIndices[j]] < adjList.degrees[maxIndices[minIdx]]) {
                    minIdx = j;
                }
            }
            if (adjList.degrees[i] > adjList.degrees[maxIndices[minIdx]]) {
                maxIndices[minIdx] = i;
            }
        }

        return Arrays.copyOf(maxIndices, 4);
    }

    private static int countCommonNeighbors(int u, int v, AdjacencyList adj) {
        int common = 0;
        for (Neighbor nu = adj.adjacencyList[u]; nu != null; nu = nu.next) {
            for (Neighbor nv = adj.adjacencyList[v]; nv != null; nv = nv.next) {
                if (nu.nodeIndex == nv.nodeIndex) {
                    common++;
                }
            }
        }
        return common;
    }

    private static void placeBoundaryOnUnitCircle(Graph graph, int[] boundary) {
        int boundaryLen = boundary.length;
        for (int i = 0; i < boundaryLen; i++) {
            double angle = 2.0 * Math.PI * i / boundaryLen;
            int v = boundary[i];
            graph.nodes[v].x = Math.cos(angle);
            graph.nodes[v].y = Math.sin(angle);
        }
    }

    private static double[][] deepCopy(double[][] matrix) {
        double[][] copy = new double[matrix.length][];
        for (int i = 0; i < matrix.length; i++) {
            copy[i] = Arrays.copyOf(matrix[i], matrix[i].length);
        }
        return copy;
    }

    private static void scaleGraph(Graph graph, double size) {
        double scale = size / 2.0;
        for (int i = 0; i < graph.nodesCount; i++) {
            graph.nodes[i].x *= scale;
            graph.nodes[i].y *= scale;
        }
    }

    // Eliminacja Gaussa z częściowym wyborem elementu podstawowego (Partial Pivoting).
    private static double[] gaussianElimination(double[][] A, double[] b) {
        int n = b.length;
        if (A.length != n) {
            return null;
        }

        for (int k = 0; k < n; k++) {
            int pivot = k;
            for (int i = k + 1; i < n; i++) {
                if (Math.abs(A[i][k]) > Math.abs(A[pivot][k])) {
                    pivot = i;
                }
            }

            if (Math.abs(A[pivot][k]) < 1e-12) {
                return null;
            }

            if (pivot != k) {
                double[] tmpRow = A[k];
                A[k] = A[pivot];
                A[pivot] = tmpRow;

                double tmpB = b[k];
                b[k] = b[pivot];
                b[pivot] = tmpB;
            }

            for (int i = k + 1; i < n; i++) {
                double factor = A[i][k] / A[k][k];
                A[i][k] = 0.0;
                for (int j = k + 1; j < n; j++) {
                    A[i][j] -= factor * A[k][j];
                }
                b[i] -= factor * b[k];
            }
        }

        double[] x = new double[n];
        for (int i = n - 1; i >= 0; i--) {
            double sum = b[i];
            for (int j = i + 1; j < n; j++) {
                sum -= A[i][j] * x[j];
            }
            x[i] = sum / A[i][i];
        }
        return x;
    }

    private static void placeSmallGraph(Graph graph) {
        if (graph.nodesCount == 1) {
            graph.nodes[0].x = 0.0;
            graph.nodes[0].y = 0.0;
            return;
        }
        if (graph.nodesCount == 2) {
            graph.nodes[0].x = -1.0;
            graph.nodes[0].y = 0.0;
            graph.nodes[1].x = 1.0;
            graph.nodes[1].y = 0.0;
            return;
        }
        if (graph.nodesCount == 3) {
            double h = Math.sqrt(3.0) / 2.0;
            graph.nodes[0].x = 0.0;
            graph.nodes[0].y = 1.0;
            graph.nodes[1].x = -h;
            graph.nodes[1].y = -0.5;
            graph.nodes[2].x = h;
            graph.nodes[2].y = -0.5;
        }
    }
}
