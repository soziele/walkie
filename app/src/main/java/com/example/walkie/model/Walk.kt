package com.example.walkie.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.walkie.model.enums.WalkState
import com.google.android.gms.maps.model.LatLng
import java.time.LocalDateTime
import java.util.*

@Entity(tableName="walk")
class Walk(
        @PrimaryKey(autoGenerate = true) val id: Int,
        @ColumnInfo(name= "date") val date: Date,
        @ColumnInfo(name="checkpoints") val checkpoints: Array<LatLng>,
        @ColumnInfo(name="visited_checkpoints") val visitedCheckpoints: BooleanArray,
        @ColumnInfo(name="length") var length: Double,
        @ColumnInfo(name="state") var state: WalkState = WalkState.Active,
        @ColumnInfo(name="distance_traveled") var distanceTraveled: Double = 0.0
    )
