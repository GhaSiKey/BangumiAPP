package com.example.bangumi.data.api

import com.example.bangumi.data.model.CalendarResponse
import retrofit2.http.GET

/**
 * Created by gaoshiqi
 * on 2025/5/30 11:06
 * email: gaoshiqi@bilibili.com
 */
interface BangumiService {

    @GET("/calendar")
    suspend fun getCalendar(): List<CalendarResponse>
}