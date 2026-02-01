package com.example.allergynavigator

import com.example.allergynavigator.models.RouteRequest
import com.example.allergynavigator.models.RouteResponse
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiService {
    @POST("/get_route")
    suspend fun getRoute(@Body request: RouteRequest): RouteResponse

    @POST("/get_tour_route")
    suspend fun getTourRoute(@Body request: RouteRequest): RouteResponse
}