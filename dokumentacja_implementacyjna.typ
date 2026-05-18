#set page(
  paper: "a4",
  margin: (x: 2.5cm, y: 2.5cm),
  numbering: "1",
)

#set text(
  lang: "pl",
  size: 11pt,
)

#set heading(
  numbering: "1.1",
)

#show heading.where(level: 1): it => {
  pagebreak(weak: true)
  v(0.2cm)
  it
  v(0.15cm)
}

#show heading.where(level: 2): it => {
  v(0.15cm)
  it
  v(0.1cm)
}

#set par(justify: true, leading: 0.65em)

#align(center)[
  #v(3cm)
  #block(text(weight: 700, size: 20pt)[
    Dokumentacja implementacyjna
  ])
  #v(1cm)
  #block(text(size: 16pt)[
    System wizualizacji grafów planarnych
  ])
  #v(0.5cm)
  #block(text(size: 14pt)[
    Projekt w języku Java (Swing)
  ])
  #v(2cm)
  #block(text(size: 12pt)[
    Autorzy:\
    Krzysztof Wasilewski, Jakub Pietrzkiewicz\
    #v(0.3cm)
    Data: #datetime.today().display("[day].[month].[year]")
  ])
]

#pagebreak()

#outline()

#pagebreak()

= Wstęp

== Cel dokumentu

Celem dokumentacji implementacyjnej jest przedstawienie szczegółów budowy warstwy obliczeniowej aplikacji w języku Java do wyznaczania współrzędnych węzłów grafu planarnego. Dokument opisuje strukturę kodu, model danych, formaty wejścia i wyjścia oraz obsługę błędów na podstawie aktualnej implementacji. Moduł GUI opisany jest jako plan docelowy i nie zawiera kodu uruchomieniowego.

== Zakres

Dokument obejmuje:

- architekturę modułową części obliczeniowej,
- model danych i struktury pomocnicze,
- implementację algorytmów Fruchterman-Reingold i Tutte,
- specyfikację wejścia i wyjścia (tekst i binaria) zgodną z formatem z części C,
- kody statusu oraz scenariusze błędowe,
- sposób budowania projektu Maven.

= Architektura systemu

== Podział na moduły

Implementacja znajduje się w pakiecie `pl.graph` i składa się z następujących klas:

- `Graph` - odczyt i zapis grafu oraz mapowanie identyfikatorów węzłów,
- `Node` - model węzła z identyfikatorem i współrzędnymi,
- `Edge` - model krawędzi (indeksy w tablicy węzłów, waga, etykieta),
- `AdjacencyList` - budowa list sąsiedztwa dla algorytmu Tutte,
- `Neighbor` - pojedynczy element listy sąsiadów,
- `Fruchterman` - algorytm siłowy wyznaczania układu,
- `Tutte` - osadzenie Tutte na podstawie zredukowanej macierzy Laplace'a,
- `Gui` - planowany moduł interfejsu graficznego (Swing),
- `ExitCodes` - wspólne kody statusu dla warstwy obliczeniowej.

Dodatkowo planowany jest moduł GUI oparty o Swing, który nie jest jeszcze zaimplementowany, ale jego docelowa struktura i odpowiedzialności opisane są poniżej.

== Przepływ danych

Warstwa obliczeniowa działa w trzech etapach:

1. Wczytanie grafu do struktury `Graph` (tekst lub binaria zgodne z częścią C).
2. Uruchomienie wybranego algorytmu rozmieszczenia węzłów.
3. Zapis współrzędnych w formacie tekstowym lub binarnym.

Każdy etap jest niezależny i może zostać wywołany przez warstwę sterującą (GUI).

= Model danych

== Struktury podstawowe

W module `Graph` zdefiniowane są trzy podstawowe modele:

- `Node`
  - `id` - identyfikator logiczny węzła,
  - `x`, `y` - współrzędne geometryczne.
- `Edge`
  - `firstNodeIndex`, `secondNodeIndex` - indeksy w tablicy `nodes`,
  - `weight` - waga krawędzi,
  - `label` - etykieta (maks. 32 znaki, nadmiar obcinany).
- `Graph`
  - `nodes` - tablica węzłów,
  - `nodesCount` - liczba węzłów,
  - `edges` - tablica krawędzi,
  - `edgesCount` - liczba krawędzi.

Węzły są sortowane po `id`, a mapowanie identyfikatorów do indeksów realizowane jest przez słownik `nodeIdToIndex`.

Fragment implementacji klasy `Node`:

