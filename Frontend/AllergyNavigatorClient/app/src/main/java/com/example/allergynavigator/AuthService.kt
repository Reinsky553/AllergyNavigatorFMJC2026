package com.example.allergynavigator

import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST

data class LoginRequest(val username: String, val pass: String)
data class RegisterRequest(val name: String, val username: String, val pass: String)
data class AllergyRequest(val allergies: List<String>)

interface AuthService {
    @POST("api/auth/login")
    suspend fun loginUser(@Body request: LoginRequest): Response<Unit>

    @POST("api/auth/register")
    suspend fun registerUser(@Body request: RegisterRequest): Response<Unit>

    @POST("api/user/allergies")
    suspend fun syncAllergies(
        @Header("Authorization") token: String,
        @Body request: AllergyRequest
    ): Response<Void>
}