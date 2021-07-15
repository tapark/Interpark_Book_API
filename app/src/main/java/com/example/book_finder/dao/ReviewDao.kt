package com.example.book_finder.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.book_finder.model.Review

@Dao
interface ReviewDao {

    @Query("SELECT * FROM review WHERE id == :id")
    fun getOneReview(id: Int): Review?

    // 동일한 id의 리뷰가 있으면 대체됨
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun saveReview(review: Review)
}