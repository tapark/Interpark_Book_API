package com.example.book_finder

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.book_finder.dao.HistoryDao
import com.example.book_finder.dao.ReviewDao
import com.example.book_finder.model.History
import com.example.book_finder.model.Review

@Database(entities = [History::class, Review::class], version = )
abstract class AppDatabase: RoomDatabase() {
    abstract fun historyDao(): HistoryDao
    abstract fun reviewDao(): ReviewDao
}

fun getAppDatabase(context: Context): AppDatabase {

    val migration1To2 = object: Migration(1, 2) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL("CREATE TABLE `REVIEW` (`id` INTEGER, `review` TEXT, PRIMARY KEY(`id`))")
        }

    }

    return Room.databaseBuilder(context, AppDatabase::class.java, "BookSearchDB")
        .addMigrations(migration1To2).build()
}