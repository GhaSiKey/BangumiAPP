package com.gaoshiqi.otakumap.trending

import com.gaoshiqi.otakumap.data.bean.TrendingSubjectItem

sealed class TrendingState {
    object Idle : TrendingState()
    object Loading : TrendingState()
    data class Success(val data: List<TrendingSubjectItem>) : TrendingState()
    data class Error(val message: String) : TrendingState()
    object LoadMore: TrendingState()
    data class LoadMoreError(val message: String): TrendingState()
}