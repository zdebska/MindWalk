package com.example.mindwalk.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mindwalk.data.Point
import com.example.mindwalk.service.OsrmService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.osmdroid.util.GeoPoint
import kotlin.math.cos
import kotlin.math.sin

class PlanViewModel : ViewModel() {
    private val osrmService = OsrmService()

    var previewRoutePoints by mutableStateOf<List<GeoPoint>>(emptyList())
        private set

    fun generateABRoute() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                // Example: Route from Brno main station to Freedom Square
                val start = Point(49.1911, 16.6122) 
                val end = Point(49.1951, 16.6083)
                
                val route = osrmService.fetchWalkingRoute(listOf(start, end))
                previewRoutePoints = route.map { GeoPoint(it.lat, it.lon) }
            } catch (e: Exception) {
                // Fallback to a straight line if network fails
                previewRoutePoints = listOf(
                    GeoPoint(49.1911, 16.6122),
                    GeoPoint(49.1951, 16.6083)
                )
            }
        }
    }

    fun generateFakePreviewRoute() {
        previewRoutePoints = fakeLoopRoute(
            center = GeoPoint(49.1951, 16.6068), // pick any center
            radiusMeters = 180.0,
            points = 42
        )
    }

    private fun fakeLoopRoute(center: GeoPoint, radiusMeters: Double, points: Int): List<GeoPoint> {
        val metersPerDegLat = 111_320.0
        val metersPerDegLon = 111_320.0 * cos(Math.toRadians(center.latitude))

        val dLat = radiusMeters / metersPerDegLat
        val dLon = radiusMeters / metersPerDegLon

        val out = ArrayList<GeoPoint>(points + 1)
        for (i in 0 until points) {
            val a = 2.0 * Math.PI * i / points
            val lat = center.latitude + dLat * sin(a)
            val lon = center.longitude + dLon * cos(a)
            out.add(GeoPoint(lat, lon))
        }
        out.add(out.first())
        return out
    }
}
