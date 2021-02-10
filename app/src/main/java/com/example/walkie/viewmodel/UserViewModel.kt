package com.example.walkie.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import com.example.walkie.R
import com.example.walkie.model.Achievement

class UserViewModel(application: Application): AndroidViewModel(application) {

    var allAchievements: List<Achievement>

    init{
        allAchievements = listOf(Achievement("First walk","Congrats lol",R.drawable.first_walk_unlocked,true), Achievement("First week walking","Congrats lol", R.drawable.one_week_combo_unlocked,true), Achievement("Something else","Congrats lol",R.drawable.first_walk,false))
    }
}