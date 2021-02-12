package com.example.walkie.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName="achievement")
class Achievement (
    @PrimaryKey(autoGenerate = true) val id: Int,
    @ColumnInfo(name="title") val title: String,
    @ColumnInfo(name="description") val description: String,
    @ColumnInfo(name="icon_path") var iconPath: Int,
    @ColumnInfo(name="received") var received: Boolean
)