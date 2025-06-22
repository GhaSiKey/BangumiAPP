package com.example.bangumi.data.api

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * Created by gaoshiqi
 * on 2025/6/22 20:35
 * email: gaoshiqi@bilibili.com
 */
object AnitabiClient {

    private const val BASE_URL = "https://api.anitabi.cn/"

    val instance: AnitabiService by lazy {
        val client = OkHttpClient.Builder().build()

        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(AnitabiService::class.java)
    }
}