# BookShelf Planner

Simple Android application for keeping a personal reading list.
Search books on Google Books, save the ones you are interested in,
mark their reading status and add your own notes.

The app is written in Kotlin with Jetpack Compose and Room.

## Current status

The project has fully implemented:
- MVVM Architecture with manual DI (`AppContainer`).
- Local SQLite database using Room with data observation via Kotlin Flows.
- Dynamic network requests to the Google Books API (Retrofit, Moshi, OkHttp).
- Clean three-screen workflow (Home, Search, Book Details) using Navigation Compose.
- Prevented duplicate searches, rapid spamming, and blank queries.
- Refactored error state handling to support robust offline and rate-limited environments.

## Features

- Search books by title, author or topic (Google Books API).
- Save selected books to a local list.
- Local persistence with Room.
- Three reading statuses: *To read*, *Reading*, *Finished*.
- Filter saved books by status.
- Add and edit a personal note for each book.
- Delete books from the list.
- Separate screens for Home, Search and Book details (Navigation Compose).
- Handles loading, empty and error states.

## Network error handling

The application communicates with the Google Books API and includes robust mechanisms to prevent and handle connection issues and API rate limits gracefully:
- **HTTP 429 (Too Many Requests)**: Handled by displaying a clear message: *"Too many requests. Please wait a moment and try again."*. Upon encountering a 429, a 5-second cooldown is activated.
- **HTTP 503 (Service Unavailable)**: Handled by showing: *"Google Books is temporarily unavailable. Please try again later."*.
- **No Internet / Connection Offline**: Caught as an `IOException` (e.g. timeout or host resolution failure) and presented to the user as: *"No internet connection. Check your network and try again."*.
- **Other Failures**: Generic fallback showing *"Something went wrong while searching books."*.
- **Technical Detail Logs**: Internal exception traces and errors are logged using standard `Log.e` for debugging and are kept out of the user interface.
- **Request Prevention**:
  - The **Go** and **Retry** buttons are automatically disabled during active loading state or during the active rate-limit cooldown.
  - Software keyboard search (IME Action) is blocked if buttons are disabled.
  - Short 1-second cooldown checks prevent repeated queries if the button is clicked multiple times rapidly.
  - Blank searches (consisting of empty strings or whitespaces) are blocked from being sent to the network.

## Tech stack

- Kotlin
- Jetpack Compose + Material 3
- Navigation Compose
- ViewModel + Kotlin Coroutines / Flow
- Room (local database)
- Retrofit + Moshi (Google Books API)
- Coil (image loading)

## Project structure

```
app/src/main/java/com/dawidchmiel/bookshelfplanner/
├── MainActivity.kt
├── BookShelfApplication.kt
├── AppContainer.kt
├── model/                  # Book, BookStatus
├── data/
│   ├── local/              # Room: BookEntity, BookDao, BookDatabase, Converters
│   ├── remote/             # Retrofit: GoogleBooksApi, DTOs
│   └── repository/         # BookRepository
└── ui/
    ├── screens/            # HomeScreen, SearchScreen, BookDetailScreen, ViewModel
    └── theme/              # BookShelfTheme
```

## Requirements

- Android Studio Ladybug or newer
- Android SDK 35
- JDK 17 (or Android Studio's embedded JBR)
- A device or emulator with Android 8.0 (API 26) or higher

## How to run

1. Clone the repository:
   ```bash
   git clone https://github.com/Kauszyn/bookshelf-planner.git
   ```
2. Open the project folder in Android Studio (open the root `bookshelf-planner` folder, not the `app` folder).
3. Wait for the Gradle sync to finish.
4. Pick an emulator or connect a device and press **Run**.

The Google Books API does not require an API key for basic search,
so no extra configuration is needed.

## Manual test scenario

1. Start the app.
2. Open **Search** and type for example `Kotlin` or `Clean Code`.
3. Add a few books to your list.
4. Go back to **Home**.
5. Open one of the saved books, change its status and save a note.
6. Close and reopen the app — the data should still be there.

## Planned improvements

- Sorting on the Home screen (title, status, page count).
- Search input debouncing.
- Snackbar confirmation after saving a book.
- Reading progress field (current page).
- Unit tests for the repository.
- Better empty-state graphics.

## License

This project is released under the MIT License. See [LICENSE](LICENSE) for details.
