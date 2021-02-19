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
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.util.*

class WalkViewModel(application: Application): AndroidViewModel(application) {
    private val walkRepository: WalkRepository

    val walks: LiveData<List<Walk>>

    var activeWalk: LiveData<Walk>
    init{
        walkRepository = WalkRepository(WalkieDatabase.getDatabase(application).walkDao())
        walks = walkRepository.getAll
        activeWalk = walkRepository.getActiveWalk
    }

    fun addWalk(checkpoints: Array<LatLng>, length: Double)
    {
        val visitedCheckpoints = BooleanArray(checkpoints.size) { false }
        val walk = Walk(checkpoints = checkpoints, length = length, date = Date(), visitedCheckpoints = visitedCheckpoints, id = 0, isComplete = false)

        viewModelScope.launch {
            walkRepository.add(walk)
            walkRepository.getActiveWalk
        }
    }

    fun updateWalk(walk: Walk){
        viewModelScope.launch {
            walkRepository.update(walk)
        }
    }

    fun completeWalk(walk: Walk)
    {
        viewModelScope.launch {
            walkRepository.completeWalk(walk)
        }
    }


}