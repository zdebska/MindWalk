package com.example.mindwalk.service

import com.example.mindwalk.data.RouteRequest
import com.example.mindwalk.data.RouteResponse
import retrofit2.http.Body
import retrofit2.http.POST

interface RouteApi {
    @POST("route")
    suspend fun getRoute(@Body req: RouteRequest): RouteResponse
}
