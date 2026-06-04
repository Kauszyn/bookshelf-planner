# Raport nr 2 — BookShelf Planner
**Temat:** Aplikacja natywna Android do zarządzania listą czytelniczą  
**Przedmiot:** Programowanie Terminali Mobilnych (PTM)  
**Autor:** Dawid Chmiel (nr indeksu: 159989)  
**Prowadzący:** dr inż. Marcin Rodziewicz  
**Repozytorium GitHub:** [github.com/Kauszyn/bookshelf-planner](https://github.com/Kauszyn/bookshelf-planner)  
**Najnowszy Commit:** `e2ea96a Update branding and add home sorting statistics`

---

## 1. Cel aplikacji
Aplikacja **BookShelf Planner** służy do organizowania i planowania procesu czytania książek. Umożliwia użytkownikowi dynamiczne przeszukiwanie zasobów Google Books, zapisywanie wybranych tytułów w lokalnej biblioteczce, zarządzanie ich statusem (*To read*, *Reading*, *Finished*) oraz przypisywanie osobistych notatek. Głównym celem projektowym jest stworzenie stabilnego oprogramowania działającego w trybie offline-first z zachowaniem wysokiej odporności na błędy sieciowe oraz limity zapytań API, spójnego graficznie z nowoczesnymi wytycznymi Android Material 3.

---

## 2. Zastosowane technologie i dostosowanie graficzne (Branding)
*   **Język**: Kotlin 2.0.21 (target JVM 17)
*   **Interfejs Użytkownika**: Jetpack Compose + Material 3
*   **Zgodność Wizualna (Branding)**: 
    *   Wdrożono nową, autorską ikonę aplikacji (Legacy, Round dla nowszych launcherów, Adaptive Icons od API 26) przedstawiającą otwartą książkę z zieloną zakładką na ciemnoniebieskim tle.
    *   Zsynchronizowano paletę kolorystyczną Material 3 w pliku `Theme.kt` z grafiką logo: dominujący ciemny granat (`#011D44`) jako kolor główny (`primary`), stonowana zieleń (`#458356`) jako akcenty (`secondary`) oraz ciepły odcień kremu (`#FAF7F0` i `#FCFBF7` jako tła `background` / `surface`) imitujący papierowe strony.
    *   Wyłączono automatyczne nakładki wysokości (`surfaceTint = Color.Transparent`), dzięki czemu podniesione elementy (karty, przyciski) zachowują czyste kremowe barwy bez nieestetycznych fioletowych przebarwień.
*   **Nawigacja**: Compose Navigation (NavHost z trasami: `home`, `search`, `detail/{bookId}`)
*   **Baza Danych**: Room Database (lokalny zapis z reaktywnym mapowaniem Flow)
*   **Obsługa Sieci**: Retrofit 2.11.0 + Moshi Converter (komunikacja z Google Books API)
*   **Asynchroniczność**: Kotlin Coroutines + Flow (StateFlow w ViewModelu)
*   **Ładowanie Obrazów**: Coil (komponent `AsyncImage` dla okładek)

---

## 3. Architektura aplikacji
Aplikacja opiera się na architekturze **MVVM (Model-View-ViewModel)** z jednokierunkowym przepływem danych (UDF) i ręcznym wstrzykiwaniem zależności (DI) za pomocą kontenera `AppContainer` powiązanego z cyklem życia aplikacji (`BookShelfApplication`).
*   **Model**: Dane domenowe (`Book`, `BookStatus`). Rozszerzono strukturę o pole `dateAdded: Long` określające moment zapisania pozycji w bazie.
*   **Data (Repository, Local DB, Remote API)**: Klasa `BookRepository` stanowi pojedyncze źródło prawdy (Single Source of Truth), łącząc dane z lokalnej bazy Room (`BookDao`) z odpowiedziami z API sieciowego (`GoogleBooksApi`).
*   **ViewModel**: `BookShelfViewModel` zarządza stanem ekranów (przechowuje `StateFlow` z listą książek oraz stanem wyszukiwania `SearchUiState`).
*   **View**: Ekrany zaimplementowane w Jetpack Compose, które subskrybują stany ViewModelu.

### 3.1. Przepływ ekranów i Nawigacja (Nawigacja Compose)
Zgodnie z wymaganiami kursu dotyczącymi nawigacji między ekranami, aplikacja posiada zaimplementowany mechanizm wieloekranowego przepływu przy użyciu biblioteki **Navigation Compose**. 

Kluczowe aspekty systemu nawigacji w aplikacji:
*   **Zarządca nawigacji (NavHost)**: Główny kontener nawigacji znajduje się w klasie `MainActivity.kt` i jest sterowany przez instancję `rememberNavController()`.
*   **Zdefiniowane trasy (Routes)**: NavHost zarządza trzema głównymi ekranami aplikacji za pomocą unikalnych identyfikatorów tras:
    1.  `home` – Ekran główny wyświetlający zapisaną listę lektur oraz statystyki.
    2.  `search` – Ekran wyszukiwania nowych książek z Google Books API.
    3.  `detail/{bookId}` – Ekran szczegółów wybranej książki.
*   **Przekazywanie parametrów (Route Parameters)**: Nawigacja na ekran szczegółów lektury wykorzystuje dynamiczny parametr `bookId` zdefiniowany w ścieżce trasy jako `detail/{bookId}` (z typem argumentu `NavType.StringType`). Parametr ten jest automatycznie przekazywany do `BookDetailScreen` i służy do odnalezienia odpowiedniej książki w bazie danych Room.
*   **Wsteczny stos (Back Stack)**: Przechodzenie wstecz (np. z ekranu wyszukiwania lub szczegółów na ekran główny) jest obsługiwane przez standardowe wywołanie `navController.popBackStack()`, co zapewnia płynność interfejsu i zgodność z systemowym przyciskiem Wstecz.

---

## 4. Opis głównych ekranów
1.  **Home Screen (Ekran główny)**: 
    *   **Karta Statystyk czytelniczych**: Nowy, estetyczny komponent podsumowujący postępy użytkownika w czasie rzeczywistym. Wyświetla: całkowitą liczbę zapisanych książek, liczbę pozycji oznaczonych jako *Reading*, liczbę pozycji *Finished* oraz zliczoną sumę stron przeczytanych lektur.
    *   **Dynamiczne Sortowanie**: Dodano menu rozwijane `DropdownMenu` wywoływane ikoną `List`, pozwalające sortować zapisaną biblioteczkę według kryteriów: Alfabetycznie (A-Z), Ostatnio dodane, Liczba stron oraz Status czytania.
    *   **Filtrowanie**: Klasyczne filtry statusów (*All*, *To read*, *Reading*, *Finished*) oparte na komponentach `FilterChip` z reaktywnym odświeżaniem listy.
2.  **Search Screen (Ekran wyszukiwania)**: Umożliwia wpisanie zapytania tekstowego. Wyświetla listę wyników z Google Books API z możliwością dodania książki do listy lektur za pomocą przycisku „Add”. W przypadku problemów z siecią lub limitami API, w tym miejscu renderowana jest sekcja błędu z przyciskiem „Retry”.
3.  **Book Detail Screen (Ekran szczegółów)**: Pokazuje okładkę, autorów, rok wydania, liczbę stron i opis książki. Umożliwia aktualizację statusu czytania lektury (zapisywaną reaktywnie w Room) oraz edycję osobistej notatki tekstowej zapisywanej przyciskiem „Save note”.

---

## 5. Komunikacja z Google Books API oraz lokalna baza Room
*   **API**: Zapytania sieciowe są wysyłane asynchronicznie za pomocą klienta Retrofit na endpoint `volumes?q={query}`. Odpowiedzi w formacie JSON są mapowane przy użyciu Moshi z typów DTO (`VolumeDto`) na obiekt domenowy `Book`. Adresy URL okładek są automatycznie modyfikowane, by wymusić protokół HTTPS.
*   **Room (Aktualizacja Schematu)**:
    *   Z racji dodania nowej kolumny `dateAdded`, zaktualizowano wersję bazy danych Room z `1` na `2` w `@Database`.
    *   W budowniczym bazy wdrożono metodę `.fallbackToDestructiveMigration()`. Jest to całkowicie bezpieczne i w pełni akceptowalne podejście w projektach studenckich na tym etapie zaawansowania – chroni aplikację przed awarią (crashem), automatycznie przebudowując tabele SQLite na urządzeniu testowym w momencie wykrycia zmiany wersji schematu.

---

## 6. Obsługa błędów i limitowania zapytań (HTTP 429 i inne)
Podczas intensywnych testów aplikacji napotkano na problem z limitem zapytań (**HTTP 429 Too Many Requests**) narzucanym przez Google Books API przy braku klucza deweloperskiego. Spowodowane to było m.in. możliwością wielokrotnego wysyłania tych samych zapytań przez spamowanie przycisku "Go".

W celu zapewnienia stabilności wdrożono kompleksowe mechanizmy obronne:
1.  **Opcjonalny Klucz API Google Books**: Zaimplementowano odczytywanie opcjonalnego klucza API z pliku `local.properties` (jako `GOOGLE_BOOKS_API_KEY`) wstrzykiwanego przez gradle do `BuildConfig`. Klucz jest przekazywany w locie w module DI `AppContainer` do repozytorium i doklejany do parametrów Retrofit, dzięki czemu w wersji repozytoryjnej na Git nie ma zahardkodowanych sekretów.
2.  **Blokowanie powielonych zapytań**: Przycisk "Go" oraz akcja wyszukiwania na klawiaturze (IME Search) są natychmiast blokowane w momencie rozpoczęcia zapytania sieciowego (`SearchUiState.Loading`).
3.  **Ogólny Cooldown**: Wprowadzono 1-sekundowy limit czasu (`searchCooldownMs = 1000L`) pomiędzy kolejnymi zapytaniami sieciowymi, co zapobiega zjawisku spamu.
4.  **Obsługa HTTP 429 (Rate Limiting)**:
    *   W przypadku otrzymania błędu 429 aplikacja wyświetla przyjazny komunikat: *„Too many requests. Please wait a moment and try again.”*.
    *   Uruchamiany jest 5-sekundowy cooldown (`isRateLimitCooldownActive`), podczas którego przycisk wyszukiwania "Go" oraz przycisk ponowienia "Retry" są wyłączone. Po upływie 5 sekund przyciski są automatycznie odblokowywane.
5.  **Obsługa HTTP 503 (Brak dostępności serwisu)**: Wyświetlany jest komunikat: *„Google Books is temporarily unavailable. Please try again later.”*.
6.  **Obsługa Braku Internetu (`IOException`)**: Wyświetlany jest komunikat: *„No internet connection. Check your network and try again.”*.
7.  **Pozostałe błędy**: Mapowane są na ogólny komunikat: *„Something went wrong while searching books.”*.
8.  **Bezpieczeństwo logów**: Wszelkie techniczne szczegóły błędów (stacktrace) są zapisywane w Logcat przy użyciu `Log.e`, nie wyciekając do interfejsu graficznego użytkownika.

---

## 7. Testy ręczne
Zweryfikowano poprawność działania aplikacji w następujących scenariuszach:
1.  *Test braku internetu*: Po włączeniu trybu samolotowego i próbie wyszukiwania aplikacja poprawnie wyświetliła błąd sieciowy oraz zablokowała przyciski w tryb „Retry”. Po przywróceniu sieci i kliknięciu „Retry” książki zostały wczytane.
2.  *Test spamu*: Szybkie, wielokrotne klikanie przycisku „Go” nie wywołało równoległych żądań HTTP (przycisk natychmiast stał się nieaktywny w stanie `Loading`).
3.  *Test pustego zapytania*: Wpisanie samych spacji lub pustego pola tekstowego nie wywołuje żadnego zapytania API (przycisk „Go” jest nieaktywny).
4.  *Test limitu 429*: Emulacja błędu 429 zablokowała wyszukiwanie na dokładnie 5 sekund, informując o konieczności odczekania, po czym interfejs powrócił do stanu używalności.
5.  *Testy sortowania i statystyk*: Zweryfikowano prawidłowe zliczanie przeczytanych stron oraz książek (podział na Reading/Finished), jak również poprawne przeorganizowanie listy po wybraniu kryterium sortowania w DropdownMenu.
6.  *Test migracji destrukcyjnej*: Zweryfikowano, że po aktualizacji schematu bazy Room, baza na telefonie została automatycznie i poprawnie utworzona na nowo bez crashowania aplikacji.

---

## 8. Problemy napotkane podczas implementacji
Największym wyzwaniem była stabilna obsługa limitu zapytań bez rejestracji płatnego klucza Google API oraz zażegnanie domyślnego fioletowego zabarwienia Material 3 na kartach (spowodowanego przez primary elevation overlay). Problem ten rozwiązano po stronie klienta poprzez odpowiednie blokowanie akcji UI, wprowadzenie timera blokady oraz nadpisanie `surfaceTint` jako przezroczysty w motywie graficznym Compose.

---

## 9. Plan dalszych prac (do Raportu nr 3)
*   Zaimplementowanie Live Search ze stabilnym debouncingiem (300 ms) w strumieniu Flow.
*   Dodanie mechanizmu cache wyników wyszukiwania w warstwie repozytorium w celu optymalizacji połączeń.
*   Przygotowanie testów jednostkowych dla ViewModelu oraz repozytorium przy użyciu MockWebServer.

---

## 10. Wnioski
Aplikacja w wersji na Raport nr 2 cechuje się wysoką stabilnością, odpornością na błędy komunikacji sieciowej oraz profesjonalnym i dopracowanym interfejsem graficznym Material 3, który jest spójny kolorystycznie z logo projektu. Zaimplementowane ulepszenia podnoszą wartość użytkową planera oraz ułatwiają obronę techniczną przed komisją.
