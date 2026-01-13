package com.example.mindwalk.data

data class RoutePreviewData(
    val durationMin: Int,
    val mindfulnessMode: String,
    val routeShape: String,
    val distanceKm: Double,
    val points: List<Point> // route polyline
)
