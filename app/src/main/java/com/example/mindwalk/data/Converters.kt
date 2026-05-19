package com.example.mindwalk.data

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

/**
 * Room [androidx.room.TypeConverters] that serialise [List]<[SerializablePoint]> to and from
 * a JSON string for storage in a single TEXT column.
 *
 * Room cannot persist complex types (lists, custom objects) directly. This converter is
 * registered on [MindWalkDatabase] via `@TypeConverters(Converters::class)` and is applied
 * automatically to the [SavedRoute.points] field.
 */
class Converters {

    private val gson = Gson()

    /**
     * Converts a list of [SerializablePoint] objects to a JSON string for database storage.
     *
     * @param points The coordinate list to serialise.
     * @return A JSON array string, e.g. `[{"lat":49.19,"lon":16.60},…]`.
     */
    @TypeConverter
    fun fromPoints(points: List<SerializablePoint>): String = gson.toJson(points)

    /**
     * Restores a list of [SerializablePoint] objects from a JSON string retrieved from the database.
     *
     * Returns an empty list if [json] is malformed or null, so the app never crashes on corrupt data.
     *
     * @param json The JSON array string previously produced by [fromPoints].
     * @return The deserialised list of [SerializablePoint], or an empty list on failure.
     */
    @TypeConverter
    fun toPoints(json: String): List<SerializablePoint> =
        gson.fromJson(json, object : TypeToken<List<SerializablePoint>>() {}.type) ?: emptyList()
}
