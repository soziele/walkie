package com.example.walkie.viewmodel

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import com.example.walkie.model.enums.Difficulty
import com.google.gson.Gson
import java.lang.Exception
import java.util.*

class State(
    var difficulty: Difficulty = Difficulty.Easy,
    var unlockedCheckpoints: Int = 0,
    var distanceTraveled: Double = 0.0,
    var lastCompletedWalkDate: Date = Date(0),
    var completedWalkCombo: Int = 0
)

class StateViewModel(application: Application): AndroidViewModel(application) {
    private val filename = "state.json"
    companion object {
        lateinit var state: State
        fun isStateInitialized() = ::state.isInitialized
    }

    init {
        if(!isStateInitialized()) {
            initialize()
        }
    }

    private fun initialize() {
        try {
            // if file exists, load it
            getApplication<Application>().openFileInput(filename).bufferedReader().use { data ->
                state = Gson().fromJson<State>(data, State::class.java)
            }
        }
        catch(e: Exception) {
            // if file doesn't exist, initialize state with basic values
            state = State()
        }
    }

    private fun writeStateToFile() {
        getApplication<Application>().openFileOutput(filename, Context.MODE_PRIVATE).use {
            it.write(Gson().toJson(state).toByteArray())
        }
    }

    fun setDifficulty(difficulty: Difficulty) {
        Log.i("STATE UPDATE", "State updated from ${state.difficulty} to $difficulty")
        state.difficulty = difficulty
        writeStateToFile()
    }

    fun addDistanceAndCheckpoint(distance: Double){
        Log.i("STATE UPDATE", "State updated from ${state.unlockedCheckpoints} to ${state.unlockedCheckpoints+1}")
        state.unlockedCheckpoints++
        Log.i("STATE UPDATE", "State updated from ${state.distanceTraveled} to ${state.distanceTraveled + distance}")
        state.distanceTraveled += distance
        writeStateToFile()
    }

    fun addCheckpoint() {
        Log.i("STATE UPDATE", "State updated from ${state.unlockedCheckpoints} to ${state.unlockedCheckpoints+1}")
        state.unlockedCheckpoints++
        writeStateToFile()
    }

    fun addDistanceTraveled(distance: Double) {
        Log.i("STATE UPDATE", "State updated from ${state.distanceTraveled} to ${state.distanceTraveled + distance}")
        state.distanceTraveled += distance
        writeStateToFile()
    }

    fun addWalkToCombo() {
        val now = Date()
        //if last walk happened more than a day ago
        if(now.time - state.lastCompletedWalkDate.time > 86400000) {
            state.completedWalkCombo = 1
        } else {
            state.completedWalkCombo++
        }

        state.lastCompletedWalkDate = now
        writeStateToFile()
    }

    fun getState(): State = state

    fun cleanState() {
        getApplication<Application>().deleteFile(filename)
        initialize()
    }
}