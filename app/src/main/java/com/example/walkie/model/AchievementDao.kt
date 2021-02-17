package com.example.walkie.model

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface AchievementDao {
    @Insert
    suspend fun insert(achievement: Achievement)

    @Delete
    suspend fun delete(achievement: Achievement)

    @Query("SELECT * FROM achievement")
    fun getAll(): LiveData<List<Achievement>>

    @Update(entity = Achievement::class)
    fun update(achievement: Achievement)
}