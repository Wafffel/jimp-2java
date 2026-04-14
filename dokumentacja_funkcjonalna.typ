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
    Dokumentacja funkcjonalna
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

== Cel projektu

Celem projektu jest stworzenie aplikacji w języku Java (Swing), służącej do wizualizacji grafu planarnego oraz do wyznaczania i modyfikowania współrzędnych jego węzłów. Program jest warstwą graficzną systemu: umożliwia użytkownikowi wczytanie danych, uruchomienie algorytmów rozmieszczania oraz zapis wyników.

Aplikacja implementuje dwa podejścia do problemu rysowania grafów:

- Algorytm Fruchtermana-Reingolda - podejście oparte na symulacji sił fizycznych.
- Algorytm Tuttego (Tutte Embedding) - podejście barycentryczne oparte na układzie równań.

== Zakres funkcjonalności

Program umożliwia:

1. Wczytanie definicji grafu z pliku tekstowego oraz walidację danych.
2. Wczytanie współrzędnych z pliku tekstowego lub binarnego.
3. Wybór algorytmu rozmieszczania.
4. Parametryzację działania algorytmu z poziomu panelu narzędziowego.
5. Interaktywną modyfikację położenia węzłów na obszarze rysowania.
6. Zapis wyników w formacie tekstowym lub binarnym.
7. Obsługę sytuacji wyjątkowych z czytelnymi komunikatami.

= Interfejs Użytkownika

Aplikacja jest programem okienkowym Swing sterowanym zdarzeniami. Główne okno zawiera:

- pasek menu,
- komponent rysujący graf,
- panel narzędziowy,
- pasek statusu.

== Dostępne akcje i parametry

Elementy sterujące i ich przeznaczenie:

- Plik -> Wczytaj graf (tekst): wybór pliku wejściowego z listą krawędzi.
- Plik -> Wczytaj współrzędne (txt/bin): odczyt gotowego układu węzłów.
- Plik -> Zapisz współrzędne (txt/bin): zapis aktualnych współrzędnych.
- Algorytm: wybór metody (`fr` albo `tutte`).
- Iteracje: liczba kroków obliczeń (dotyczy głównie algorytmu siłowego).
- Uruchom: start obliczeń dla wybranego algorytmu.
- Tryb edycji: ręczne przesuwanie węzłów myszą.
- Widok: powiększanie, pomniejszanie i przesuwanie obszaru rysowania.

== Przykłady użycia

Scenariusz A: wygenerowanie układu metodą siłową.

1. Wybierz Plik -> Wczytaj graf (tekst).
2. Ustaw Algorytm = `fr`, Iteracje = 1000.
3. Kliknij Uruchom.
4. Zapisz wynik przez Plik -> Zapisz współrzędne (txt).

Scenariusz B: wygenerowanie układu metodą Tuttego i zapis binarny.

1. Wczytaj graf z pliku tekstowego.
2. Ustaw Algorytm = `tutte`.
3. Kliknij Uruchom.
4. Zapisz wynik przez Plik -> Zapisz współrzędne (bin).

= Formaty Danych

== Format wejściowy (Input)

Plik tekstowy, gdzie każda linia definiuje krawędź:

```
<nazwa_krawedzi> <wierzcholek_A> <wierzcholek_B> <waga>
```

Przykład:

```
AB 1 2 1
BC 2 3 1
CD 3 4 1
DB 4 2 1.407
```

== Format wyjściowy (Output)

=== Format tekstowy (.txt)

Każda linia zawiera ID wierzchołka oraz jego współrzędne:

```
<wierzcholek> <x> <y>
```

Przykład:

```
1 0.0 0.0
2 1.0 0.0
3 1.0 1.0
4 0.0 1.0
```

=== Format binarny (.bin)

Struktura zgodna z modułem C:

1. uint32_t: liczba wierzchołków N.
2. Ciąg N rekordów:
   - uint32_t: ID wierzchołka,
   - double: współrzędna X,
   - double: współrzędna Y.

= Opis Matematyczny i Pseudokod

