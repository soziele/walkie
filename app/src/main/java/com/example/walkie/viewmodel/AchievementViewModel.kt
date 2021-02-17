package com.example.walkie.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.example.walkie.model.Achievement
import com.example.walkie.model.WalkieDatabase
import com.example.walkie.model.repositories.AchievementRepository
import kotlinx.coroutines.launch

class AchievementViewModel(application: Application): AndroidViewModel(application) {
    private val achievementRepository: AchievementRepository = AchievementRepository(WalkieDatabase.getDatabase(application).achievementDao())

    val achievements: LiveData<List<Achievement>> = achievementRepository.getAll

    fun transitionToNextStage(achievement: Achievement)
    {
        viewModelScope.launch {
            achievementRepository.transitionToNextStage(achievement)
        }
    }
}