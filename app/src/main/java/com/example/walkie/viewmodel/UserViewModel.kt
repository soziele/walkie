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
    val achievements: MutableLiveData<List<Achievement>>

    init{
        achievements = MutableLiveData(
            listOf(Achievement(id=0,title="Big foot",description="Walk a lot of kilometres.", iconPath="one_week_combo",stage=1),
            Achievement(id=0,title="Walkaholic",description="Go on a lot of walks.", iconPath="one_week_combo",stage=1),
                Achievement(id=0,title="Collector",description="Collect a lot of checkpoints.", iconPath="one_week_combo",stage=2),
                Achievement(id=0,title="Combo maker",description="Go on walks a lot of days in a row.", iconPath="one_week_combo",stage=1)))
    }
}