package com.gaoshiqi.otakumap.data.api

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * Created by gaoshiqi
 * on 2025/7/16 18:35
 * email: gaoshiqi@bilibili.com
 */
object NextClient {

    private const val BASE_URL = "https://next.bgm.tv/"
    private const val USER_AGENT = "BANGUMI/1.0 (gaoshiqi@bilibili.com)"
    private const val bearerToken = "YjkgiGyCUlS6XAXetMUrCulh6Q7yuW3rI8Y4SEOG"

    val instance: NextService by lazy {
        val client = OkHttpClient.Builder()
            .addInterceptor { chain ->
                val request = chain.request().newBuilder()
                    .header("Authorization", "Bearer $bearerToken")
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
            .create(NextService::class.java)
    }


}