```java
public class Node implements Comparable<Node> {
  public int id;
  public double x;
  public double y;

  public Node(int id) {
    this.id = id;
    this.x = 0.0;
    this.y = 0.0;
  }

  @Override
  public int compareTo(Node other) {
    return Integer.compare(this.id, other.id);
  }
}
```

Fragment implementacji klasy `Edge`:

```java
public Edge(int firstNodeIndex, int secondNodeIndex, double weight, String label) {
  this.firstNodeIndex = firstNodeIndex;
  this.secondNodeIndex = secondNodeIndex;
  this.weight = weight;
  this.label = label != null ? label : "";
}
```

== Struktury pomocnicze

- `AdjacencyList` - tablica list sąsiedztwa i stopni węzłów,
- `Neighbor` - element listy sąsiadów: indeks węzła, waga, wskaźnik `next`.

Fragment implementacji budowy listy sąsiedztwa:

```java
for (int i = 0; i < graph.edgesCount; i++) {
    int firstIndex = graph.edges[i].firstNodeIndex;
    int secondIndex = graph.edges[i].secondNodeIndex;
    double weight = graph.edges[i].weight;

    Neighbor neighbor1 = new Neighbor(secondIndex, weight);
    neighbor1.next = adjList.adjacencyList[firstIndex];
    adjList.adjacencyList[firstIndex] = neighbor1;

    Neighbor neighbor2 = new Neighbor(firstIndex, weight);
    neighbor2.next = adjList.adjacencyList[secondIndex];
    adjList.adjacencyList[secondIndex] = neighbor2;

    adjList.degrees[firstIndex]++;
    adjList.degrees[secondIndex]++;
}
```

= Interfejsy modułów

== Moduł Graph

Najważniejsze metody publiczne:

- `Graph loadGraph(String path)`
- `ExitCodes saveGraphAsText(Graph graph, String path)`
- `ExitCodes saveGraphAsBinary(Graph graph, String path)`
- `int getNodeIndex(Graph graph, int nodeId)`

Uwaga: odczyt wejścia binarnego nie jest jeszcze zaimplementowany w warstwie obliczeniowej. Format binarny jest jednak zdefiniowany i zgodny z dokumentacją funkcjonalną; planowane jest dodanie dedykowanej metody I/O w module GUI/IO.

Fragment mapowania identyfikatora na indeks (wyszukiwanie binarne):

```java
public static int getNodeIndex(Graph graph, int nodeId) {
  Node key = new Node(nodeId);
  int result = Arrays.binarySearch(graph.nodes, key);
  return result < 0 ? -1 : result;
}
```

Fragment parsowania wejścia i obcinania etykiety:

```java
String label = parts[0];
if (label.length() > 32) {
  label = label.substring(0, 32);
}

int firstNode = Integer.parseInt(parts[1]);
int secondNode = Integer.parseInt(parts[2]);
if (firstNode < 0 || secondNode < 0) {
  continue;
}
```

Fragment mapowania krawędzi na indeksy węzłów:

```java
Integer firstIndex = nodeIdToIndex.get(edge.firstNodeId);
Integer secondIndex = nodeIdToIndex.get(edge.secondNodeId);
if (firstIndex == null || secondIndex == null) {
  throw new IllegalArgumentException("Error: Node ID not found");
}
graph.edges[i] = new Edge(firstIndex, secondIndex, edge.weight, edge.label);
```

== Moduł Fruchterman

- konstruktor `Fruchterman(double size, int iterations, Graph graph)`
- `void layout(Graph graph)`

== Moduł Tutte

- `ExitCodes runTutte(Graph graph, double size, int maxIterations)`

== Moduł AdjacencyList

- `AdjacencyList createAdjacencyList(Graph graph)`

== Kody statusu

Wspólne kody statusu zdefiniowane są w `ExitCodes`. Najczęściej używane w warstwie obliczeniowej:

- `SUCCESS`
- `FILE_ERROR`
- `OUTPUT_WRITE_ERROR`
- `TUTTE_ASSUMPTIONS_ERROR`
- `NUMERICAL_ERROR`
- `EMPTY_OR_INVALID_GRAPH`

= Specyfikacja danych wejściowych i wyjściowych

== Format wejściowy (tekst)

Plik wejściowy jest listą krawędzi:

```
<label> <nodeA> <nodeB> <weight>
```

Reguły parsowania:

