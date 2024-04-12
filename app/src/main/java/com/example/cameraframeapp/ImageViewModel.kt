package com.example.cameraframeapp

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream

//const val BASE_URL = "http://10.10.11.202:8000/"
const val BASE_URL = "http://100.80.23.38:8000/"

object ApiServiceFactory {
    fun create(): ApiService {
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        return retrofit.create(ApiService::class.java)
    }
}

class MainViewModel : ViewModel() {

    private val _status = MutableStateFlow("")
    val status: StateFlow<String> = _status

    fun saveImageToFile(context: Context, bitmap: Bitmap): File {
        val imagesFolder = File(context.getExternalFilesDir(null), "images")
        if (!imagesFolder.exists()) {
            imagesFolder.mkdirs()
        }
        val file = File(imagesFolder, "image${System.currentTimeMillis()}.png")
        Log.d("data-response","Image saved to destination file:- ${file.name + file.path + file.exists()}")
        val outputStream: OutputStream = FileOutputStream(file)
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
        outputStream.flush()
        outputStream.close()
        return file
    }

    suspend fun sendImageToServer(file: File) {
        val requestBody = file.asRequestBody("image/*".toMediaTypeOrNull())
        val body = MultipartBody.Part.createFormData("image", file.name, requestBody)
        try {
            ApiServiceFactory.create().sendImage(body)
            _status.emit("Image sent successfully")
        } catch (e: Exception) {
            _status.emit(e.printStackTrace().toString())
        }
    }
}
