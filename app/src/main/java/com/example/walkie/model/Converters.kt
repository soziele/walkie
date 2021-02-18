package com.example.teacherhelper.model

import androidx.room.TypeConverter
import com.google.android.gms.common.util.JsonUtils
import com.google.android.gms.maps.model.LatLng
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.json.JSONStringer
import java.util.*

class Converters {
    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }

    @TypeConverter
    fun fromCheckpoints(value: String): Array<LatLng> {
        val sType = object : TypeToken<Array<LatLng>>() { }.type
        return Gson().fromJson<Array<LatLng>>(value, sType)
    }

    @TypeConverter
    fun toCheckpoints(value: Array<LatLng>): String {
        return Gson().toJson(value)
    }

    @TypeConverter
    fun fromVisited(value: String): BooleanArray {
        val sType = object : TypeToken<BooleanArray>() { }.type
        return Gson().fromJson<BooleanArray>(value, sType)
    }

    @TypeConverter
    fun toVisited(value: BooleanArray): String {
        return Gson().toJson(value)
    }
}