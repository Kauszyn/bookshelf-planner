package com.dawidchmiel.bookshelfplanner.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.dawidchmiel.bookshelfplanner.model.Book
import com.dawidchmiel.bookshelfplanner.model.BookStatus

@Entity(tableName = "books")
data class BookEntity(
    @PrimaryKey val id: String,
    val title: String,
    val authors: String,
    val description: String,
    val thumbnailUrl: String?,
    val publishedDate: String?,
    val pageCount: Int?,
    val status: BookStatus,
    val personalNote: String,
    val dateAdded: Long
)

fun BookEntity.toBook() = Book(
    id = id,
    title = title,
    authors = authors,
    description = description,
    thumbnailUrl = thumbnailUrl,
    publishedDate = publishedDate,
    pageCount = pageCount,
    status = status,
    personalNote = personalNote,
    dateAdded = dateAdded
)

fun Book.toEntity() = BookEntity(
    id = id,
    title = title,
    authors = authors,
    description = description,
    thumbnailUrl = thumbnailUrl,
    publishedDate = publishedDate,
    pageCount = pageCount,
    status = status,
    personalNote = personalNote,
    dateAdded = dateAdded
)
