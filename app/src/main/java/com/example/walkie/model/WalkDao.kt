package com.example.walkie.model

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface WalkDao {
    @Insert
    suspend fun insert(walk: Walk)

    @Delete
    suspend fun delete(walk: Walk)

    @Update
    suspend fun update(walk: Walk)

    @Query("SELECT * FROM walk")
    fun getAll(): LiveData<List<Walk>>

    @Query("SELECT * FROM walk WHERE state = 0")
    fun getActive(): Walk

    @Query("UPDATE walk SET state = 1, distance_traveled = :distance WHERE id = :walkId")
    fun finishWalk(walkId: Int, distance: Double)

    @Query("UPDATE walk SET state = 2 WHERE id = :walkId")
    fun cancelWalk(walkId: Int)

}