== Algorytm Fruchtermana-Reingolda

Algorytm symuluje układ fizyczny, w którym wierzchołki wzajemnie się odpychają, a krawędzie działają jak sprężyny przyciągające.

=== Model Matematyczny

Dla obszaru o powierzchni $A$ i liczby wierzchołków $n$ przyjmujemy:

$
k = C sqrt(A / n)
$

Siły dla odległości $d$:

$
f_r(d) = k^2 / d, quad f_a(d) = d^2 / k
$

Maksymalne przesunięcie ogranicza temperatura $T$, malejąca w kolejnych iteracjach.

```text
Algorytm Fruchterman-Reingold
Wejście: G = (V, E), iteracje K
Wyjście: Pozycje pos[v] dla v z V

1: inicjalizuj losowo pos[v]
2: oblicz k
3: for i <- 1..K do
4:    oblicz siły odpychania dla par wierzchołków
5:    oblicz siły przyciągania dla krawędzi
6:    zaktualizuj pozycje z ograniczeniem temperatury
7:    schłodź temperaturę
8: end for
```

== Algorytm Osadzania Tuttego (Tutte Embedding)

Metoda opiera się na zależności barycentrycznej: każdy wierzchołek wewnętrzny jest średnią (ważoną) położeń swoich sąsiadów.

=== Model Matematyczny

Niech $B$ oznacza zbiór wierzchołków ustalonych (brzegowych), a $I$ zbiór wierzchołków wewnętrznych. Dla każdego $v$ z $I$:

W zapisie skróconym: pos_v = (1 / deg_v) \* S.

gdzie S oznacza sumę położeń sąsiadów wierzchołka v.

W praktyce rozwiązujemy układ równań liniowych dla współrzędnych X i Y.

```text
Algorytm Tutte Embedding
Wejście: Graf G = (V, E)
Wyjście: Pozycje pos[v] dla v z V

1: wybierz i ustal wierzchołki brzegowe B
2: zidentyfikuj wierzchołki wewnętrzne I = V \ B
3: zbuduj układ równań dla współrzędnych X i Y
4: rozwiąż układ (iteracyjnie lub metodą liniową)
5: przypisz obliczone współrzędne wierzchołkom z I
```

= Porównanie Algorytmów

- Typ algorytmu:
  Fruchterman-Reingold - symulacja fizyczna (iteracyjny),
  Tutte Embedding - algebraiczny (deterministyczny).
- Gwarancja planarności:
  Fruchterman-Reingold - brak formalnej gwarancji,
  Tutte Embedding - tak, dla odpowiednich założeń planarnych.
- Złożoność:
  Fruchterman-Reingold - około $O(N^2 \cdot i t e r)$,
  Tutte Embedding - koszt rozwiązania układu równań, zwykle około $O(N^3)$.
- Stabilność wyniku:
  Fruchterman-Reingold - zależna od inicjalizacji,
  Tutte Embedding - deterministyczna przy tych samych danych.

= Obsługa Sytuacji Wyjątkowych

Program informuje użytkownika komunikatem i zachowuje spójny stan interfejsu.

- Kod 1: Input file not found
  opis: podana ścieżka do pliku nie istnieje.
- Kod 2: Cannot open file
  opis: brak uprawnień do odczytu/zapisu.
- Kod 3: Invalid edge format
  opis: błąd parsowania linii wejściowej.
- Kod 4: Unknown layout algorithm
  opis: podano nieznaną nazwę algorytmu.
- Kod 5: Invalid iteration count
  opis: niepoprawna liczba iteracji.
- Kod 6: Output write error
  opis: błąd zapisu pliku wynikowego.

= Ograniczenia Systemu

- Dla dużych grafów interaktywność może maleć z powodu kosztu przeliczeń i odświeżania widoku.
- Dla algorytmu Tuttego wymagane są odpowiednie warunki wejściowe grafu; ich niespełnienie może prowadzić do zdegenerowanego układu.
- Format binarny wymaga ścisłej zgodności typów i kolejności pól, aby zachować kompatybilność z modułem C.
