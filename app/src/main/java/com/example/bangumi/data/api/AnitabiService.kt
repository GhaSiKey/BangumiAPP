package com.example.bangumi.data.api

import com.example.bangumi.data.bean.LitePoint
import retrofit2.http.GET
import retrofit2.http.Path

/**
 * Created by gaoshiqi
 * on 2025/6/22 20:31
 * email: gaoshiqi@bilibili.com
 */
interface AnitabiService {

    @GET("/bangumi/{subject_id}/points/detail")
    suspend fun getSubjectPoints(
        @Path("subject_id") subjectId: Int,
    ): List<LitePoint>
}