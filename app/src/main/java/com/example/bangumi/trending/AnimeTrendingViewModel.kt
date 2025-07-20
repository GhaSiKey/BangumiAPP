package com.example.bangumi.trending

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bangumi.data.api.NextClient
import com.example.bangumi.data.bean.TrendingSubjectItem
import kotlinx.coroutines.launch

class AnimeTrendingViewModel: ViewModel() {

    private val _state = MutableLiveData<TrendingState>(TrendingState.Idle)
    val state = _state

    private var offset = 0
    private var limit = 10
    private var total = 0

    private var mTrending: List<TrendingSubjectItem> = ArrayList()

    init {
        loadFirstPage()
    }

    fun handleIntent(intent: TrendingIntent) {
        when (intent) {
            TrendingIntent.Refresh -> refresh()
            TrendingIntent.LoadMore -> loadMore()
        }
    }

    private fun loadFirstPage() {
        offset = 0
        viewModelScope.launch {
            _state.value = TrendingState.Loading
            try {
                val result = NextClient.instance.getTrendingSubjects(2, offset, limit)
                total = result.total
                mTrending = result.data
                _state.value = TrendingState.Success(mTrending)
            } catch (e: Exception) {
                _state.value = TrendingState.Error(e.message?: "加载失败")
            }
        }
    }

    private fun refresh() {
        offset = 0
        viewModelScope.launch {
            _state.value = TrendingState.Loading
            try {
                val result = NextClient.instance.getTrendingSubjects(2, offset, limit)
                total = result.total
                mTrending = result.data
                _state.value = TrendingState.Success(mTrending)
            } catch (e: Exception) {
                _state.value = TrendingState.Error(e.message?: "刷新失败")
            }
        }
    }

    private fun loadMore() {
        if (offset >= total) {
            return
        }
        val nextOffset = offset + limit
        viewModelScope.launch {
            _state.value = TrendingState.LoadMore
            try {
                val result = NextClient.instance.getTrendingSubjects(2, nextOffset, limit)
                offset = nextOffset
                total = result.total

                mTrending = mTrending + result.data
                _state.value = TrendingState.Success(mTrending)
            } catch (e: Exception) {
                _state.value = TrendingState.LoadMoreError(e.message?: "加载失败")
            }
        }
    }
}