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

    @Query("SELECT * FROM walk WHERE is_complete = 0 AND id=(SELECT max(id) FROM walk)")
    fun getActive(): LiveData<Walk>

    @Query("UPDATE walk SET is_complete = 1 WHERE id = :walkId")
    fun finishWalk(walkId: Int)

}