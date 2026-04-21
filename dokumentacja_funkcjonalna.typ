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

Celem projektu jest stworzenie aplikacji okienkowej w języku Java (Swing), która pozwala użytkownikowi końcowemu wczytać graf planarny, uruchomić wybrany algorytm wyznaczania położeń węzłów, zwizualizować wynik oraz zapisać współrzędne do pliku. Program pełni rolę warstwy interfejsu użytkownika dla modułu obliczeniowego i zachowuje zgodność formatów danych z częścią C.

W aplikacji przewidziano dwa algorytmy rozmieszczania węzłów:

- Fruchterman-Reingold (metoda siłowa),
- Tutte Embedding (metoda barycentryczna).

== Zakres funkcjonalności

Program umożliwia:

1. Wczytanie definicji grafu z pliku tekstowego lub binarnego.
2. Walidację danych wejściowych i raportowanie błędów w czytelnej formie.
3. Wybór jednego z dostępnych algorytmów rozmieszczania.
4. Konfigurację parametrów obliczeń z panelu narzędziowego.
5. Interaktywną modyfikację położeń węzłów na obszarze rysowania.
6. Zmianę pola widzenia (powiększanie, pomniejszanie, przesuwanie).
7. Zmianę sposobu prezentacji grafu (etykiety, wagi, style wizualne).
8. Zapis wyników w formacie tekstowym lub binarnym.

= Interfejs Użytkownika

Aplikacja jest programem okienkowym Swing sterowanym zdarzeniami. Główne okno zawiera:

- pasek menu,
- centralny komponent rysujący graf,
- prawy panel narzędziowy,

== Układ sekcji GUI

Interfejs jest podzielony na dwa główne obszary:

- Sekcja górna (pasek menu): zarządznie plikami, dostęp do pomocy i informacji o programie.
- Sekcja główna: duży obszar rysowania prezentujący aktualny graf oraz boczny panel narzędziowy.

Sekcja wizualizacji zawiera interaktywną wizualizację i obsługuje:

- rysowanie węzłów i krawędzi zgodnie z aktualnymi ustawieniami,
- podgląd zmian po każdej modyfikacji parametrów,
- nawigację myszą i zaznaczanie elementów grafu.

Pasek menu udostępnia akcje wymagane w projekcie:

- Menu Plik:
  - Wczytaj graf (tekstowy): odczyt listy krawędzi z pliku `.txt`.
  - Wczytaj graf (binarny): odczyt grafu z pliku `.bin`.
  - Wczytaj współrzędne (tekstowe/binarne): odczyt gotowego układu węzłów.
  - Zapisz współrzędne (tekstowy): zapis wyników do pliku `.txt`.
  - Zapisz współrzędne (binarny): zapis wyników do pliku `.bin`.
- Menu Pomoc:
  - O programie.
  - Instrukcja obsługi.

Panel narzędziowy (po prawej stronie) zawiera:

- wybór algorytmu (`fruchterman` lub `tutte`),
- parametry obliczeń (iteracje, temperatura początkowa, rozmiar obszaru),
- przyciski: Uruchom, Dopasuj do okna, Powiększ, Pomniejsz, Zastosuj widok, Reset
- przełączniki widoczności: etykiety węzłów, etykiety krawędzi, wagi,
- tryb edycji umożliwiający ręczne przesuwanie węzłów myszą.

== Dostępne akcje i parametry

Elementy sterujące i ich przeznaczenie:

- Menu Plik -> Wczytaj graf (tekstowy): wybór pliku wejściowego z listą krawędzi lub gotowym grafem binarnym.
- Menu Plik -> Wczytaj współrzędne (tekstowe/binarne): odczyt gotowego układu węzłów
- Menu Plik -> Zapisz współrzędne (tekstowe/binarne): zapis aktualnych współrzędnych.
- Menu Pomoc -> O programie: wyświetlenie informacji o programie.
- Menu Pomoc -> Instrukcja obsługi: wyświetlenie krótkiego przewodnika po funkcjach programu.
- Panel narzędziowy -> Wybór metody: wybór metody (`fruchterman` albo `tutte`).
- Panel narzędziowy -> Iteracje: liczba kroków obliczeń (dla metody siłowej liczba iteracji symulacji, dla Tuttego górny limit iteracji relaksacji).
- Panel narzędziowy -> Temperatura początkowa: parametr metody siłowej (musi być dodatnia).
- Panel narzędziowy -> Rozmiar obszaru: bok kwadratu roboczego (musi być dodatni).
- Panel narzędziowy -> Uruchom: start obliczeń dla wybranego algorytmu.
- Panel narzędziowy -> Tryb edycji: ręczne przesuwanie węzłów myszą.
- Panel narzędziowy -> Nawigacja: powiększanie, pomniejszanie i dopasowanie do okna.
- Panel narzędziowy -> Widoczność: włączanie/wyłączanie etykiet węzłów, etykiet krawędzi i wag.

