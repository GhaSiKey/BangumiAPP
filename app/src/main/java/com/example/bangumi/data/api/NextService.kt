package com.example.bangumi.data.api

import com.example.bangumi.data.bean.CommentResponse
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Created by gaoshiqi
 * on 2025/7/16 18:35
 * email: gaoshiqi@bilibili.com
 */
interface NextService {

    @GET("p1/subjects/{subject_id}/comments")
    suspend fun getSubjectComments(
        @Path("subject_id") subject: Int,
        @Query("offset") offset: Int = 0,
        @Query("limit") limit: Int = 20,
    ): CommentResponse
}