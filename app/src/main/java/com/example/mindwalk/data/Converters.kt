package com.example.mindwalk.data

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class Converters {
    private val gson = Gson()

    @TypeConverter
    fun fromPoints(points: List<SerializablePoint>): String = gson.toJson(points)

    @TypeConverter
    fun toPoints(json: String): List<SerializablePoint> =
        gson.fromJson(json, object : TypeToken<List<SerializablePoint>>() {}.type) ?: emptyList()
}
