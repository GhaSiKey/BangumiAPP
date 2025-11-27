package com.gaoshiqi.otakumap.data.api

import com.gaoshiqi.otakumap.data.bean.BangumiDetail
import com.gaoshiqi.otakumap.data.bean.CalendarResponse
import com.gaoshiqi.otakumap.data.bean.BangumiCharacter
import com.gaoshiqi.otakumap.data.bean.SearchResponse
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

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

    @GET("/search/subject/{keywords}")
    suspend fun searchSubject(
        @Path("keywords") keywords: String,
        @Query("type") type: Int = 2,
        @Query("responseGroup") responseGroup: String = "large",
        @Query("start") start: Int = 1,
        @Query("max_results") limit: Int = 10,
    ): SearchResponse
}