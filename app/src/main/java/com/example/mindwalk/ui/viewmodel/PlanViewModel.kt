package com.example.mindwalk.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mindwalk.data.Point
import com.example.mindwalk.service.OsrmService
import com.example.mindwalk.service.PythonRouteService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.osmdroid.util.GeoPoint

class PlanViewModel : ViewModel() {
    private val osrmService = OsrmService()
    private val pythonRouteService = PythonRouteService()

    var startLocation by mutableStateOf<GeoPoint?>(null)
    var startLocationName by mutableStateOf("Current Location")

    var previewRoutePoints by mutableStateOf<List<GeoPoint>>(emptyList())
        private set

    var isLoading by mutableStateOf(false)
        private set

    fun setStartLocation(point: GeoPoint, name: String) {
        startLocation = point
        startLocationName = name
    }

    fun generatePythonRoute(durationMin: Int, mode: String, shape: String) {
        viewModelScope.launch(Dispatchers.IO) {
            isLoading = true
            try {
                val distanceKm = (durationMin / 12.0).coerceAtLeast(1.0)
                val route = pythonRouteService.getRouteFromPython(distanceKm, mode)
                previewRoutePoints = route.map { GeoPoint(it.lat, it.lon) }
            } catch (e: Exception) {
                e.printStackTrace()
                generateABRoute()
            } finally {
                isLoading = false
            }
        }
    }

    fun generateABRoute() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val start = Point(49.1951, 16.6083)
                val end = Point(49.1951, 16.6083)

                val route = osrmService.fetchWalkingRoute(listOf(start, end))
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
