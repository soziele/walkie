package com.example.walkie.model.repositories

import androidx.lifecycle.LiveData
import com.example.walkie.model.Achievement
import com.example.walkie.model.AchievementDao

class AchievementRepository(private val achievementDao: AchievementDao) {
    val getAll: LiveData<List<Achievement>> = achievementDao.getAll()

    suspend fun add(achievement: Achievement) = achievementDao.insert(achievement)

    suspend fun delete(achievement: Achievement) = achievementDao.delete(achievement)

    fun transitionToNextStage(achievement: Achievement) {
        achievement.stage += 1
        achievementDao.update(achievement)
    }
}