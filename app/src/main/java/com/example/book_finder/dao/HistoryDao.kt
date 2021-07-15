package com.example.book_finder.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.book_finder.model.History

@Dao
interface HistoryDao {

    @Query("SELECT * FROM history")
    fun getAll(): List<History>

    @Insert
    fun insertHistory(history: History)

    @Query("DELETE FROM history WHERE db_keyword == :keyword")
    fun delete(keyword: String)
}