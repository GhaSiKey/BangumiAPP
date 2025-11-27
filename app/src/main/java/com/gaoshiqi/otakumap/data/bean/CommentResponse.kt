package com.gaoshiqi.otakumap.data.bean

/**
 * Created by gaoshiqi
 * on 2025/7/17 17:08
 * email: gaoshiqi@bilibili.com
 */
data class CommentResponse(
    val data: List<CommentData>,
    val total: Int
)

data class CommentData(
    val id: Long,
    val user: CommentUser,
    val type: Int,
    val rate: Int,
    val comment: String,
    val updatedAt: Long
)

data class CommentUser(
    val id: Long,
    val username: String,
    val nickname: String,
    val avatar: CommentAvatar,
    val group: Int,
    val sign: String,
    val joinedAt: Long
)

data class CommentAvatar(
    val small: String,
    val medium: String,
    val large: String
)
