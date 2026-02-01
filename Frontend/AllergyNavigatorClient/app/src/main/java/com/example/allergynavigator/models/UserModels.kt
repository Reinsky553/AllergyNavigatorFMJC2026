package com.example.allergynavigator.models

import com.google.gson.annotations.SerializedName

// Отправка в питончик
data class UserRequest(
    @SerializedName("username") val username: String,
    @SerializedName("password") val password: String
)

// Получение из питончика
data class AuthResponse(
    @SerializedName("status") val status: String,
    @SerializedName("message") val message: String,
    @SerializedName("user_id") val userId: Int? = null
)