- linie puste i komentarze `#` są pomijane,
- komentarze inline po danych są odcinane,
- `label` jest skracany do 32 znaków,
- `nodeA`, `nodeB` muszą być liczbami całkowitymi nieujemnymi,
- `weight` jest opcjonalny, domyślnie `1.0`,
- linie niepoprawne są ignorowane.

Fragment filtrowania komentarzy i pustych linii:

```java
line = line.trim();
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
```

== Format wejściowy (binarny)

Zgodny z formatem z części C i dokumentacją funkcjonalną:

1. `uint32_t` / `int`: liczba wierzchołków $N$.
2. Ciąg $N$ rekordów:
  - `uint32_t` / `int`: ID wierzchołka,
  - `double`: współrzędna X,
  - `double`: współrzędna Y.

Kolejność pól w rekordzie jest stała. W implementacji Java zapisy i odczyty są wykonywane jawnie w kolejności little-endian, aby zachować zgodność z eksportem z modułu C.

Szkic odczytu binarnego (planowana implementacja):

```java
try (DataInputStream dis = new DataInputStream(
        new BufferedInputStream(new FileInputStream(path)))) {
    int count = Integer.reverseBytes(dis.readInt());
    Graph graph = new Graph(count, 0);
    for (int i = 0; i < count; i++) {
        int id = Integer.reverseBytes(dis.readInt());
        double x = Double.longBitsToDouble(Long.reverseBytes(dis.readLong()));
        double y = Double.longBitsToDouble(Long.reverseBytes(dis.readLong()));
        graph.nodes[i] = new Node(id, x, y);
    }
}
```

== Format wyjściowy (tekst)

Każdy rekord opisuje węzeł:

```
<node_id> <x> <y>
```

Przykład zapisu:

```java
writer.printf(Locale.ROOT, "%d %.6f %.6f%n", node.id, node.x, node.y);
```

Obsługa błędów zapisu:

```java
try (PrintWriter writer = new PrintWriter(new FileWriter(path))) {
  // zapis
  return ExitCodes.SUCCESS;
} catch (IOException e) {
  System.err.println("Error: Cannot write to file: " + path);
  return ExitCodes.OUTPUT_WRITE_ERROR;
}
```

== Format wyjściowy (binarny)

Plik binarny zapisuje:

1. `nodesCount` jako 32-bitowy `int` w kolejności little-endian,
2. dla każdego węzła: `id` (`int`), `x` (`double`), `y` (`double`) w kolejności little-endian.

Fragment implementacji zapisu:

```java
dos.writeInt(Integer.reverseBytes(graph.nodesCount));
for (int i = 0; i < graph.nodesCount; i++) {
    dos.writeInt(Integer.reverseBytes(graph.nodes[i].id));
    dos.writeLong(Long.reverseBytes(Double.doubleToLongBits(graph.nodes[i].x)));
    dos.writeLong(Long.reverseBytes(Double.doubleToLongBits(graph.nodes[i].y)));
}
```

Fragment obsługi błędu zapisu binarnego:

```java
try (DataOutputStream dos = new DataOutputStream(
    new BufferedOutputStream(new FileOutputStream(path)))) {
  // zapis binarny
  return ExitCodes.SUCCESS;
} catch (IOException e) {
  System.err.println("Error: Cannot write to file: " + path);
  return ExitCodes.OUTPUT_WRITE_ERROR;
}
```

= Implementacja algorytmów

== Fruchterman-Reingold

Algorytm używa modelu siłowego z ograniczeniem do kwadratu o boku `size`:

- siły odpychania między każdą parą węzłów,
- siły przyciągania dla węzłów połączonych krawędzią,
- ograniczenie długości kroku przez temperaturę.

Wzory użyte w kodzie:

- $k = sqrt("area" / |V|)$, gdzie $"area" = "size" dot "size"$
- $f_r = (k^2) / d$
- $f_a = w dot (d^2) / k$

W trakcie każdej iteracji:

1. Zerowanie wektorów przemieszczeń.
2. Akumulacja sił odpychania dla każdej pary węzłów.
3. Akumulacja sił przyciągania dla każdej krawędzi.
4. Aktualizacja położeń z ograniczeniem przez `temperature`.
5. Chłodzenie temperatury liniowo do zera.

Fragment aktualizacji położenia:

```java
if (distance > 0) {
    double limitedDistance = Math.min(distance, temperature);
    node.x += (dx / distance) * limitedDistance;
    node.y += (dy / distance) * limitedDistance;
}
node.x = Math.min(size, Math.max(0, node.x));
node.y = Math.min(size, Math.max(0, node.y));
```

