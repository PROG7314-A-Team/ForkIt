package com.example.forkit.data

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {
    private const val BASE_URL = "https://forkit-api.onrender.com/"//render time baby!
    // private const val BASE_URL = "http://10.0.2.2:3000/"//Android emulator localhost
    
    // Configure OkHttp with timeout settings
    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)      // Connection timeout
        .readTimeout(60, TimeUnit.SECONDS)         // Read timeout (for slow API responses)
        .writeTimeout(30, TimeUnit.SECONDS)        // Write timeout
        .build()
    
    val api: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)  // Add the configured OkHttp client
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}
