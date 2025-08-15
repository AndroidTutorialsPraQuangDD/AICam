package com.example.aicamtest.api

import okhttp3.MultipartBody
import retrofit2.Call
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface CalorieApi {
    @Multipart
    @POST("predict")
    fun predictCalories(
        @Part file: MultipartBody.Part
    ): Call<CalorieResponse>
}

data class CalorieResponse(
    val filename: String,
    val predicted_calories: Double,
    val predicted_normalized: Double
)