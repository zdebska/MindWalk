package com.example.mindwalk.service

import com.example.mindwalk.data.Point
import kotlin.math.cos

/**
 * Geometric helper for generating a loop-route waypoint from a start coordinate.
 *
 * Used as a fallback when the Python backend is unavailable: a rough midpoint is computed
 * at half the target route distance north-east of the start, which OSRM then uses to
 * construct a there-and-back loop approximation.
 *
 * All calculations use a spherical Earth model (radius 6 378 137 m).
 */
object LoopHeuristics {

    /**
     * Computes a waypoint located roughly half the desired loop distance north-east of [start].
     *
     * The returned point is intended to be used as the mid-stop in an A→B→A OSRM request so
     * that the round trip approximates [targetKm] in total length.
     *
     * @param start    The walk start coordinate.
     * @param targetKm The desired total loop distance in kilometres.
     * @return A [Point] displaced ~[targetKm]/2 km north-east of [start].
     */
    fun waypointForLoop(start: Point, targetKm: Float): Point {
        val meters = (targetKm * 1000f) * 0.5f
        return offsetPoint(start, meters.toDouble())
    }

    /**
     * Displaces [start] by [meters] in both the latitude and longitude directions using a
     * flat-Earth approximation corrected for the latitude-dependent longitude scale.
     *
     * @param start  Origin coordinate.
     * @param meters Displacement distance in metres applied equally to lat and lon offsets.
     * @return The displaced [Point].
     */
    private fun offsetPoint(start: Point, meters: Double): Point {
        val earth = 6_378_137.0
        val dLat  = meters / earth
        val dLon  = meters / (earth * cos(Math.toRadians(start.lat)))
        return Point(
            lat = start.lat + Math.toDegrees(dLat),
            lon = start.lon + Math.toDegrees(dLon)
        )
    }
}
