package com.dawidchmiel.bookshelfplanner.data.remote

import retrofit2.http.GET
import retrofit2.http.Query

interface GoogleBooksApi {
    @GET("volumes")
    suspend fun searchBooks(
        @Query("q") query: String,
        @Query("key") key: String? = null,
        @Query("maxResults") maxResults: Int = 20,
        @Query("printType") printType: String = "books"
    ): GoogleBooksResponse
}
