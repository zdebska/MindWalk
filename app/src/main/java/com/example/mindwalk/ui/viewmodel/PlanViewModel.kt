package com.example.mindwalk.ui.viewmodel

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Looper
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.mindwalk.data.CityPreferences
import com.example.mindwalk.data.Point
import com.example.mindwalk.data.SavedRoute
import com.example.mindwalk.service.OsrmService
import com.example.mindwalk.service.PythonRouteService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import org.osmdroid.util.GeoPoint
import kotlin.coroutines.resume
import kotlin.math.*

/**
 * ViewModel for walk planning and active route management.
 *
 * Owns the entire lifecycle of a route from planning to post-walk reflection:
 * - Stores user-selected start and end locations.
 * - Calls [PythonRouteService] to generate routes from the Azure backend.
 * - Exposes the generated route as [previewRoutePoints] for the map screens.
 * - Manages a seed counter that increments on each "New route" request to ensure
 *   different route variants are returned for the same parameters.
 * - Provides [planDistanceKm] computed from the Haversine distance of the polyline,
 *   used on the reflection screen for walk statistics.
 *
 * Extends [AndroidViewModel] to access [CityPreferences] and [LocationManager] via
 * the application context without leaking an Activity reference.
 *
 * @param app Application context used for city preferences and location access.
 */
class PlanViewModel(app: Application) : AndroidViewModel(app) {

    private val osrmService        = OsrmService()
    private val pythonRouteService = PythonRouteService()

    /** User-selected start coordinate, or null when "Current Location" is the default. */
    var startLocation by mutableStateOf<GeoPoint?>(null)

    /** Display label for the start location shown in [com.example.mindwalk.ui.screens.PlanYourWalkScreen]. */
    var startLocationName by mutableStateOf("Current Location")

    /** User-selected end coordinate for point-to-point (Line) routes. */
    var endLocation by mutableStateOf<GeoPoint?>(null)

    /** Display label for the end location. */
    var endLocationName by mutableStateOf("Select end point")

    /**
     * Ordered list of [GeoPoint]s representing the currently generated route polyline.
     * Observed by [com.example.mindwalk.ui.screens.RoutePreviewScreen] and
     * [com.example.mindwalk.ui.screens.WalkingScreen] via Compose state.
     */
    var previewRoutePoints by mutableStateOf<List<GeoPoint>>(emptyList())
        private set

    /** True while a route generation HTTP request is in flight. */
    var isLoading by mutableStateOf(false)
        private set

    /** True if the last route generation attempt failed. */
    var routeError by mutableStateOf(false)
        private set

    /** Human-readable error detail from the backend, shown in the error dialog. */
    var routeErrorMessage by mutableStateOf<String?>(null)
        private set

    private var lastDuration = 30
    private var lastMode     = "Green"
    private var lastShape    = "Loop"

    /**
     * Seed incremented on each call to [retryRoute] to obtain a different route variant
     * from the backend for the same parameters. Reset to 0 when the user leaves
     * [com.example.mindwalk.ui.screens.RoutePreviewScreen].
     */
    private var routeSeed = 0

    /** Duration (minutes) of the most recently generated route. */
    val planDurationMin: Int    get() = lastDuration

    /** Mindfulness mode label of the most recently generated route. */
    val planMode:        String get() = lastMode

    /** Shape label of the most recently generated route. */
    val planShape:       String get() = lastShape

    /**
     * Length of [previewRoutePoints] in kilometres, computed using the Haversine formula.
     *
     * Iterates consecutive point pairs and accumulates the great-circle distance.
     * Returns 0.0 if fewer than 2 points are available.
     */
    val planDistanceKm: Double get() {
        val pts = previewRoutePoints
        if (pts.size < 2) return 0.0
        var total = 0.0
        for (i in 0 until pts.size - 1) {
            val p1 = pts[i]; val p2 = pts[i + 1]
            val dLat = Math.toRadians(p2.latitude  - p1.latitude)
            val dLon = Math.toRadians(p2.longitude - p1.longitude)
            val lat1 = Math.toRadians(p1.latitude)
            val lat2 = Math.toRadians(p2.latitude)
            val h = sin(dLat / 2).pow(2) + cos(lat1) * cos(lat2) * sin(dLon / 2).pow(2)
            total += 2 * 6_371_000.0 * asin(sqrt(h))
        }
        return total / 1000.0
    }

    /**
     * Sets the user-chosen start location from [com.example.mindwalk.ui.screens.LocationPickerScreen].
     *
     * @param point The tapped map coordinate.
     * @param name  Human-readable label (e.g. "Pinned Location" or "Current Location").
     */
    fun setStartLocation(point: GeoPoint, name: String) {
        startLocation     = point
        startLocationName = name
    }

    /**
     * Sets the user-chosen end location for point-to-point (Line) routes.
     *
     * @param point The tapped map coordinate.
     * @param name  Human-readable label.
     */
    fun setEndLocation(point: GeoPoint, name: String) {
        endLocation     = point
        endLocationName = name
    }

