package com.example.mindwalk.service

import com.example.mindwalk.data.Point
import com.example.mindwalk.data.PrepareRequest
import com.example.mindwalk.data.RouteRequest
import kotlinx.coroutines.delay
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * HTTP client for the MindWalk Python backend hosted on Azure.
 *
 * Provides suspend functions for the three main backend operations:
 * 1. **City preparation** — downloading and pre-processing the OSMnx street graph.
 * 2. **Status polling** — checking when preparation is complete.
 * 3. **Route generation** — requesting a mindful walking route.
 *
 * OkHttp timeouts are set high (connect: 180 s, read/write: 360 s) because the Azure
 * cold-start and OSMnx graph computation can take over a minute on first run.
 */
class PythonRouteService {

    private val BASE_URL = "https://katya-route-api-2026.azurewebsites.net/"

    /** Logs full request and response bodies to Logcat for network debugging. */
    private val logging = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(logging)
        .connectTimeout(180, TimeUnit.SECONDS)
        .readTimeout(360, TimeUnit.SECONDS)
        .writeTimeout(360, TimeUnit.SECONDS)
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val api = retrofit.create(RouteApi::class.java)

    // ── Public API ────────────────────────────────────────────────────────────

    /**
     * Sends a `GET /ping` request and returns the server's message string.
     *
     * Returns a descriptive error string instead of throwing if the call fails,
     * making it safe to call without a try-catch.
     *
     * @return `"pong"` on success, or an error description on failure.
     */
    suspend fun ping(): String {
        return try {
            val response = api.ping()
            if (response.isSuccessful) {
                response.body()?.get("message") ?: "Empty pong"
            } else {
                "Ping failed: ${response.code()}"
            }
        } catch (e: Exception) {
            "Ping error: ${e.message}"
        }
    }

    /**
     * Requests a walking route from the backend and returns it as an ordered list of [Point]s.
     *
     * The mode string is lowercased before sending so UI labels ("Green", "Quiet", "Sight")
     * map directly to the API vocabulary ("green", "quiet", "sight").
     *
     * GeoJSON coordinates are in [lon, lat] order (RFC 7946); this function swaps them to
     * [lat, lon] when constructing [Point] objects.
     *
     * @param place      City in Nominatim format (e.g. `"Brno, Czechia"`).
     * @param distanceKm Desired route length in kilometres.
     * @param mode       Mindfulness mode: `"Green"`, `"Quiet"`, or `"Sight"`.
     * @param seed       Deterministic seed; increment to get a different route variant.
     * @param startLat   Optional start latitude. If null the backend picks a random start node.
     * @param startLon   Optional start longitude.
     * @param endLat     Optional end latitude for point-to-point routes.
     * @param endLon     Optional end longitude.
     * @return Ordered list of [Point]s forming the route polyline.
     * @throws Exception If the HTTP response is not 2xx, with the status code and error body included.
     */
    suspend fun getRouteFromPython(
        place: String,
        distanceKm: Double,
        mode: String,
        seed: Int = 0,
        startLat: Double? = null,
        startLon: Double? = null,
        endLat: Double? = null,
        endLon: Double? = null
    ): List<Point> {
        val request = RouteRequest(
            place      = place,
            distanceKm = distanceKm,
            startLat   = startLat,
            startLon   = startLon,
            endLat     = endLat,
            endLon     = endLon,
            mode       = mode.lowercase(),
            seed       = seed
        )

        val response = api.getRoute(request)

        if (!response.isSuccessful) {
            throw Exception("HTTP ${response.code()}: ${response.errorBody()?.string()}")
        }

        val body = response.body() ?: throw Exception("Empty response body")

        // GeoJSON uses [lon, lat] order — swap to [lat, lon] for Point
        return body.geojson.coordinates.map { pair ->
            Point(lat = pair[1], lon = pair[0])
        }
    }

    /**
     * Sends `POST /prepare` to trigger asynchronous city graph computation on the backend.
     *
     * Returns immediately after the server accepts the job. Call [pollPrepareStatus] to
     * wait for completion.
     *
     * @param place City name in Nominatim format.
     * @throws Exception If the HTTP response is not 2xx.
     */
    suspend fun triggerPrepare(place: String) {
        val response = api.prepare(PrepareRequest(place))
        if (!response.isSuccessful) {
            throw Exception("Prepare failed: HTTP ${response.code()}")
        }
    }

    /**
     * Polls `GET /prepare/status` every 2 seconds until the city graph is ready or an error occurs.
     *
     * Invokes [onStatus] with the server's progress message on each poll so the UI can display
     * live feedback to the user during the onboarding loading phase.
     *
     * Times out after 5 minutes and throws if the backend has not reached `"ready"` by then.
     *
     * @param place    City name to check (must match the one passed to [triggerPrepare]).
     * @param onStatus Callback invoked with the server's human-readable status message.
     * @throws Exception If the status is `"error"`, or if the 5-minute timeout is exceeded.
     */
    suspend fun pollPrepareStatus(place: String, onStatus: (String) -> Unit) {
        val timeoutMs = 5 * 60 * 1000L
        val startTime = System.currentTimeMillis()

        while (System.currentTimeMillis() - startTime < timeoutMs) {
            val response = api.getPrepareStatus(place)
            if (response.isSuccessful) {
                val body = response.body() ?: break
                onStatus(body.message ?: "Preparing…")
                when (body.status) {
                    "ready" -> return
                    "error" -> throw Exception(body.message ?: "City preparation failed")
                }
            }
            delay(2_000)
        }
        throw Exception("City preparation timed out. Please try again.")
    }
}
