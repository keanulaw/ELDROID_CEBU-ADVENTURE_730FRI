package com.example.eldroid_project

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiService {
    @POST("login")  // Make sure this matches your Laravel route
    suspend fun login(@Body loginRequest: LoginRequest): Response<LoginResponse>
}