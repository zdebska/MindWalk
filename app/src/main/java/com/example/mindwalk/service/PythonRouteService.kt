package com.example.mindwalk.service

import com.example.mindwalk.data.Point
import com.example.mindwalk.data.RouteRequest
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class PythonRouteService {
    private val BASE_URL = "http://10.0.2.2:8000/"

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

    suspend fun getRouteFromPython(
        distanceKm: Double,
        mode: String,
        startLat: Double? = null,
        startLon: Double? = null
    ): List<Point> {
        val request = RouteRequest(
            place = "Brno, Czechia",
            distanceKm = distanceKm,
            startLat = startLat,
            startLon = startLon,
            mode = mode.lowercase(),
            seed = (0..1000).random()
        )

        val response = api.getRoute(request)

        if (!response.isSuccessful) {
            throw Exception("HTTP ${response.code()}: ${response.errorBody()?.string()}")
        }

        val body = response.body() ?: throw Exception("Empty response body")

        return body.geojson.coordinates.map { pair ->
            Point(lat = pair[1], lon = pair[0])
        }
    }
}
