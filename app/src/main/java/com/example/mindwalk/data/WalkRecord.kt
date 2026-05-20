package com.example.mindwalk.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room entity representing a single completed walk session.
 *
 * A [WalkRecord] is written to the `walk_records` table every time the user taps
 * "Complete walk" on [com.example.mindwalk.ui.screens.PostWalkReflectionScreen],
 * regardless of whether they choose to save the route. This ensures the Journey screen
 * statistics and leaf collection reflect all completed walks, not only the saved ones.
 *
 * This table was added in database schema version 2 via [MindWalkDatabase].
 *
 * @property id           Unique identifier (random UUID).
 * @property completedAt  Unix timestamp (ms) when the walk was completed.
 * @property durationMin  Planned walk duration in minutes.
 * @property distanceKm   Route length in kilometres computed from the coordinate polyline.
 * @property mode         Mindfulness mode used ("Green", "Quiet", "Sight").
 * @property shape        Route shape ("Loop" or "Line").
 * @property mood         Post-walk mood selected by the user ("Great", "Good", "Okay", "Not great").
 */
@Entity(tableName = "walk_records")
data class WalkRecord(
    @PrimaryKey val id: String,
    val completedAt: Long,
    val durationMin: Int,
    val distanceKm: Double,
    val mode: String,
    val shape: String,
    val mood: String
)
