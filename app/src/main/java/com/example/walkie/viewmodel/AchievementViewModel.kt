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
    private val achievementRepository: AchievementRepository =
        AchievementRepository(WalkieDatabase.getDatabase(application).achievementDao())
    val achievements: LiveData<List<Achievement>> = achievementRepository.getAll

    fun transitionToNextStage(achievement: Achievement)
    {
        viewModelScope.launch {
            achievementRepository.transitionToNextStage(achievement)
        }
    }

    private fun addAchievement(achievement: Achievement){
        viewModelScope.launch {
            achievementRepository.add(achievement)
        }
    }

    fun seedDatabase() {
        addAchievement(Achievement(id = 0, title = "Walkin 'n Rockin", description = "The more walks you complete, the higher level you unlock!", iconPath = "walks_achievement"))
        addAchievement(Achievement(id = 0, title = "Marathon walker", description = "The further the better!", iconPath = "kilometers_achievement_locked"))
        addAchievement(Achievement(id = 0, title = "Everyday I'm walkin", description = "Can you complete walks every day? Let's see for how long!" , iconPath = "days_achievement"))
        addAchievement(Achievement(id = 0, title = "Walking apPOINTment", description = "Collect as many checkpoints as possible!", iconPath = "checkpoints_achievement"))
    }
}