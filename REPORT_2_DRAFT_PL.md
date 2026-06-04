# Raport nr 2 — BookShelf Planner
**Temat:** Aplikacja natywna Android do zarządzania listą czytelniczą  
**Przedmiot:** Programowanie Terminali Mobilnych (PTM)  
**Autor:** Dawid Chmiel (nr indeksu: 159989)  
**Prowadzący:** dr inż. Marcin Rodziewicz  
**Repozytorium GitHub:** [github.com/Kauszyn/bookshelf-planner](https://github.com/Kauszyn/bookshelf-planner)  
**Najnowszy Commit:** `5d76627 Document Navigation Compose screen flow`

---


## 1. Cel aplikacji
Aplikacja **BookShelf Planner** służy do organizowania i planowania procesu czytania książek. Umożliwia użytkownikowi dynamiczne przeszukiwanie zasobów Google Books, zapisywanie wybranych tytułów w lokalnej biblioteczce, zarządzanie ich statusem (*To read*, *Reading*, *Finished*) oraz przypisywanie osobistych notatek. Głównym celem projektowym jest stworzenie stabilnego oprogramowania działającego w trybie offline-first z zachowaniem wysokiej odporności na błędy sieciowe, w pełni spójnego graficznie z nowoczesnymi wytycznymi Android Material 3.

---

## 2. Zastosowane technologie i dostosowanie graficzne (Branding)
*   **Język**: Kotlin 2.0.21 (target JVM 17)
*   **Interfejs Użytkownika**: Jetpack Compose + Material 3
*   **Zgodność Wizualna (Branding)**: 
    *   Wdrożono nową, autorską ikonę aplikacji (Legacy, Round oraz Adaptive Icons od API 26) przedstawiającą otwartą książkę z zieloną zakładką na ciemnoniebieskim tle.
    *   Zsynchronizowano paletę kolorystyczną Material 3 w pliku `Theme.kt` z grafiką logo: dominujący ciemny granat (`#011D44`) jako kolor główny (`primary`), stonowana zieleń (`#458356`) jako akcenty (`secondary`) oraz ciepły odcień kremu (`#FAF7F0` i `#FCFBF7` jako tła `background` / `surface`) imitujący papierowe strony.
    *   Wyłączono automatyczne nakładki wysokości (`surfaceTint = Color.Transparent`), dzięki czemu podniesione elementy (karty, przyciski) zachowują czyste kremowe barwy bez nieestetycznych fioletowych przebarwień.
*   **Nawigacja**: Compose Navigation (NavHost z trasami: `home`, `search`, `detail/{bookId}`)
*   **Baza Danych**: Room Database (lokalny zapis z reaktywnym mapowaniem Flow, schemat wersji 2)
*   **Obsługa Sieci**: Retrofit 2.11.0 + Moshi Converter (komunikacja z Google Books API)
*   **Asynchroniczność**: Kotlin Coroutines + Flow (StateFlow w ViewModelu)
*   **Ładowanie Obrazów**: Coil (komponent `AsyncImage` dla okładek)

---

## 3. Architektura aplikacji
Aplikacja opiera się na architekturze **MVVM (Model-View-ViewModel)** z jednokierunkowym przepływem danych (UDF) i ręcznym wstrzykiwaniem zależności (DI) za pomocą kontenera `AppContainer` powiązanego z cyklem życia aplikacji (`BookShelfApplication`).
*   **Model**: Dane domenowe (`Book`, `BookStatus`). Rozszerzono strukturę o pole `dateAdded: Long` określające moment pierwszego zapisania pozycji w bazie.
*   **Data (Repository, Local DB, Remote API)**: Klasa `BookRepository` stanowi pojedyncze źródło prawdy (Single Source of Truth), łącząc dane z lokalnej bazy Room (`BookDao`) z odpowiedziami z API sieciowego (`GoogleBooksApi`).
*   **ViewModel**: `BookShelfViewModel` zarządza stanem ekranów (przechowuje `StateFlow` z listą książek oraz stanem wyszukiwania `SearchUiState`).
*   **View**: Ekrany zaimplementowane w Jetpack Compose, które subskrybują stany ViewModelu.

### 3.1. Przepływ ekranów i Nawigacja
Aplikacja posiada zaimplementowany mechanizm wieloekranowego przepływu przy użyciu biblioteki **Navigation Compose**:
*   **Zarządca nawigacji (NavHost)**: Główny kontener nawigacji znajduje się w klasie `MainActivity.kt` i jest sterowany przez instancję `rememberNavController()`.
*   **Zdefiniowane trasy (Routes)**: NavHost zarządza trzema głównymi ekranami aplikacji za pomocą unikalnych identyfikatorów tras:
    1.  `home` – Ekran główny wyświetlający zapisaną listę lektur oraz statystyki.
    2.  `search` – Ekran wyszukiwania nowych książek z Google Books API.
    3.  `detail/{bookId}` – Ekran szczegółów wybranej książki.
*   **Przekazywanie parametrów (Route Parameters)**: Nawigacja na ekran szczegółów lektury wykorzystuje dynamiczny parametr `bookId` zdefiniowany w ścieżce trasy jako `detail/{bookId}`. Parametr ten służy do odnalezienia odpowiedniej książki w bazie danych Room.
*   **Wsteczny stos (Back Stack)**: Przechodzenie wstecz jest obsługiwane przez standardowe wywołanie `navController.popBackStack()`, co zapewnia płynność interfejsu i zgodność z systemowym przyciskiem Wstecz.

---

## 4. Opis głównych ekranów
1.  **Home Screen (Ekran główny)**: 
    *   **Karta Statystyk czytelniczych**: Komponent podsumowujący postępy użytkownika w czasie rzeczywistym. Wyświetla: całkowitą liczbę zapisanych książek, liczbę pozycji oznaczonych jako *Reading*, liczbę pozycji *Finished* oraz sumę stron przeczytanych lektur.
    *   **Dynamiczne Sortowanie**: Wywoływane dedykowaną ikoną menu wyboru sortowania, pozwalające przeorganizować listę według kryteriów: Alfabetycznie (A-Z), Ostatnio dodane, Liczba stron oraz Status czytania.
    *   **Filtrowanie**: Filtry statusów (*All*, *To read*, *Reading*, *Finished*) oparte na komponentach `FilterChip` z reaktywnym odświeżaniem listy.
2.  **Search Screen (Ekran wyszukiwania)**: Umożliwia wpisanie zapytania tekstowego. Wyświetla listę wyników z Google Books API z możliwością dodania książki do listy lektur za pomocą przycisku „Add”.
3.  **Book Detail Screen (Ekran szczegółów)**: Pokazuje okładkę, autorów, rok wydania, liczbę stron i opis książki. Umożliwia aktualizację statusu czytania lektury oraz edycję osobistej notatki tekstowej zapisywanej przyciskiem „Save note”.

---

## 5. Komunikacja z Google Books API oraz lokalna baza Room
*   **API**: Zapytania sieciowe są wysyłane asynchronicznie za pomocą klienta Retrofit na endpoint `volumes?q={query}`. Odpowiedzi w formacie JSON są mapowane przy użyciu Moshi z typów DTO na obiekt domenowy `Book`. Adresy URL okładek są automatycznie modyfikowane, by wymusić protokół HTTPS.
    *   **Opcjonalny Klucz API**: Zaimplementowano odczytywanie opcjonalnego klucza API z pliku `local.properties` (jako `GOOGLE_BOOKS_API_KEY`) wstrzykiwanego przez gradle do `BuildConfig`. Klucz jest przekazywany w locie w module DI `AppContainer` do repozytorium, dzięki czemu w wersji repozytoryjnej na Git nie ma zahardkodowanych sekretów.
*   **Room**: Encja `BookEntity` przechowuje wszystkie kluczowe dane książki lokalnie. Room dba o to, by użytkownik miał natychmiastowy dostęp do swoich notatek i statusów bez połączenia z siecią. W budowniczym bazy wdrożono metodę `.fallbackToDestructiveMigration()`. Jest to bezpieczne i w pełni akceptowalne podejście w projektach studenckich na tym etapie zaawansowania – chroni aplikację przed awarią (crashem), automatycznie przebudowując tabele SQLite na urządzeniu testowym w momencie wykrycia zmiany wersji schematu (z 1 na 2).
*   **Jakość i obsługa błędów**: Aplikacja posiada również obsługę sytuacji wyjątkowych, takich jak brak połączenia internetowego, czasowa niedostępność API lub limit zapytań, dzięki czemu interfejs pozostaje stabilny i czytelny dla użytkownika.

---

## 6. Prezentacja działania aplikacji

Poniżej przedstawiono zrzuty ekranu ilustrujące kompletny interfejs i działanie aplikacji w systemie Android:

### 6.1. Ekran główny i statystyki
![Ekran główny](screenshots/home_page.png)
*Ekran główny prezentuje zapisaną bibliotekę użytkownika, statystyki czytelnicze oraz filtry statusów.*

### 6.2. Filtrowanie i sortowanie listy lektur
![Filtrowanie listy](screenshots/category_view.png)
*Lista lektur przefiltrowana reaktywnie według statusu 'Reading' (W trakcie czytania).*

![Menu sortowania](screenshots/sort_view.png)
*Menu sortowania umożliwia zmianę kolejności książek według tytułu, daty dodania, liczby stron lub statusu.*

![Lista posortowana](screenshots/sorted_by_pagecount.png)
*Widok listy posortowanej malejąco według liczby stron dla łatwiejszego wyszukiwania najkrótszych/najdłuższych lektur.*

### 6.3. Wyszukiwanie w Google Books API
![Ekran wyszukiwania](screenshots/seearch_view.png)
*Ekran wyszukiwania korzysta z Google Books API i prezentuje wyniki w postaci kart z okładką, autorem, opisem oraz możliwością dodania pozycji do lokalnej listy.*

### 6.4. Ekran szczegółów, zmiana statusu i notatki
![Ekran szczegółów](screenshots/details_and_status_view.png)
*Ekran szczegółów pozwala podejrzeć informacje o książce, zmienić status czytania oraz zapisać osobistą notatkę.*

![Osobista notatka](screenshots/note_view.png)
*Osobista notatka tekstowa użytkownika zsynchronizowana z bazą Room, widoczna w sekcji szczegółów książki.*

### 6.5. Spójność graficzna (Branding)
![Logo aplikacji](screenshots/BK_logo.png)
*Ikona i kolorystyka aplikacji tworzą spójną identyfikację wizualną projektu.*

---

## 7. Ściąga do obrony (Wskazówki ustne)
*   **Co robi aplikacja?**
    *   Jest to planer czytelniczy offline-first. Pozwala wyszukiwać książki z zewnętrznego API Google Books, dodawać je do swojej biblioteki, przypisywać im status czytania (*To read*, *Reading*, *Finished*) oraz dodawać osobiste notatki.
*   **Jak działa nawigacja?**
    *   Używa biblioteki `Navigation Compose` powiązanej w `MainActivity`. Przepływ między ekranem głównym a szczegółami lektury jest dynamiczny i realizowany przy pomocy parametru trasy `detail/{bookId}`.
*   **Co zapisuje Room?**
    *   Lokalna baza Room trwale przechowuje wszystkie informacje o dodanych książkach (unikalne ID, tytuł, autorów, opis, datę dodania, status czytania, notatkę, link do okładki).
*   **Jak działa wyszukiwanie przez Google Books API?**
    *   Zapytania sieciowe są wysyłane asynchronicznie przez Retrofit do endpointu Google Books. JSON jest mapowany na obiekty klasy domenowej `Book` przy użyciu adaptera Moshi.
*   **Po co jest API key w `local.properties`?**
    *   Służy do autoryzacji zapytań z Google Books w celu podniesienia limitu zapytań (quota). Ponieważ plik `local.properties` jest lokalny i ignorowany przez gita, klucz nie wycieka do repozytorium GitHub (jest wstrzykiwany przez `BuildConfig`).
*   **Jak działa sortowanie i statystyki?**
    *   Sortowanie działa w ViewModelu poprzez reaktywne łączenie (`combine`) strumienia książek z bazy z wybranym stanem `BookSortOrder`. Statystyki (np. suma stron) są dynamicznie wyliczane bezpośrednio z listy obiektów w pamięci za pomocą metod Kotlin Collection (`count()`, `sumOf()`).
*   **Jak użytkownik zmienia status i notatkę?**
    *   Użytkownik klika w przycisk statusu lub edytuje pole tekstowe notatki na ekranie szczegółów, co wywołuje odpowiednio metody ViewModelu wykonujące operacje `@Upsert` w Roomie (z zachowaniem obecnego obiektu poprzez `.copy()`).

---

## 8. Wnioski
Aplikacja BookShelf Planner w pełni realizuje wszystkie kryteria i wymagania techniczne projektu studenckiego. Integracja lokalnej persystencji Room z zewnętrznym API Retrofit, zaimplementowana Nawigacja Compose oraz profesjonalnie dostosowany interfejs Material 3 stanowią spójną i stabilną całość, gotową do prezentacji i obrony przed komisją.
