package com.dawidchmiel.bookshelfplanner.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(entities = [BookEntity::class], version = 2, exportSchema = true)
@TypeConverters(Converters::class)
abstract class BookDatabase : RoomDatabase() {
    abstract fun bookDao(): BookDao

    companion object {
        fun create(context: Context): BookDatabase = Room.databaseBuilder(
            context.applicationContext,
            BookDatabase::class.java,
            "bookshelf.db"
        ).fallbackToDestructiveMigration()
            .build()
    }
}
