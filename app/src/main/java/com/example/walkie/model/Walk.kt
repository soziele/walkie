package com.example.walkie.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.android.gms.maps.model.LatLng
import java.sql.Date

@Entity(tableName="walk")
class Walk (
    @PrimaryKey(autoGenerate = true) val id: Int,
    @ColumnInfo(name= "date") val date: Date,
    @ColumnInfo(name="checkpoints") val checkpoints: Array<LatLng>,
    @ColumnInfo(name="visited_checkpoints") val visitedCheckpoints: Array<Boolean>,
    @ColumnInfo(name="length") val length: Double)


