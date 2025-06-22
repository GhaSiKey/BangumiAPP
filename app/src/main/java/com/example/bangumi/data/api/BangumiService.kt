package com.example.bangumi.data.api

import com.example.bangumi.data.model.BangumiDetail
import com.example.bangumi.data.model.CalendarResponse
import com.example.bangumi.data.model.BangumiCharacter
import retrofit2.http.GET
import retrofit2.http.Path

/**
 * Created by gaoshiqi
 * on 2025/5/30 11:06
 * email: gaoshiqi@bilibili.com
 */
interface BangumiService {

    @GET("/calendar")
    suspend fun getCalendar(): List<CalendarResponse>

    @GET("v0/subjects/{subject_id}")
    suspend fun getSubjectDetail(
        @Path("subject_id") subjectId: Int
    ): BangumiDetail

    @GET("/v0/subjects/{subject_id}/characters")
    suspend fun getSubjectCharacters(
        @Path("subject_id") subjectId: Int,
    ): List<BangumiCharacter>
}