package com.example.mindwalk.ui.viewmodel

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.location.Geocoder
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
import com.example.mindwalk.service.NominatimService
import com.example.mindwalk.service.PythonRouteService
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import java.util.Locale
import kotlin.coroutines.resume

/**
 * Phases of the onboarding flow shown in [com.example.mindwalk.ui.screens.OnboardingScreen].
 *
 * - [CITY_SELECT] — The user enters or detects their city.
 * - [LOADING]     — The app is calling the backend to prepare the city graph.
 * - [ERROR]       — Preparation failed; the user can retry.
 */
enum class OnboardingPhase { CITY_SELECT, LOADING, ERROR }

/**
 * ViewModel managing the first-launch onboarding flow.
 *
 * Orchestrates three sub-tasks:
 * 1. **City input** — keyboard entry with Nominatim autocomplete (debounced 400 ms).
 * 2. **GPS detection** — resolves the device location and geocodes it to a `"City, Country"` string.
 * 3. **City preparation** — calls `POST /prepare` then polls `GET /prepare/status` until the
 *    backend's OSMnx street graph for the city is ready.
 *
 * The [phase] state drives [com.example.mindwalk.ui.screens.OnboardingScreen]'s
 * `AnimatedContent` to show the correct UI panel.
 *
 * @param app Application context used for geocoding, location, and city preference storage.
 */
class OnboardingViewModel(app: Application) : AndroidViewModel(app) {

    private val prepareService   = PythonRouteService()
    private val nominatimService = NominatimService()

    /** Current phase of the onboarding state machine. */
    var phase by mutableStateOf(OnboardingPhase.CITY_SELECT)
        private set

    /** Text currently shown in the city input field. Also used as the prepare request payload. */
    var cityInput by mutableStateOf("")

    /** City name detected from GPS, displayed as a confirmation chip below the GPS button. */
    var detectedCity by mutableStateOf<String?>(null)

    /** Live status message from the backend preparation polling loop, shown during [LOADING]. */
    var statusMessage by mutableStateOf("Connecting to server…")
        private set

    /** Error message from a failed prepare attempt, shown in the [ERROR] phase. */
    var errorMessage by mutableStateOf<String?>(null)
        private set

    /** True while a GPS location request is in flight. */
    var isDetectingLocation by mutableStateOf(false)
        private set

    /** True if GPS detection completed but failed to produce a city name. */
    var locationError by mutableStateOf(false)
        private set

    /** Autocomplete suggestions returned by Nominatim for the current [cityInput] text. */
    var suggestions by mutableStateOf<List<String>>(emptyList())
        private set

    /** True while a Nominatim search request is in flight. */
    var isSearching by mutableStateOf(false)
        private set

    /** Coroutine job for the debounced Nominatim search; cancelled on each keystroke. */
    private var searchJob: Job? = null

    /**
     * Called on every keystroke in the city input field.
     *
     * Clears [detectedCity] (input takes priority over GPS), cancels any pending search,
     * and schedules a new Nominatim search after a 400 ms debounce delay. Searches with
     * fewer than 2 characters are suppressed to avoid noisy results.
     *
     * @param value The updated text field value.
     */
    fun onCityInputChanged(value: String) {
        cityInput    = value
        detectedCity = null

        searchJob?.cancel()
        if (value.trim().length < 2) {
            suggestions = emptyList()
            return
        }
        searchJob = viewModelScope.launch {
            delay(400)
            isSearching = true
            suggestions = try {
                nominatimService.searchCity(value.trim())
            } catch (e: Exception) {
                emptyList()
            }
            isSearching = false
        }
    }

    /**
     * Fills [cityInput] with the chosen autocomplete suggestion and closes the dropdown.
     *
     * @param suggestion A `"City, Country"` string from [suggestions].
     */
    fun selectSuggestion(suggestion: String) {
        cityInput   = suggestion
        suggestions = emptyList()
        searchJob?.cancel()
    }

    /** Clears the autocomplete dropdown without changing [cityInput]. */
    fun dismissSuggestions() {
        suggestions = emptyList()
    }

