package com.example.walkie.model

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.teacherhelper.model.Converters

@Database(entities = [Walk::class, Achievement::class], version = 1, exportSchema = false)
@TypeConverters(Converters::class)
abstract class WalkieDatabase : RoomDatabase() {

    abstract fun walkDao(): WalkDao
    abstract fun achievementDao(): AchievementDao

    companion object{
        @Volatile
        private var INSTANCE: WalkieDatabase ?= null

        fun getDatabase(context: Context): WalkieDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    WalkieDatabase::class.java,
                    "walkie_database"
                ).allowMainThreadQueries().build()
                INSTANCE = instance
                instance
            }
        }
    }
}