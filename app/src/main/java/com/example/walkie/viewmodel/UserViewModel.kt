package com.example.walkie.viewmodel

import android.app.Application
import androidx.lifecycle.*
import com.example.walkie.R
import com.example.walkie.model.Achievement
import com.example.walkie.model.Walk
import com.example.walkie.model.WalkieDatabase
import com.example.walkie.model.repositories.AchievementRepository
import com.example.walkie.model.repositories.WalkRepository
import kotlinx.coroutines.launch

class UserViewModel(application: Application): AndroidViewModel(application) {

    val achievementViewModel: AchievementViewModel = AchievementViewModel(application)
    val walkViewModel: WalkViewModel = WalkViewModel(application)
}