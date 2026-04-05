package com.example.mindwalk.data

import com.google.gson.annotations.SerializedName

data class RouteRequest(
    val place: String,
    @SerializedName("distance_km") val distanceKm: Double,
    @SerializedName("start_lat") val startLat: Double? = null,
    @SerializedName("start_lon") val startLon: Double? = null,
    val mode: String = "both",
    val seed: Int = 0,
    val topk: Int = 20,
    val n: Int = 5,
    val attempts: Int = 12,
    val err: Double = 0.05,
    @SerializedName("w_green") val wGreen: Double = 1.5,
    @SerializedName("w_tree") val wTree: Double = 1.0,
    @SerializedName("w_car") val wCar: Double = 1.2
)

data class GeoJsonLineString(
    val type: String,
    val coordinates: List<List<Double>> // [lon, lat]
)

data class RouteResponse(
    @SerializedName("length_km") val lengthKm: Double,
    val geojson: GeoJsonLineString
)

data class RoutePreviewData(
    val durationMin: Int,
    val mindfulnessMode: String,
    val routeShape: String,
    val distanceKm: Double,
    val points: List<Point>
)