    /**
     * Generates a walking route via [PythonRouteService] and stores the result in [previewRoutePoints].
     *
     * If [startLocation] is null (the user left the default "Current Location"), the device's
     * GPS position is resolved first via [getCurrentLocation] before sending the request.
     *
     * Sets [isLoading] to true for the duration of the call and [routeError] to true on failure,
     * storing the error detail in [routeErrorMessage].
     *
     * @param durationMin Desired walk duration in minutes; converted to distance at 1 km / 12 min.
     * @param mode        Mindfulness mode label from the UI ("Green", "Quiet", "Sight").
     */
    fun generatePythonRoute(durationMin: Int, mode: String) {
        lastDuration = durationMin
        lastMode     = mode
        lastShape    = "Loop"
        routeError   = false
        isLoading    = true
        val city = CityPreferences.getCity(getApplication()) ?: "Brno, Czechia"
        val seed = kotlin.random.Random.nextInt()

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val distanceKm = (durationMin / 12.0).coerceAtLeast(1.0)

                // If the user did not pin a start location, resolve the device GPS position.
                val resolvedStart: GeoPoint? = startLocation
                    ?: getCurrentLocation(getApplication())

                val route = pythonRouteService.getRouteFromPython(
                    place        = city,
                    distanceKm   = distanceKm,
                    mode         = mode,
                    seed         = seed,
                    nCandidates  = 5,
                    startLat     = resolvedStart?.latitude,
                    startLon     = resolvedStart?.longitude,
                    endLat       = endLocation?.latitude,
                    endLon       = endLocation?.longitude
                )
                previewRoutePoints = route.map { GeoPoint(it.lat, it.lon) }
            } catch (e: Exception) {
                e.printStackTrace()
                routeErrorMessage = e.message
                routeError = true
            } finally {
                isLoading = false
            }
        }
    }

    /**
     * Resolves the device's current GPS position for use as the route start coordinate.
     *
     * Strategy:
     * 1. Try [LocationManager.getLastKnownLocation] on GPS and Network providers (instant, may be stale).
     * 2. If no cached location is available, register a one-shot [LocationListener] with an 8-second timeout.
     *
     * Returns null if neither provider is enabled or the timeout expires, in which case the backend
     * may return a 500 error if it cannot handle a null start coordinate.
     *
     * @param context Application context.
     * @return The resolved [GeoPoint], or null if location could not be determined.
     */
    @SuppressLint("MissingPermission")
    private suspend fun getCurrentLocation(context: Context): GeoPoint? =
        withContext(Dispatchers.IO) {
            val lm = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

            // Fast path: use the most recent cached location if available
            val cached = listOf(LocationManager.GPS_PROVIDER, LocationManager.NETWORK_PROVIDER)
                .firstNotNullOfOrNull { provider ->
                    runCatching { lm.getLastKnownLocation(provider) }.getOrNull()
                }
            if (cached != null) return@withContext GeoPoint(cached.latitude, cached.longitude)

            // Slow path: request a fresh location update with a timeout
            val enabledProvider = listOf(LocationManager.NETWORK_PROVIDER, LocationManager.GPS_PROVIDER)
                .firstOrNull { lm.isProviderEnabled(it) } ?: return@withContext null

            val location: Location? = withTimeoutOrNull(8_000) {
                suspendCancellableCoroutine { cont ->
                    val listener = object : LocationListener {
                        override fun onLocationChanged(loc: Location) {
                            if (cont.isActive) cont.resume(loc)
                        }
                        override fun onProviderDisabled(provider: String) {
                            if (cont.isActive) cont.resume(null)
                        }
                    }
                    lm.requestSingleUpdate(enabledProvider, listener, Looper.getMainLooper())
                    cont.invokeOnCancellation { lm.removeUpdates(listener) }
                }
            }
            location?.let { GeoPoint(it.latitude, it.longitude) }
        }

    /**
     * Restores a previously saved route into [previewRoutePoints] for re-walking or preview.
     *
     * Called from [com.example.mindwalk.navigation.AppNav] when the user taps a route on
     * [com.example.mindwalk.ui.screens.SavedRoutesScreen].
     *
     * @param route The [SavedRoute] to load.
     */
    fun loadSavedRoute(route: SavedRoute) {
        lastDuration       = route.durationMin
        lastMode           = route.mode
        lastShape          = route.shape
        startLocationName  = route.startName
        previewRoutePoints = route.points.map { GeoPoint(it.lat, it.lon) }
    }

    /**
     * Increments [routeSeed] and re-generates the route with the same parameters.
     *
     * Incrementing the seed causes the backend to return a different route variant, enabling
     * the "New route" feature on [com.example.mindwalk.ui.screens.RoutePreviewScreen].
     */
    fun retryRoute() {
        routeSeed++
        generatePythonRoute(lastDuration, lastMode)
    }

    /**
     * Resets [routeSeed] to 0.
     *
     * Called via `DisposableEffect.onDispose` when the user leaves
     * [com.example.mindwalk.ui.screens.RoutePreviewScreen], so the next fresh generation
     * always starts from seed 0.
     */
    fun resetSeed() { routeSeed = 0 }

    /** Clears the error state after the user dismisses the error dialog. */
    fun clearError() { routeError = false; routeErrorMessage = null }

    /**
     * Fetches a simple A-to-B walking route via [OsrmService] as a fallback.
     *
     * Retained for debugging and offline scenarios. Uses a hardcoded Brno coordinate pair.
     */
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
