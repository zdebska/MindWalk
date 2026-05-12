package com.example.mindwalk.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mindwalk.data.Point
import com.example.mindwalk.data.SavedRoute
import com.example.mindwalk.service.OsrmService
import com.example.mindwalk.service.PythonRouteService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.osmdroid.util.GeoPoint
import kotlin.math.*

class PlanViewModel : ViewModel() {
    private val osrmService = OsrmService()
    private val pythonRouteService = PythonRouteService()

    var startLocation by mutableStateOf<GeoPoint?>(null)
    var startLocationName by mutableStateOf("Current Location")

    var endLocation by mutableStateOf<GeoPoint?>(null)
    var endLocationName by mutableStateOf("Select end point")

    var previewRoutePoints by mutableStateOf<List<GeoPoint>>(emptyList())
        private set

    var isLoading by mutableStateOf(false)
        private set

    var routeError by mutableStateOf(false)
        private set

    // Remember last params so retryRoute() can re-use them
    private var lastDuration = 30
    private var lastMode     = "Calm"
    private var lastShape    = "Loop"

    // Exposed for saving a route after the walk
    val planDurationMin: Int    get() = lastDuration
    val planMode:        String get() = lastMode
    val planShape:       String get() = lastShape
    val planDistanceKm:  Double get() {
        val pts = previewRoutePoints
        if (pts.size < 2) return 0.0
        var total = 0.0
        for (i in 0 until pts.size - 1) {
            val p1 = pts[i]; val p2 = pts[i + 1]
            val dLat = Math.toRadians(p2.latitude  - p1.latitude)
            val dLon = Math.toRadians(p2.longitude - p1.longitude)
            val lat1 = Math.toRadians(p1.latitude); val lat2 = Math.toRadians(p2.latitude)
            val h = sin(dLat / 2).pow(2) + cos(lat1) * cos(lat2) * sin(dLon / 2).pow(2)
            total += 2 * 6_371_000.0 * asin(sqrt(h))
        }
        return total / 1000.0
    }

    fun setStartLocation(point: GeoPoint, name: String) {
        startLocation = point
        startLocationName = name
    }

    fun setEndLocation(point: GeoPoint, name: String) {
        endLocation = point
        endLocationName = name
    }

    fun generatePythonRoute(durationMin: Int, mode: String, shape: String) {
        lastDuration = durationMin
        lastMode     = mode
        lastShape    = shape
        routeError   = false
        isLoading    = true   // set on Main thread before coroutine starts
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val distanceKm = (durationMin / 12.0).coerceAtLeast(1.0)
                val route = pythonRouteService.getRouteFromPython(
                    distanceKm = distanceKm,
                    mode       = mode,
                    shape      = shape,
                    startLat   = startLocation?.latitude,
                    startLon   = startLocation?.longitude,
                    endLat     = endLocation?.latitude,
                    endLon     = endLocation?.longitude
                )
                previewRoutePoints = route.map { GeoPoint(it.lat, it.lon) }
            } catch (e: Exception) {
                e.printStackTrace()
                routeError = true
            } finally {
                isLoading = false
            }
        }
    }

    fun loadSavedRoute(route: SavedRoute) {
        lastDuration       = route.durationMin
        lastMode           = route.mode
        lastShape          = route.shape
        startLocationName  = route.startName
        previewRoutePoints = route.points.map { GeoPoint(it.lat, it.lon) }
    }

    fun retryRoute() = generatePythonRoute(lastDuration, lastMode, lastShape)

    fun clearError() { routeError = false }

    fun generateABRoute() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val start = Point(49.1951, 16.6083)
                val route = osrmService.fetchWalkingRoute(listOf(start, start))
                previewRoutePoints = route.map { GeoPoint(it.lat, it.lon) }
            } catch (e: Exception) {
                previewRoutePoints = listOf(
                    GeoPoint(49.1911, 16.6122),
                    GeoPoint(49.1951, 16.6122)
                )
            }
        }
    }
}
