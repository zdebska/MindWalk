package com.example.mindwalk.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * A coordinate pair that can be serialised to JSON for Room storage.
 *
 * Room cannot store complex types natively. [SerializablePoint] is used as the element
 * type of [SavedRoute.points], which is converted to and from a JSON string column by
 * [Converters]. It mirrors [Point] but exists separately so that the route-persistence
 * concern stays isolated from the networking concern.
 *
 * @property lat Latitude in decimal degrees (WGS 84).
 * @property lon Longitude in decimal degrees (WGS 84).
 */
data class SerializablePoint(val lat: Double, val lon: Double)

/**
 * Room entity representing a user-saved walking route.
 *
 * A [SavedRoute] is created in [com.example.mindwalk.ui.screens.PostWalkReflectionScreen]
 * only when the user explicitly toggles the "Save this route" switch after completing a walk.
 * It is stored in the `saved_routes` table and displayed on
 * [com.example.mindwalk.ui.screens.SavedRoutesScreen].
 *
 * The [points] list is persisted as a Gson-serialised JSON string via [Converters].
 *
 * @property id           Unique identifier (random UUID).
 * @property name         User-provided display name for the route.
 * @property savedAt      Unix timestamp (ms) of when the route was saved.
 * @property durationMin  Planned walk duration in minutes.
 * @property distanceKm   Actual route length in kilometres, computed from the coordinate list.
 * @property mode         Mindfulness mode used when generating the route ("Green", "Quiet", "Sight").
 * @property shape        Route shape ("Loop" or "Line").
 * @property mood         Post-walk mood selected by the user ("Great", "Good", "Okay", "Not great").
 * @property startName    Human-readable label for the start location (e.g. "Current Location").
 * @property points       Ordered list of geographic coordinates that form the route polyline.
 */
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
