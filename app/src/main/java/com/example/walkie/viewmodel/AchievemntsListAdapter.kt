package com.example.walkie.viewmodel

import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.walkie.R
import android.view.View
import android.widget.*
import androidx.lifecycle.LiveData
import com.example.walkie.model.Achievement


class AchievementsListAdapter (var achievements:List<Achievement>, var viewModel:UserViewModel): RecyclerView.Adapter<AchievementsListAdapter.AchievementsHolder>() {

    inner class AchievementsHolder(view:View):RecyclerView.ViewHolder(view)


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int):AchievementsHolder{
        val view= LayoutInflater.from(parent.context).inflate(R.layout.achievement_layout,parent,false)

        return AchievementsHolder(view)
    }

    override fun onBindViewHolder(holder: AchievementsHolder, position: Int) {

        var name = holder.itemView.findViewById<TextView>(R.id.achievement_name)
        var description = holder.itemView.findViewById<TextView>(R.id.achievement_description)
        var icon = holder.itemView.findViewById<ImageView>(R.id.achievement_icon)
        var row = holder.itemView.findViewById<LinearLayout>(R.id.achievement_row)
        var progress = holder.itemView.findViewById<TextView>(R.id.achievement_progress)
        var progressBar = holder.itemView.findViewById<ProgressBar>(R.id.achievement_progressBar)

        name.text = achievements[position].title
        description.text = achievements[position].description
        icon.setImageResource(achievements[position].iconPath)

        if(achievements[position].received == false){
            row.setBackgroundColor(Color.parseColor("#febeb4"))
            name.setBackgroundColor(Color.parseColor("#ed8e7c"))
            description.setBackgroundColor(Color.parseColor("#ed8e7c"))
            icon.setBackgroundColor(Color.parseColor("#ed8e7c"))
            progress.setBackgroundColor(Color.parseColor("#ed8e7c"))
            progressBar.max = 10
            progressBar.progress = 2
        }


    }

    override fun getItemCount(): Int {
        return achievements.size?:0
    }
}
