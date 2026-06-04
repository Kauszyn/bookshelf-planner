package com.dawidchmiel.bookshelfplanner

import android.content.Context
import com.dawidchmiel.bookshelfplanner.data.local.BookDatabase
import com.dawidchmiel.bookshelfplanner.data.remote.GoogleBooksApi
import com.dawidchmiel.bookshelfplanner.data.repository.BookRepository
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

class AppContainer(context: Context) {
    private val database = BookDatabase.create(context)

    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private val httpClient = OkHttpClient.Builder()
        .addInterceptor(HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BASIC })
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl("https://www.googleapis.com/books/v1/")
        .client(httpClient)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()

    private val api = retrofit.create(GoogleBooksApi::class.java)

    val repository = BookRepository(
        dao = database.bookDao(),
        api = api,
        apiKey = BuildConfig.GOOGLE_BOOKS_API_KEY.takeIf { it.isNotBlank() }
    )
}
