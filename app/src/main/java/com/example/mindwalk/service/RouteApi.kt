package com.example.mindwalk.service

import com.example.mindwalk.data.PrepareRequest
import com.example.mindwalk.data.PrepareResponse
import com.example.mindwalk.data.PrepareStatusResponse
import com.example.mindwalk.data.RouteRequest
import com.example.mindwalk.data.RouteResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

/**
 * Retrofit interface defining the MindWalk Python backend REST API.
 *
 * The base URL is `https://katya-route-api-2026.azurewebsites.net/` and is configured
 * in [PythonRouteService]. All functions return [Response] wrappers so callers can
 * inspect HTTP status codes and error bodies on failure.
 */
interface RouteApi {

    /**
     * Health-check endpoint. Returns a simple JSON object `{"message": "pong"}`.
     *
     * Used to verify connectivity to the backend before heavier operations.
     *
     * @return [Response] wrapping a string-keyed map with a single `"message"` entry.
     */
    @GET("ping")
    suspend fun ping(): Response<Map<String, String>>

    /**
     * Generates a walking route for the given parameters.
     *
     * The backend selects a route on the pre-computed street graph using a beam-search
     * algorithm and returns the result as a GeoJSON LineString.
     *
     * @param request The route parameters (city, distance, mode, seed, start coordinates, etc.).
     * @return [Response] wrapping a [RouteResponse] containing the GeoJSON geometry.
     */
    @POST("route")
    suspend fun getRoute(@Body request: RouteRequest): Response<RouteResponse>

    /**
     * Triggers asynchronous pre-computation of the street graph for a given city.
     *
     * The backend downloads the OpenStreetMap graph via OSMnx and stores it for subsequent
     * route generation. This call returns immediately; use [getPrepareStatus] to poll progress.
     *
     * @param request Contains the city name in Nominatim-compatible format.
     * @return [Response] wrapping a [PrepareResponse] with the initial job status.
     */
    @POST("prepare")
    suspend fun prepare(@Body request: PrepareRequest): Response<PrepareResponse>

    /**
     * Polls the preparation status for a city.
     *
     * Should be called repeatedly (every ~2 seconds) after [prepare] until the returned
     * [PrepareStatusResponse.status] is `"ready"` or `"error"`.
     *
     * @param place The city name passed as a query parameter, matching the one sent to [prepare].
     * @return [Response] wrapping a [PrepareStatusResponse] with the current status and message.
     */
    @GET("prepare/status")
    suspend fun getPrepareStatus(@Query("place") place: String): Response<PrepareStatusResponse>
}
