package com.example.walkie.viewmodel

import android.app.Application
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.example.walkie.model.Walk
import com.example.walkie.model.WalkieDatabase
import com.example.walkie.model.repositories.WalkRepository
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.*
import java.time.LocalDateTime
import java.util.*

class WalkViewModel(application: Application): AndroidViewModel(application) {
    private var walkRepository: WalkRepository

    var walks: LiveData<List<Walk>>

    var activeWalk: Walk
    init{

            walkRepository = WalkRepository(WalkieDatabase.getDatabase(application).walkDao())
            walks = walkRepository.getAll

        activeWalk = walkRepository.getActiveWalk
    }

    fun addWalk(checkpoints: Array<LatLng>, length: Double)
    {
        val visitedCheckpoints = BooleanArray(checkpoints.size) { false }
        val walk = Walk(checkpoints = checkpoints, length = length, date = Date(), visitedCheckpoints = visitedCheckpoints, id = 0)

        runBlocking {
            walkRepository.add(walk)
        }

    }

    fun getAllWalks(){
        walks = walkRepository.getAll
    }

    fun getActiveWalk(){
        runBlocking {
            val count1: Deferred<Unit> = async(context = Dispatchers.IO) {walkRepository = WalkRepository(WalkieDatabase.getDatabase(this@WalkViewModel.getApplication()).walkDao())}
            val count2: Deferred<Unit> = async(context = Dispatchers.IO) {walks = walkRepository.getAll}

            count1.await()
            count2.await()
            activeWalk = walkRepository.getActiveWalk
        }

    }

    fun updateWalk(walk: Walk){
        viewModelScope.launch {
            walkRepository.update(walk)
        }
    }

    fun completeWalk(walk: Walk, distance: Double)
    {
        viewModelScope.launch {
            walkRepository.completeWalk(walk, distance)
        }
    }

    fun cancelWalk(walk: Walk){
        runBlocking {
            walkRepository.cancelWalk(walk)
        }
    }

    fun hasCompletedWalkToday(): Boolean {
        return walkRepository.hasBeenCompletedToday()
    }
}