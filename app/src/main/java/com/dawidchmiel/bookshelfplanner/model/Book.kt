package com.dawidchmiel.bookshelfplanner.model

data class Book(
    val id: String,
    val title: String,
    val authors: String,
    val description: String,
    val thumbnailUrl: String?,
    val publishedDate: String?,
    val pageCount: Int?,
    val status: BookStatus = BookStatus.TO_READ,
    val personalNote: String = "",
    val dateAdded: Long = System.currentTimeMillis()
)
