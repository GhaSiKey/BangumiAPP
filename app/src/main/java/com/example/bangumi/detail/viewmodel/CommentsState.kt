package com.example.bangumi.detail.viewmodel

import com.example.bangumi.data.bean.CommentData

/**
 * Created by gaoshiqi
 * on 2025/7/17 17:22
 * email: gaoshiqi@bilibili.com
 */
sealed class CommentsState {
    object Idle : CommentsState()
    object Loading : CommentsState()
    data class Success(val data: List<CommentData>) : CommentsState()
    data class Error(val message: String) : CommentsState()
    object LoadingMore : CommentsState() // 加载更多
    data class LoadMoreError(val message: String) : CommentsState() // 加载更多错误
}