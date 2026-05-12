package com.example.mindwalk.data

import androidx.room.Entity
import androidx.room.PrimaryKey

data class SerializablePoint(val lat: Double, val lon: Double)

@Entity(tableName = "saved_routes")
data class SavedRoute(
    @PrimaryKey val id: String,
    val name: String,
    val savedAt: Long,
    val durationMin: Int,
    val distanceKm: Double,
    val mode: String,
    val shape: String,
    val mood: String,
    val startName: String,
    val points: List<SerializablePoint>
)
