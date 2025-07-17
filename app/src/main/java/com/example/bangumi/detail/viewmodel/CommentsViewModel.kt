package com.example.bangumi.detail.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bangumi.data.api.NextClient
import kotlinx.coroutines.launch

/**
 * Created by gaoshiqi
 * on 2025/7/17 17:30
 * email: gaoshiqi@bilibili.com
 */
class CommentsViewModel(private val mSubjectId: Int) : ViewModel() {


    private val _state = MutableLiveData<CommentsState>(CommentsState.Idle)
    val state = _state

    private var offset = 0
    private var limit = 20
    private var total = 0

    init {
        loadFirstPage()
    }

    fun handleIntent(intent: CommentsIntent) {
        when (intent) {
            CommentsIntent.Refresh -> refresh()
            CommentsIntent.LoadMore -> loadMore()
        }
    }

    private fun loadFirstPage() {
        offset = 0
        viewModelScope.launch {
            _state.value = CommentsState.Loading
            try {
                val result = NextClient.instance.getSubjectComments(mSubjectId)
                total = result.total
                _state.value = CommentsState.Success(result.data)
            } catch (e: Exception) {
                _state.value = CommentsState.Error(e.message ?: "加载失败")
            }
        }
    }

    private fun refresh() {
        offset = 0
        viewModelScope.launch {
            _state.value = CommentsState.Loading
            try {
                val result = NextClient.instance.getSubjectComments(mSubjectId)
                total = result.total
                _state.value = CommentsState.Success(result.data)
            } catch (e: Exception) {
                _state.value = CommentsState.Error(e.message ?: "刷新失败")
            }
        }
    }

    private fun loadMore() {
        if (offset >= total) {
            // 没有更多了
            return
        }
        val nextOffset = offset + limit
        viewModelScope.launch {
            _state.value = CommentsState.LoadingMore
            try {
                val result = NextClient.instance.getSubjectComments(mSubjectId, nextOffset, limit)
                offset = nextOffset
                total = result.total

                // 合并数据
                val currentData = (_state.value as? CommentsState.Success)?.data
                if (currentData != null) {
                    val mergedData = currentData + result.data
                    _state.value = CommentsState.Success(mergedData)
                } else {
                    _state.value = CommentsState.Success(result.data)
                }
            } catch (e: Exception) {
                _state.value = CommentsState.LoadMoreError(e.message ?: "加载更多失败")
            }
        }
    }

}