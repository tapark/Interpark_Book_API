package com.example.book_finder.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "history")
data class History(
    @PrimaryKey val uid: Int?,
    @ColumnInfo(name = "db_keyword") val keyword: String?
)
