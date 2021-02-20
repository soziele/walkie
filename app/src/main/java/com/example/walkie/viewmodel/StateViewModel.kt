package com.example.walkie.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import com.example.walkie.model.enums.Difficulty
import com.google.gson.Gson
import java.lang.Exception

class State(var difficulty: Difficulty, var unlockedCheckpoints: Int, var distanceTraveled: Double)

class StateViewModel(application: Application): AndroidViewModel(application) {
    private val filename = "state.json"
    var state: State;

    init {
        try {
            // if file exists, load it
            application.openFileInput(filename).bufferedReader().use { data ->
                state = Gson().fromJson<State>(data, State::class.java)
            }
        }
        catch(e: Exception) {
            // if file doesn't exist, initialize state with basic values
            state = State(Difficulty.Easy, 0, 0.0)
        }
    }

    private fun writeStateToFile() {
        getApplication<Application>().openFileOutput(filename, Context.MODE_PRIVATE).use {
            it.write(Gson().toJson(state).toByteArray())
        }
    }

    fun setDifficulty(difficulty: Difficulty) {
        state.difficulty = difficulty
        writeStateToFile()
    }

    fun addCheckpoint() {
        state.unlockedCheckpoints++
        writeStateToFile()
    }

    fun addDistanceTraveled(distance: Double) {
        state.distanceTraveled += distance
        writeStateToFile()
    }
}