Fragment obliczania siły odpychania między parami węzłów:

```java
double dx = a.x - b.x;
double dy = a.y - b.y;
double distance = Math.hypot(dx, dy);
if (distance < 0.01) {
  dx = (random.nextDouble() - 0.5) * 0.1;
  dy = (random.nextDouble() - 0.5) * 0.1;
  distance = Math.hypot(dx, dy);
}
double force = (k * k) / distance;
double deltaX = (dx / distance) * force;
double deltaY = (dy / distance) * force;
```

Fragment uwzględniający wagę krawędzi w przyciąganiu:

```java
double force = (distance * distance) / k * edge.weight;
double deltaX = (dx / distance) * force;
double deltaY = (dy / distance) * force;
```

Fragment chłodzenia temperatury:

```java
double cooled = initialTemperature * (1.0 - (double) iteration / iterations);
temperature = Math.max(0.0, cooled);
```

Złożoność czasowa: $O(|V|^2 + |E|)$ na iterację.

== Tutte

Algorytm przebiega etapami:

1. Dla grafów z mniej niż 4 węzłami stosowane są pozycje specjalne.
2. Tworzona jest lista sąsiedztwa.
3. Wyznaczany jest cykl brzegowy (heurystyka + fallback).
4. Węzły brzegowe rozmieszczane są równomiernie na okręgu jednostkowym.
5. Dla węzłów wewnętrznych budowana jest zredukowana macierz Laplace'a.
6. Układ równań rozwiązywany jest eliminacją Gaussa z częściowym wyborem.
7. Wynik jest skalowany do obszaru o boku `size`.

Fragment wczesnego wyjscia dla bardzo malych grafow:

```java
if (graph.nodesCount < 4) {
  placeSmallGraph(graph);
  scaleGraph(graph, size);
  return ExitCodes.SUCCESS;
}
```

Fragment budowy listy sasiadow i walidacji cyklu brzegowego:

```java
AdjacencyList adjList = AdjacencyList.createAdjacencyList(graph);
int[] boundary = findBoundaryCycle(adjList);
if (boundary == null || boundary.length < 3) {
  return ExitCodes.TUTTE_ASSUMPTIONS_ERROR;
}
```

Fragment wyznaczania indeksow wewnetrznych wierzcholkow:

```java
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
```

Fragment budowy zredukowanej macierzy Laplace'a i wektorow wyrazow wolnych:

```java
double[][] A = new double[internalCount][internalCount];
double[] bx = new double[internalCount];
double[] by = new double[internalCount];

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
```

Heurystyka cyklu brzegowego:

- start w węźle o najmniejszym stopniu,
- przejście do sąsiada minimalizującego liczbę wspólnych sąsiadów,
- w razie porażki: wybór 4 węzłów o największych stopniach.

Złożoność budowy macierzy: $O(|V| + |E|)$, złożoność eliminacji Gaussa: $O(n^3)$ dla $n$ węzłów wewnętrznych.

Fragment rozmieszczenia węzłów brzegowych na okręgu jednostkowym:

```java
for (int i = 0; i < boundaryLen; i++) {
  double angle = 2.0 * Math.PI * i / boundaryLen;
  int v = boundary[i];
  graph.nodes[v].x = Math.cos(angle);
  graph.nodes[v].y = Math.sin(angle);
}
```

Fragment przypisania rozwiazania do wierzcholkow wewnetrznych:

```java
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
```

Fragment skalowania wyniku do zadanego rozmiaru:

```java
double scale = size / 2.0;
for (int i = 0; i < graph.nodesCount; i++) {
  graph.nodes[i].x *= scale;
  graph.nodes[i].y *= scale;
}
```

Fragment eliminacji Gaussa z częściowym wyborem elementu podstawowego:

```java
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
```

= Obsługa błędów i sytuacje wyjątkowe

Najważniejsze scenariusze błędowe:

- brak węzłów lub krawędzi podczas wczytania grafu (wyjątek `IllegalArgumentException`),
- nieprawidłowy format danych w pliku wejściowym (linia jest ignorowana),
- brak cyklu brzegowego w Tutte (`TUTTE_ASSUMPTIONS_ERROR`),
- osobliwość układu równań w Tutte (`NUMERICAL_ERROR`),
- błędy zapisu plików (`OUTPUT_WRITE_ERROR`).
- niezgodny format binarny (niepełny plik lub błędny nagłówek) - błąd I/O lub walidacji.
