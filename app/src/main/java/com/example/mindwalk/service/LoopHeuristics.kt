package com.example.mindwalk.service

import com.example.mindwalk.data.Point
import kotlin.math.cos

object LoopHeuristics {

    fun waypointForLoop(start: Point, targetKm: Float): Point {
        val meters = (targetKm * 1000f) * 0.5f
        return offsetPoint(start, meters.toDouble())
    }

    private fun offsetPoint(start: Point, meters: Double): Point {
        val earth = 6378137.0
        val dLat = meters / earth
        val dLon = meters / (earth * cos(Math.toRadians(start.lat)))

        return Point(
            lat = start.lat + Math.toDegrees(dLat),
            lon = start.lon + Math.toDegrees(dLon)
        )
    }
}