Wymagania spójności interfejsu:

- przy próbie zapisu bez danych wyświetlany jest komunikat błędu,
- uruchomienie algorytmu jest blokowane, gdy graf nie został poprawnie wczytany,

== Przykłady użycia

Scenariusz A: wczytanie grafu tekstowego i wygenerowanie układu metodą siłową.

1. Wybierz z paska menu Plik -> Wczytaj graf (tekstowy).
2. W panelu narzędziowym ustaw Algorytm = `fruchterman`.
3. Ustaw parametry obliczeń (np. Iteracje = 1000, Temperatura = 10.0, Rozmiar obszaru = 800.0).
4. Kliknij Uruchom.
5. Opcjonalnie użyj przycisku Dopasuj do okna, aby poprawić widoczność rysunku.
6. Zapisz wynik przez Menu Plik -> Zapisz współrzędne (tekstowe).

Scenariusz B: wygenerowanie układu metodą Tuttego i zapis binarny.

1. Wczytaj graf z paska menu (wariant tekstowy lub binarny).
2. W panelu narzędziowym ustaw Algorytm = `tutte`.
3. Ustaw liczbę iteracji.
4. Kliknij Uruchom.
5. Zapisz wynik przez Menu Plik -> Zapisz współrzędne (binarne).

Scenariusz C: ręczna korekta położeń i eksport wizualizacji.

1. Wczytaj graf oraz wygeneruj układ jednym z algorytmów.
2. Włącz opcję Tryb edycji w panelu narzędziowym.
3. Przeciągnij wybrane węzły myszą, aby skorygować układ.
4. Dostosuj widok (Powiększ, Pomniejsz, Reset widoku) oraz widoczność etykiet i wag.
5. Wyeksportuj bieżący widok przez Menu Plik -> Eksport obrazu (PNG).

Scenariusz D: obsługa błędu użytkownika przy zapisie.

1. Uruchom aplikację i nie wczytuj danych grafu.
2. Wybierz Menu Plik -> Zapisz współrzędne (tekstowe) lub (binarne).
3. Aplikacja wyświetli komunikat o błędzie informujący, że brak danych do zapisania.

= Formaty Danych

== Format wejściowy (Input)

Program akceptuje dwa warianty wejścia.

Wariant A: graf w postaci tekstowej (lista krawędzi), gdzie każda linia definiuje krawędź:

```
<nazwa_krawedzi> <wierzcholek_A> <wierzcholek_B> <waga>
```

Uwagi praktyczne:

- etykieta krawędzi ma maksymalnie 32 znaki,
- dozwolone są białe znaki między polami (spacje i tabulatory),
- linie puste oraz komentarze zaczynające się od `#` są ignorowane.

Przykład:

```
AB 1 2 1
BC 2 3 1
CD 3 4 1
DB 4 2 1.407
```

Wariant B: graf/współrzędne w formacie binarnym zgodnym z eksportem modułu C.

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

1. `uint32_t` / `int`: liczba wierzchołków $N$.
2. Ciąg $N$ rekordów dla kolejnych wierzchołków:
  - `uint32_t` / `int`: ID wierzchołka,
  - `double`: współrzędna X,
  - `double`: współrzędna Y.

Kolejność pól w rekordzie jest stała i musi być identyczna przy zapisie i odczycie.

= Opis Matematyczny i Pseudokod

== Algorytm Fruchtermana-Reingolda

Algorytm symuluje układ fizyczny, w którym wierzchołki wzajemnie się odpychają, a krawędzie działają jak sprężyny przyciągające.

=== Model Matematyczny

Dla obszaru o powierzchni $A$ i liczby wierzchołków $n$ przyjmujemy:

$
k = sqrt(A / n)
$

Siły dla odległości $d$:

$
f_r(d) = k^2 / d,
f_a(d) = w d^2 / k
$

Maksymalne przesunięcie ogranicza temperatura $T$, malejąca w kolejnych iteracjach.

