package com.example.walkie.viewmodel

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.DialogInterface
import android.content.res.Resources
import android.content.res.loader.ResourcesProvider
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.util.Range
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.walkie.R
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import com.example.walkie.model.Achievement
import com.example.walkie.model.enums.WalkState
import com.example.walkie.view.MainActivity
import kotlin.coroutines.coroutineContext
import kotlin.math.roundToInt


class AchievementsListAdapter (var achievements:LiveData<List<Achievement>>, var viewModel:UserViewModel, var stateViewModel: StateViewModel, var context: Context, var activity: Activity, var viewLifecycleOwner: LifecycleOwner): RecyclerView.Adapter<AchievementsListAdapter.AchievementsHolder>() {

    inner class AchievementsHolder(view:View):RecyclerView.ViewHolder(view)
    val walksLevels = arrayOf(1, 5, 10, 15, 20, 30, 40, 50, 60, 70, 80, 90, 100, 200, 300, 400, 500)
    val kilometersLevels = arrayOf(5, 10, 20, 30, 50, 75, 100, 125, 150, 200, 250, 300)
    val daysLevels = arrayOf(2, 4, 6, 8, 10, 15, 20, 25, 30, 35, 40, 50, 60, 70, 80, 90, 100)
    val checkpointsLevels = arrayOf(8, 16, 24, 32, 40, 56, 75, 100, 128, 150, 200, 250, 300)


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

        checkAchievements(position, progressBar, progress, description)

        name.text = achievements.value!![position].title
        if(achievements.value!![position].stage ==0) description.text = achievements.value!![position].description+ "\nNot unlocked yet."
        else description.text = achievements.value!![position].description+"\nLevel: "+achievements.value!![position].stage
        val iconResource = context.resources.getIdentifier(achievements.value!![position].iconPath,"drawable",context.packageName)
        icon.setImageResource(iconResource)

        if(achievements.value!![position].stage == 0){
            row.setBackgroundColor(Color.parseColor("#febeb4"))
            name.setBackgroundColor(Color.parseColor("#ed8e7c"))
            description.setBackgroundColor(Color.parseColor("#ed8e7c"))
            icon.setBackgroundColor(Color.parseColor("#ed8e7c"))
            progress.setBackgroundColor(Color.parseColor("#ed8e7c"))
        }


    }

    fun checkAchievements(position: Int, progressBar: ProgressBar, progressText: TextView, description: TextView){
        when(position){
            0->{
                var tmpStage = 0
                var progress = getCompletedWalksNumber()

                progressBar.max = walksLevels[0]
                for(i in walksLevels.indices){
                    if(progress > walksLevels[i]){
                        progressBar.max = walksLevels[i+1]
                        tmpStage++
                    }
                }
                while(tmpStage > achievements.value!![0].stage){
                    viewModel.achievementViewModel.transitionToNextStage(achievements.value!![0])
                    congratulationsDialog(achievements.value!![0])
                }
                progressBar.progress = progress
                progressText.text = progressBar.progress.toString()+"/"+progressBar.max.toString()
            }
            1->{
                var tmpStage = 0
                for(i in kilometersLevels.indices){

                    progressBar.max = kilometersLevels[0]
                    if(stateViewModel.getState().distanceTraveled/1000 > kilometersLevels[i]){
                        progressBar.max = kilometersLevels[i+1]
                        tmpStage++
                    }
                }
                while(tmpStage > achievements.value!![1].stage){
                    viewModel.achievementViewModel.transitionToNextStage(achievements.value!![1])
                    congratulationsDialog(achievements.value!![1])
                }
                progressBar.progress = (stateViewModel.getState().distanceTraveled/1000).toInt()
                progressText.text = progressBar.progress.toString()+"/"+progressBar.max.toString()+" km"
            }
            2->{
                var tmpStage = 0
                var progress = getDaysInRowNumber()

                progressBar.max = daysLevels[0]
                for(i in daysLevels.indices){
                    if(progress > daysLevels[i]){
                        progressBar.max = daysLevels[i+1]
                        tmpStage++
                    }
                }
                while(tmpStage > achievements.value!![2].stage){
                    viewModel.achievementViewModel.transitionToNextStage(achievements.value!![2])
                    congratulationsDialog(achievements.value!![2])
                }
                progressBar.progress = progress
                progressText.text = progressBar.progress.toString()+"/"+progressBar.max.toString()
            }
            3->{
                var tmpStage = 0
                progressBar.max = checkpointsLevels[0]
                for(i in checkpointsLevels.indices) {
                    if (stateViewModel.getState().unlockedCheckpoints > checkpointsLevels[i]){
                        progressBar.max = checkpointsLevels[i+1]
                        tmpStage++
                    }
                }
                while(tmpStage > achievements.value!![3].stage){
                    viewModel.achievementViewModel.transitionToNextStage(achievements.value!![3])
                    congratulationsDialog(achievements.value!![3])

                }
                progressBar.progress = stateViewModel.getState().unlockedCheckpoints
                progressText.text = progressBar.progress.toString()+"/"+progressBar.max.toString()
            }
        }

    }

    private fun congratulationsDialog(achievement: Achievement){

            val alertDialog: AlertDialog? = activity?.let {
                val builder = AlertDialog.Builder(context)
                builder.apply {
                    setTitle("Congratulations")
                    setIcon(R.drawable.hoorray_icon)
                    setMessage("You've unlocked "+achievement.stage+" level of "+achievement.title+" achievement")
                    setPositiveButton("Got it, thanks!",
                            DialogInterface.OnClickListener { dialog, id ->
                                // User clicked OK button
                            })
                }
                builder.create()
                builder.show()
            }
    }

    private fun getCompletedWalksNumber():Int{
        var completedWalks = 0
        viewModel.walkViewModel.walks.observe(viewLifecycleOwner, Observer { walks->
            for(walk in walks){
                if(walk.state == WalkState.Completed) completedWalks++
            }
        })
        return completedWalks
    }

    private fun getDaysInRowNumber():Int{
        return 0
    }



    override fun getItemCount(): Int {
        return achievements.value?.size?:0
    }
}
