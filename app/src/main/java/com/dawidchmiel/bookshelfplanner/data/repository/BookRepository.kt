package com.dawidchmiel.bookshelfplanner.data.repository

import com.dawidchmiel.bookshelfplanner.data.local.BookDao
import com.dawidchmiel.bookshelfplanner.data.local.toBook
import com.dawidchmiel.bookshelfplanner.data.local.toEntity
import com.dawidchmiel.bookshelfplanner.data.remote.GoogleBooksApi
import com.dawidchmiel.bookshelfplanner.data.remote.toBook
import com.dawidchmiel.bookshelfplanner.model.Book
import com.dawidchmiel.bookshelfplanner.model.BookStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class BookRepository(
    private val dao: BookDao,
    private val api: GoogleBooksApi,
    private val apiKey: String? = null
) {
    val savedBooks: Flow<List<Book>> = dao.observeBooks().map { entities -> entities.map { it.toBook() } }

    fun observeBook(id: String): Flow<Book?> = dao.observeBook(id).map { it?.toBook() }

    suspend fun searchBooks(query: String): List<Book> =
        api.searchBooks(query = query.trim(), key = apiKey).items.map { it.toBook() }

    suspend fun saveBook(book: Book) = dao.upsert(book.toEntity())

    suspend fun deleteBook(book: Book) = dao.delete(book.toEntity())

    suspend fun updateStatus(book: Book, status: BookStatus) = dao.upsert(book.copy(status = status).toEntity())

    suspend fun updateNote(book: Book, note: String) = dao.upsert(book.copy(personalNote = note).toEntity())
}
