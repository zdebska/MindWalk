package com.example.mindwalk.service

import com.example.mindwalk.data.Point
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject

class OsrmService(
    private val client: OkHttpClient = OkHttpClient()
) {
    fun fetchWalkingRoute(path: List<Point>): List<Point> {
        val coords = path.joinToString(";") { "${it.lon},${it.lat}" }
        val url = "https://router.project-osrm.org/route/v1/walking/$coords?overview=full&geometries=geojson"

        val request = Request.Builder().url(url).build()
        val response = client.newCall(request).execute()
        val body = response.body?.string() ?: error("Empty response")

        val json = JSONObject(body)
        val routes = json.getJSONArray("routes")
        if (routes.length() == 0) error("No routes found")

        val geometry = routes.getJSONObject(0).getJSONObject("geometry")
        val coordsArray = geometry.getJSONArray("coordinates")

        val out = ArrayList<Point>(coordsArray.length())
        for (i in 0 until coordsArray.length()) {
            val pair = coordsArray.getJSONArray(i)
            val lon = pair.getDouble(0)
            val lat = pair.getDouble(1)
            out.add(Point(lat, lon))
        }
        return out
    }
}
