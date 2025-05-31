package com.example.bangumi.data.api

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * Created by gaoshiqi
 * on 2025/5/30 11:09
 * email: gaoshiqi@bilibili.com
 */
object BangumiClient {

    private const val BASE_URL = "https://api.bgm.tv/"
    private const val USER_AGENT = "BANGUMI/1.0 (gaoshiqi@bilibili.com)"

    val instance: BangumiService by lazy {
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
            .create(BangumiService::class.java)
    }


}