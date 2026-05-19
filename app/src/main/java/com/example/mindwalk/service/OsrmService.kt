package com.example.mindwalk.service

import com.example.mindwalk.data.Point
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject

/**
 * Client for the public OSRM (Open Source Routing Machine) walking-route API.
 *
 * Used as a fallback route source before the Python backend was fully integrated.
 * Retained in the codebase for potential offline or alternative routing scenarios.
 *
 * Queries `https://router.project-osrm.org/route/v1/walking/` with a list of
 * coordinate waypoints and parses the GeoJSON LineString geometry from the response.
 *
 * @param client OkHttp client used for HTTP calls. A default instance is created if not provided.
 */
class OsrmService(
    private val client: OkHttpClient = OkHttpClient()
) {

    /**
     * Fetches a walking route between the given waypoints from the OSRM public API.
     *
     * This is a blocking call and must be invoked from a background thread or coroutine
     * on [kotlinx.coroutines.Dispatchers.IO].
     *
     * @param path Ordered list of [Point]s defining the waypoints (at minimum start and end).
     * @return Ordered list of [Point]s representing the full route geometry.
     * @throws IllegalStateException If the API returns an empty response body or no routes.
     */
    fun fetchWalkingRoute(path: List<Point>): List<Point> {
        // Build the coordinate string in OSRM's "lon,lat;lon,lat" format
        val coords = path.joinToString(";") { "${it.lon},${it.lat}" }
        val url = "https://router.project-osrm.org/route/v1/walking/$coords?overview=full&geometries=geojson"

        val request = Request.Builder().url(url).build()
        val response = client.newCall(request).execute()
        val body = response.body?.string() ?: error("Empty response")

        val json   = JSONObject(body)
        val routes = json.getJSONArray("routes")
        if (routes.length() == 0) error("No routes found")

        val geometry   = routes.getJSONObject(0).getJSONObject("geometry")
        val coordsArray = geometry.getJSONArray("coordinates")

        // GeoJSON coordinates are in [lon, lat] order — swap to [lat, lon] for Point
        val out = ArrayList<Point>(coordsArray.length())
        for (i in 0 until coordsArray.length()) {
            val pair = coordsArray.getJSONArray(i)
            out.add(Point(lat = pair.getDouble(1), lon = pair.getDouble(0)))
        }
        return out
    }
}
