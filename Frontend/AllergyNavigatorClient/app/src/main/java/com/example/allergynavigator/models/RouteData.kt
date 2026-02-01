package com.example.allergynavigator.models

data class RouteRequest(
    val start_coords: List<Double>,
    val end_coords: List<Double>,
    val sensitivity: Double
)

data class RouteResponse(
    val status: String,
    val route: List<List<Double>>,
    val total_nodes: Int? = null,
    val landmarks: List<Landmark>? = null
)

data class Landmark(
    val name: String,
    val lat: Double,
    val lon: Double,
    val image_url: String
)