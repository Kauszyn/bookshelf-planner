package com.dawidchmiel.bookshelfplanner.ui.screens

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dawidchmiel.bookshelfplanner.data.repository.BookRepository
import com.dawidchmiel.bookshelfplanner.model.Book
import com.dawidchmiel.bookshelfplanner.model.BookStatus
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

sealed interface SearchUiState {
    data object Idle : SearchUiState
    data object Loading : SearchUiState
    data class Success(val books: List<Book>) : SearchUiState
    data class Error(val message: String) : SearchUiState
}

class BookShelfViewModel(private val repository: BookRepository) : ViewModel() {
    val savedBooks: StateFlow<List<Book>> = repository.savedBooks.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList()
    )

    private val _query = MutableStateFlow("")
    val query = _query.asStateFlow()

    private val _searchState = MutableStateFlow<SearchUiState>(SearchUiState.Idle)
    val searchState = _searchState.asStateFlow()

    // Holds state for HTTP 429 rate-limiting cooldown period to temporarily disable retry/search
    private val _isRateLimitCooldownActive = MutableStateFlow(false)
    val isRateLimitCooldownActive = _isRateLimitCooldownActive.asStateFlow()

    private var lastSearchTime = 0L
    private val searchCooldownMs = 1000L // 1 second general search cooldown

    fun onQueryChange(value: String) { _query.value = value }

    fun search() {
        val currentQuery = query.value.trim()
        if (currentQuery.isEmpty()) {
            // Prevent blank searches from triggering API requests
            return
        }
        if (currentQuery.length < 2) {
            _searchState.value = SearchUiState.Error("Type at least 2 characters.")
            return
        }

        // Avoid concurrent requests and check for cooldown period to prevent spamming
        if (_searchState.value is SearchUiState.Loading) return
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastSearchTime < searchCooldownMs) return
        lastSearchTime = currentTime

        viewModelScope.launch {
            _searchState.value = SearchUiState.Loading
            _searchState.value = runCatching { repository.searchBooks(currentQuery) }
                .fold(
                    onSuccess = { SearchUiState.Success(it) },
                    onFailure = { throwable ->
                        Log.e("BookShelfViewModel", "Search failed internally: ${throwable.message}", throwable)
                        val errorMessage = when (throwable) {
                            is HttpException -> {
                                when (throwable.code()) {
                                    429 -> {
                                        // Handles rate limiting to block rapid retries and inform user
                                        startRateLimitCooldown()
                                        "Too many requests. Please wait a moment and try again."
                                    }
                                    503 -> "Google Books is temporarily unavailable. Please try again later."
                                    else -> "Something went wrong while searching books."
                                }
                            }
                            is IOException -> {
                                "No internet connection. Check your network and try again."
                            }
                            else -> {
                                "Something went wrong while searching books."
                            }
                        }
                        SearchUiState.Error(errorMessage)
                    }
                )
        }
    }

    private fun startRateLimitCooldown() {
        viewModelScope.launch {
            _isRateLimitCooldownActive.value = true
            delay(5000L) // 5 seconds cooldown after hitting HTTP 429
            _isRateLimitCooldownActive.value = false
        }
    }

    fun save(book: Book) = viewModelScope.launch { repository.saveBook(book) }
    fun delete(book: Book) = viewModelScope.launch { repository.deleteBook(book) }
    fun updateStatus(book: Book, status: BookStatus) = viewModelScope.launch { repository.updateStatus(book, status) }
    fun updateNote(book: Book, note: String) = viewModelScope.launch { repository.updateNote(book, note) }
}
