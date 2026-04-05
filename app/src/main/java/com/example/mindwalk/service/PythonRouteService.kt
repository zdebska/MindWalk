package com.example.mindwalk.service

import com.example.mindwalk.data.Point
import com.example.mindwalk.data.RouteRequest
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class PythonRouteService {
    private val BASE_URL = "http://10.0.2.2:8000/"

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val api = retrofit.create(RouteApi::class.java)

    suspend fun getRouteFromPython(distanceKm: Double, mode: String): List<Point> {
        val request = RouteRequest(
            place = "Brno, Czechia",
            distanceKm = distanceKm,
            mode = mode.lowercase(),
            seed = (0..1000).random()
        )

        val response = api.getRoute(request)
        
        // GeoJSON coordinates are [longitude, latitude]
        return response.geojson.coordinates.map { pair ->
            Point(lat = pair[1], lon = pair[0])
        }
    }
}
