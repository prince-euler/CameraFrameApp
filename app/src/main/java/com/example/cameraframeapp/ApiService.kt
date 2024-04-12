package com.example.cameraframeapp

import okhttp3.MultipartBody
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface ApiService {
    @Multipart
    @POST("upload_dummy")
    suspend fun sendImage(
        @Part files: MultipartBody.Part?
    )
}