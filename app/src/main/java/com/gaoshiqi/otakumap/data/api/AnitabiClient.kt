package com.gaoshiqi.otakumap.data.api

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
    private const val USER_AGENT = "BANGUMI/1.0 (gaoshiqi@bilibili.com)"

    val instance: AnitabiService by lazy {
        val client = OkHttpClient.Builder()
            .addInterceptor { chain ->
                val request = chain.request().newBuilder()
                    .addHeader("User-Agent", USER_AGENT)
                    .build()
                chain.proceed(request)
            }
            .build()

        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(AnitabiService::class.java)
    }
}