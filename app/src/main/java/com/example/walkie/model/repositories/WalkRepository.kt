package com.example.walkie.model.repositories

import android.util.Log
import androidx.lifecycle.LiveData
import com.example.walkie.model.Walk
import com.example.walkie.model.WalkDao

class WalkRepository(private val walkDao: WalkDao) {
    val getAll: LiveData<List<Walk>> = walkDao.getAll()

    suspend fun add(walk: Walk) = walkDao.insert(walk)

    suspend fun delete(walk: Walk) = walkDao.delete(walk)

    suspend fun update(walk: Walk) = walkDao.update(walk)

    var getActiveWalk: Walk = walkDao.getActive()

    fun completeWalk(walk: Walk, distance: Double) = walkDao.finishWalk(walkId = walk.id, distance = distance)

    fun cancelWalk(walk: Walk) = walkDao.cancelWalk(walkId = walk.id)

    fun hasBeenCompletedToday(): Boolean {
        walkDao.getTodayCompleted() ?: return false
        return true
    }

    fun deleteAll() = walkDao.deteleAll()
}