package com.example.walkie.viewmodel

import android.app.Application
import android.content.Context
import android.content.res.Resources
import android.content.res.loader.ResourcesProvider
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.walkie.R
import android.view.View
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.lifecycle.LiveData
import com.example.walkie.model.Achievement
import kotlin.coroutines.coroutineContext


class AchievementsListAdapter (var achievements:LiveData<List<Achievement>>, var viewModel:UserViewModel, var context: Context): RecyclerView.Adapter<AchievementsListAdapter.AchievementsHolder>() {

    inner class AchievementsHolder(view:View):RecyclerView.ViewHolder(view)


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int):AchievementsHolder{
        val view= LayoutInflater.from(parent.context).inflate(R.layout.achievement_layout,parent,false)

        return AchievementsHolder(view)
    }

    override fun onBindViewHolder(holder: AchievementsHolder, position: Int) {

        val name = holder.itemView.findViewById<TextView>(R.id.achievement_name)
        val description = holder.itemView.findViewById<TextView>(R.id.achievement_description)
        val icon = holder.itemView.findViewById<ImageView>(R.id.achievement_icon)
        val row = holder.itemView.findViewById<LinearLayout>(R.id.achievement_row)
        val progress = holder.itemView.findViewById<TextView>(R.id.achievement_progress)
        val progressBar = holder.itemView.findViewById<ProgressBar>(R.id.achievement_progressBar)

        name.text = achievements.value!![position].title
        description.text = achievements.value!![position].description
        val iconResource = context.resources.getIdentifier(achievements.value!![position].iconPath,"drawable",context.packageName)
        icon.setImageResource(iconResource)

        if(achievements.value!![position].stage == 0){
            row.setBackgroundColor(Color.parseColor("#febeb4"))
            name.setBackgroundColor(Color.parseColor("#ed8e7c"))
            description.setBackgroundColor(Color.parseColor("#ed8e7c"))
            icon.setBackgroundColor(Color.parseColor("#ed8e7c"))
            progress.setBackgroundColor(Color.parseColor("#ed8e7c"))
        }


        checkAchievements(position, progressBar, progress)
    }

    fun checkAchievements(position: Int, progressBar: ProgressBar, progressText: TextView){
        when(position){
            0->{
                progressBar.max = achievements.value!![position].stage * 10 + 5
                progressBar.progress = 3
            }
            1->{
                progressBar.max = achievements.value!![position].stage * 4 + 1
                progressBar.progress = 1
            }
            2->{
                progressBar.max = achievements.value!![position].stage * 20 + 5
                progressBar.progress = 6
            }
            3->{
                progressBar.max = achievements.value!![position].stage * 2 + 5
                progressBar.progress = 0
            }
        }
        progressText.text = progressBar.progress.toString()+"/"+progressBar.max.toString()

    }


    override fun getItemCount(): Int {
        return achievements.value?.size?:0
    }
}
