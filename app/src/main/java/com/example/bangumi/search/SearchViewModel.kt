package com.example.bangumi.search

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bangumi.data.api.BangumiClient
import com.example.bangumi.data.bean.SubjectSmall
import kotlinx.coroutines.launch
import java.util.ArrayList

/**
 * Created by gaoshiqi
 * on 2025/7/31 17:40
 * email: gaoshiqi@bilibili.com
 */
class SearchViewModel: ViewModel() {
    private val _state = MutableLiveData<SearchState>(SearchState.Idle)
    val state = _state

    private var query: String? = null
    private var start = 1
    private var limit = 10
    private var total = 0

    private var mList: List<SubjectSmall> = ArrayList()

    init {
        _state.value = SearchState.Idle
        query = null
    }

    fun handleIntent(intent: SearchIntent) {
        when (intent) {
            is SearchIntent.Search -> search(intent.query)
            is SearchIntent.LoadMore -> loadMore()
            is SearchIntent.Clear -> {}
        }
    }

    private fun search(q: String?) {
        start = 1
        query = q
        query.isNullOrEmpty() ?: return
        viewModelScope.launch {
            _state.value = SearchState.Loading
            try {
                val result = BangumiClient.instance.searchSubject(
                    keywords = query!!,
                    start = start,
                    limit = limit
                )
                result.results?.let { total = it }
                result.list?.let { mList = it }
                _state.value = SearchState.Success(mList)
            } catch (e: Exception) {
                _state.value = SearchState.Error(e.message ?: "搜索失败")
            }
        }
    }

    private fun loadMore() {
        if (start >= total || query.isNullOrEmpty()) {
            return
        }
        val next = start + limit
        viewModelScope.launch {
            _state.value = SearchState.LoadMore
            try {
                val result = BangumiClient.instance.searchSubject(
                    keywords = query!!,
                    start = next,
                    limit = limit
                )
                start = next
                result.results?.let { total = it }
                result.list?.let { mList = mList + it }

                _state.value = SearchState.Success(mList)
            } catch (e: Exception) {
                _state.value = SearchState.LoadMoreError(e.message ?: "加载失败")
            }
        }
    }
}

sealed class SearchIntent {
    data class Search(val query: String?): SearchIntent()
    object LoadMore: SearchIntent()
    object Clear: SearchIntent()
}

sealed class SearchState {
    object Idle: SearchState()
    object Loading: SearchState()
    data class Success(val data: List<SubjectSmall>): SearchState()
    data class Error(val message: String): SearchState()
    object LoadMore: SearchState()
    data class LoadMoreError(val message: String): SearchState()
}