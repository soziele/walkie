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

class WalkViewModel(application: Application): AndroidViewModel(application) {
    private val walkRepository: WalkRepository = WalkRepository(WalkieDatabase.getDatabase(application).walkDao())

    val walks: LiveData<List<Walk>> = walkRepository.getAll

    var activeWalk: LiveData<Walk> = walkRepository.getActiveWalk

    init{

    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun addWalk(checkpoints: Array<LatLng>, length: Double)
    {
        val visitedCheckpoints = BooleanArray(checkpoints.size) { false }
        val walk = Walk(checkpoints = checkpoints, length = length, date = LocalDateTime.now(), visitedCheckpoints = visitedCheckpoints, id = 0, isComplete = false)

        viewModelScope.launch {
            walkRepository.add(walk)
        }
    }

    fun completeWalk(walk: Walk)
    {
        viewModelScope.launch {
            walkRepository.completeWalk(walk)
        }
    }
}