package com.example.mindwalk.service

import com.example.mindwalk.data.RouteRequest
import com.example.mindwalk.data.RouteResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface RouteApi {
    @GET("ping")
    suspend fun ping(): Response<Map<String, String>>

    @POST("route")
    suspend fun getRoute(@Body request: RouteRequest): Response<RouteResponse>
}
