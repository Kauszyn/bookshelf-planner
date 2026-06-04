# Raport nr 2 — BookShelf Planner
**Temat:** Aplikacja natywna Android do zarządzania listą czytelniczą  
**Przedmiot:** Programowanie Terminali Mobilnych (PTM)  
**Autor:** Dawid Chmiel (nr indeksu: 159989)  
**Prowadzący:** dr inż. Marcin Rodziewicz  
**Repozytorium GitHub:** [github.com/Kauszyn/bookshelf-planner](https://github.com/Kauszyn/bookshelf-planner)  
**Najnowszy Commit:** `f61045b Improve search error handling`

---

## 1. Cel aplikacji
Aplikacja **BookShelf Planner** służy do organizowania i planowania procesu czytania książek. Umożliwia użytkownikowi dynamiczne przeszukiwanie zasobów Google Books, zapisywanie wybranych tytułów w lokalnej biblioteczce, zarządzanie ich statusem (*To read*, *Reading*, *Finished*) oraz przypisywanie osobistych notatek. Głównym celem projektowym jest stworzenie stabilnego oprogramowania działającego w trybie offline-first z zachowaniem wysokiej odporności na błędy sieciowe oraz limity zapytań API.

---

## 2. Zastosowane technologie
*   **Język**: Kotlin 2.0.21 (target JVM 17)
*   **Interfejs Użytkownika**: Jetpack Compose + Material 3
*   **Nawigacja**: Compose Navigation (NavHost z trasami: `home`, `search`, `detail/{bookId}`)
*   **Baza Danych**: Room Database (lokalny zapis z reaktywnym mapowaniem Flow)
*   **Obsługa Sieci**: Retrofit 2.11.0 + Moshi Converter (komunikacja z Google Books API)
*   **Asynchroniczność**: Kotlin Coroutines + Flow (StateFlow w ViewModelu)
*   **Ładowanie Obrazów**: Coil (komponent `AsyncImage` dla okładek)

---

## 3. Architektura aplikacji
Aplikacja opiera się na architekturze **MVVM (Model-View-ViewModel)** z jednokierunkowym przepływem danych (UDF) i ręcznym wstrzykiwaniem zależności (DI) za pomocą kontenera `AppContainer` powiązanego z cyklem życia aplikacji (`BookShelfApplication`).
*   **Model**: Dane domenowe (`Book`, `BookStatus`).
*   **Data (Repository, Local DB, Remote API)**: Klasa `BookRepository` stanowi pojedyncze źródło prawdy (Single Source of Truth), łącząc dane z lokalnej bazy Room (`BookDao`) z odpowiedziami z API sieciowego (`GoogleBooksApi`).
*   **ViewModel**: `BookShelfViewModel` zarządza stanem ekranów (przechowuje `StateFlow` z listą książek oraz stanem wyszukiwania `SearchUiState`).
*   **View**: Ekrany zaimplementowane w Jetpack Compose, które subskrybują stany ViewModelu.

---

## 4. Opis głównych ekranów
1.  **Home Screen (Ekran główny)**: Wyświetla listę książek zapisaną w lokalnej bazie Room. Zawiera filtry (*All*, *To read*, *Reading*, *Finished*) oparte na komponentach `FilterChip` oraz umożliwia szybkie usuwanie książek przyciskiem „Remove”.
2.  **Search Screen (Ekran wyszukiwania)**: Umożliwia wpisanie zapytania tekstowego. Wyświetla listę wyników z Google Books API z możliwością dodania książki do listy lektur za pomocą przycisku „Add”. W przypadku problemów z siecią lub limitami API, w tym miejscu renderowana jest sekcja błędu z przyciskiem „Retry”.
3.  **Book Detail Screen (Ekran szczegółów)**: Pokazuje okładkę, autorów, rok wydania, liczbę stron i opis książki. Umożliwia aktualizację statusu czytania lektury (zapisywaną reaktywnie w Room) oraz edycję osobistej notatki tekstowej zapisywanej przyciskiem „Save note”.

---

## 5. Komunikacja z Google Books API oraz lokalna baza Room
*   **API**: Zapytania sieciowe są wysyłane asynchronicznie za pomocą klienta Retrofit na endpoint `volumes?q={query}`. Odpowiedzi w formacie JSON są mapowane przy użyciu Moshi z typów DTO (`VolumeDto`) na obiekt domenowy `Book`. Adresy URL okładek są automatycznie modyfikowane, by wymusić protokół HTTPS.
*   **Room**: Encja `BookEntity` przechowuje wszystkie kluczowe dane książki lokalnie. Room dba o to, by użytkownik miał natychmiastowy dostęp do swoich notatek i statusów bez połączenia z siecią.

---

## 6. Obsługa błędów i limitowania zapytań (HTTP 429 i inne)
Podczas intensywnych testów aplikacji napotkano na problem z limitem zapytań (**HTTP 429 Too Many Requests**) narzucanym przez Google Books API przy braku klucza deweloperskiego. Spowodowane to było m.in. możliwością wielokrotnego wysyłania tych samych zapytań przez spamowanie przycisku "Go".

W celu zapewnienia stabilności wdrożono kompleksowe mechanizmy obronne:
1.  **Blokowanie powielonych zapytań**: Przycisk "Go" oraz akcja wyszukiwania na klawiaturze (IME Search) są natychmiast blokowane w momencie rozpoczęcia zapytania sieciowego (`SearchUiState.Loading`).
2.  **Ogólny Cooldown**: Wprowadzono 1-sekundowy limit czasu (`searchCooldownMs = 1000L`) pomiędzy kolejnymi zapytaniami sieciowymi, co zapobiega zjawisku spamu.
3.  **Obsługa HTTP 429 (Rate Limiting)**:
    *   W przypadku otrzymania błędu 429 aplikacja wyświetla przyjazny komunikat: *„Too many requests. Please wait a moment and try again.”*.
    *   Uruchamiany jest 5-sekundowy cooldown (`isRateLimitCooldownActive`), podczas którego przycisk wyszukiwania "Go" oraz przycisk ponowienia "Retry" są wyłączone. Po upływie 5 sekund przyciski są automatycznie odblokowywane.
4.  **Obsługa HTTP 503 (Brak dostępności serwisu)**: Wyświetlany jest komunikat: *„Google Books is temporarily unavailable. Please try again later.”*.
5.  **Obsługa Braku Internetu (`IOException`)**: Wyświetlany jest komunikat: *„No internet connection. Check your network and try again.”*.
6.  **Pozostałe błędy**: Mapowane są na ogólny komunikat: *„Something went wrong while searching books.”*.
7.  **Bezpieczeństwo logów**: Wszelkie techniczne szczegóły błędów (stacktrace) są zapisywane w Logcat przy użyciu `Log.e`, nie wyciekając do interfejsu graficznego użytkownika.

---

## 7. Testy ręczne
Zweryfikowano poprawność działania aplikacji w następujących scenariuszach:
1.  *Test braku internetu*: Po włączeniu trybu samolotowego i próbie wyszukiwania aplikacja poprawnie wyświetliła błąd sieciowy oraz zablokowała przyciski w tryb „Retry”. Po przywróceniu sieci i kliknięciu „Retry” książki zostały poprawnie wczytane.
2.  *Test spamu*: Szybkie, wielokrotne klikanie przycisku „Go” nie wywołało równoległych żądań HTTP (przycisk natychmiast stał się nieaktywny w stanie `Loading`).
3.  *Test pustego zapytania*: Wpisanie samych spacji lub pustego pola tekstowego nie wywołuje żadnego zapytania API (przycisk „Go” jest nieaktywny).
4.  *Test limitu 429*: Emulacja błędu 429 zablokowała wyszukiwanie na dokładnie 5 sekund, informując o konieczności odczekania, po czym interfejs powrócił do stanu używalności.

---

## 8. Problemy napotkane podczas implementacji
Największym wyzwaniem była stabilna obsługa limitu zapytań bez rejestracji płatnego klucza Google API. Problem ten rozwiązano po stronie klienta poprzez odpowiednie blokowanie akcji UI (Go / Retry), wprowadzenie timera blokady oraz zapobieganie wysyłaniu duplikatów zapytań w trakcie ładowania.

---

## 9. Plan dalszych prac (do Raportu nr 3)
*   Przeniesienie opcjonalnego klucza API do pliku `local.properties` i wstrzykiwanie go przez buildConfig.
*   Zaimplementowanie Live Search ze stabilnym debouncingiem (300 ms) w strumieniu Flow.
*   Dodanie mechanizmu cache wyników wyszukiwania w warstwie repozytorium.
*   Przygotowanie testów jednostkowych dla ViewModelu oraz repozytorium przy użyciu MockWebServer.

---

## 10. Wnioski
Aplikacja w wersji na Raport nr 2 cechuje się dużą stabilnością i odpornością na błędy komunikacji sieciowej. Zastosowanie Jetpack Compose wraz ze spójną obsługą stanów w ViewModelu pozwoliło na eleganckie odzwierciedlenie ograniczeń sieciowych (jak HTTP 429 czy brak połączenia) bezpośrednio w interfejsie użytkownika, podnosząc komfort korzystania z aplikacji w warunkach mobilnych.