```text
Algorytm Fruchterman-Reingold
Wejście: G = (V, E), iteracje K, temperatura T0, rozmiar size
Wyjście: Pozycje pos[v] dla każdego wierzchołka v w V

1: inicjalizuj T := T0
2: dla każdego wierzchołka v: pos[v] := losowa pozycja [0, size] x [0, size]
3: oblicz k := sqrt(size^2 / |V|)
4: for i := 1 to K do
5:    dla każdej pary wierzchołków (u, v):
6:        oblicz wektor d := pos[v] - pos[u], odległość dist := |d|
7:        sila[v] += (d / dist) * k^2 / dist  (odpychanie)
8:    dla każdej krawędzi (u, v) z wagą w:
9:        oblicz wektor d := pos[v] - pos[u], odległość dist := |d|
10:       sila[v] -= (d / dist) * w * dist^2 / k  (przyciąganie)
11:   dla każdego wierzchołka v:
12:       delta := min(|sila[v]|, T)
13:       pos[v] := pos[v] + (sila[v] / |sila[v]|) * delta
14:       ogranicz pos[v] do [0, size] x [0, size]
15:   T := T * 0.95  (schłodzenie)
16: end for
```

== Algorytm Osadzania Tuttego (Tutte Embedding)

Metoda opiera się na zależności barycentrycznej: każdy wierzchołek wewnętrzny jest średnią ważoną położeń swoich sąsiadów.

=== Model Matematyczny

Niech $B$ oznacza zbiór wierzchołków ustalonych (brzegowych), a $I$ zbiór wierzchołków wewnętrznych. Dla każdego $v$ z $I$:

$
p_v = (sum_(u in N(v)) w_(v,u) p_u) / (sum_(u in N(v)) w_(v,u))
$

W praktyce stosowana jest iteracyjna relaksacja aż do osiągnięcia zbieżności (lub limitu iteracji).

```text
Algorytm Tutte Embedding
Wejście: Graf G = (V, E), iteracje K, próg zbieżności eps
Wyjście: Pozycje pos[v] dla każdego wierzchołka v w V

1: I := zbiór wierzchołków wewnętrznych, B := zbiór wierzchołków brzegowych
2: rozmieść wierzchołki z B równomiernie na brzegu kwadratu [0, size]^2
3: dla każdego wierzchołka v w I: pos[v] := (size/2, size/2)
4: for iteracja := 1 to K do
5:    max_delta := 0
6:    dla każdego wierzchołka v w I:
7:        suma := (0, 0)
8:        waga_suma := 0
9:        dla każdego sąsiada u wierzchołka v:
10:           waga := waga krawędzi (v, u), domyślnie 1
11:           suma := suma + waga * pos[u]
12:           waga_suma := waga_suma + waga
13:       nowa_pos[v] := suma / waga_suma
14:       delta := |nowa_pos[v] - pos[v]|  (zmiana pozycji)
15:       max_delta := max(max_delta, delta)
16:   pos[v] := nowa_pos[v] dla wszystkich v w I
17:   jeśli max_delta < eps: zatrzymaj (osiągnięta zbieżność)
18: end for
```

= Charakterystyka Systemu

== Obsługa Sytuacji Wyjątkowych

Program informuje użytkownika za pomocą okienek kontekstowych. Aplikacja wykorzystuje okienka kontekstowe do interaktywnej komunikacji z użytkownikiem i ułatwienia nawigacji po interfejsie.

Użytkownik jest informowany o błędach i nieprawidłowych operacjach poprzez okienka kontekstowe, które wyświetlają czytelny komunikat o problemie i umożliwiają anulowanie operacji lub wskazanie wymaganych poprawek.

== Ograniczenia Systemu

- Dla dużych grafów interaktywność może maleć z powodu kosztu przeliczeń i odświeżania widoku.
- Dla algorytmu Tuttego jakość osadzenia zależy od poprawnego wyboru wierzchołków brzegowych; dla trudnych przypadków stosowany jest wariant zapasowy.
- Format binarny wymaga ścisłej zgodności typów i kolejności pól (`int`, `double`, `double`) oraz obecności nagłówka z liczbą węzłów.

== Porównanie algorytmów

- Typ algorytmu:
  Fruchterman-Reingold - symulacja fizyczna (iteracyjny),
  Tutte Embedding - barycentryczny (iteracyjna relaksacja).
- Gwarancja planarności:
  Fruchterman-Reingold - brak formalnej gwarancji,
  Tutte Embedding - tak, dla odpowiednich założeń planarnych.
- Złożoność:
  Fruchterman-Reingold - około $O((N^2 + E) \cdot I)$,
  Tutte Embedding - około $O((N + E) \cdot I)$.
- Stabilność wyniku:
  Fruchterman-Reingold - zależna od inicjalizacji,
  Tutte Embedding - zasadniczo deterministyczna przy tych samych danych wejściowych.