    /**
     * Attempts to detect the user's city from the device's GPS or network location.
     *
     * Strategy:
     * 1. Try [LocationManager.getLastKnownLocation] on GPS and Network providers (instant).
     * 2. If no cached location exists, register a one-shot [LocationListener] with a 10-second timeout.
     * 3. Geocode the resolved coordinate to a city name using [Geocoder] with [Locale.ENGLISH]
     *    to ensure English country names (required by the OSMnx backend).
     *
     * On success, fills [cityInput] and [detectedCity]. On failure, sets [locationError].
     *
     * @param context Activity or application context providing [LocationManager] access.
     */
    @SuppressLint("MissingPermission")
    fun detectCityFromGps(context: Context) {
        isDetectingLocation = true
        locationError       = false
        viewModelScope.launch {
            val location = withContext(Dispatchers.IO) { requestLocation(context) }
            val city     = location?.let { geocodeToCity(context, it) }
            isDetectingLocation = false
            if (city != null) {
                cityInput    = city
                detectedCity = city
                suggestions  = emptyList()
            } else {
                locationError = true
            }
        }
    }

    /**
     * Resolves the device's current location using [LocationManager].
     *
     * Tries cached locations first for an instant result, then falls back to a live
     * one-shot update wrapped in [suspendCancellableCoroutine] with a 10-second timeout.
     *
     * @param context Context used to access the system [LocationManager].
     * @return The resolved [Location], or null if no fix could be obtained.
     */
    @SuppressLint("MissingPermission")
    private suspend fun requestLocation(context: Context): Location? {
        val lm = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

        // Fast path: last known location from any provider
        val cached = listOf(LocationManager.GPS_PROVIDER, LocationManager.NETWORK_PROVIDER)
            .firstNotNullOfOrNull { provider ->
                runCatching { lm.getLastKnownLocation(provider) }.getOrNull()
            }
        if (cached != null) return cached

        // Slow path: live one-shot update with timeout
        val enabledProvider = listOf(LocationManager.NETWORK_PROVIDER, LocationManager.GPS_PROVIDER)
            .firstOrNull { lm.isProviderEnabled(it) } ?: return null

        return withTimeoutOrNull(10_000) {
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
    }

    /**
     * Converts a [Location] to a `"City, Country"` string using the Android [Geocoder].
     *
     * [Locale.ENGLISH] is passed explicitly so the country name is always in English
     * regardless of the device language, matching the Nominatim format expected by the backend.
     *
     * @param context Context required by [Geocoder].
     * @param location The device location to reverse-geocode.
     * @return A non-blank `"City, Country"` string, or null if geocoding fails.
     */
    private fun geocodeToCity(context: Context, location: Location): String? {
        return try {
            @Suppress("DEPRECATION")
            val addresses = Geocoder(context, Locale.ENGLISH)
                .getFromLocation(location.latitude, location.longitude, 1)
            val addr = addresses?.firstOrNull() ?: return null
            // Prefer locality; fall back to sub-admin area (e.g. county) if locality is absent
            listOfNotNull(addr.locality ?: addr.subAdminArea, addr.countryName)
                .joinToString(", ").takeIf { it.isNotBlank() }
        } catch (e: Exception) { null }
    }

    /**
     * Initiates city graph preparation on the backend and advances the phase to [LOADING].
     *
     * Calls `POST /prepare` then polls `GET /prepare/status` every 2 seconds, forwarding
     * progress messages to [statusMessage]. On success, saves the city to [CityPreferences]
     * and invokes [onDone] to navigate to the home screen. On failure, transitions to [ERROR].
     *
     * @param context Context used by [CityPreferences.setCity].
     * @param onDone  Callback invoked on successful preparation (navigates away from onboarding).
     */
    fun startPrepare(context: Context, onDone: () -> Unit) {
        val city = cityInput.trim()
        if (city.isBlank()) return
        phase         = OnboardingPhase.LOADING
        statusMessage = "Connecting to server…"

        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    prepareService.triggerPrepare(city)
                    prepareService.pollPrepareStatus(city) { msg -> statusMessage = msg }
                }
                CityPreferences.setCity(context, city)
                onDone()
            } catch (e: Exception) {
                errorMessage = e.message ?: "Something went wrong. Please try again."
                phase        = OnboardingPhase.ERROR
            }
        }
    }

    /**
     * Resets the onboarding state back to [CITY_SELECT] after a failed preparation attempt.
     *
     * Clears [errorMessage] so the error panel is dismissed.
     */
    fun retry() {
        phase        = OnboardingPhase.CITY_SELECT
        errorMessage = null
    }
}
