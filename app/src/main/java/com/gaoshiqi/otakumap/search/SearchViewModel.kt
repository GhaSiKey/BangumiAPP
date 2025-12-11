package com.gaoshiqi.otakumap.search

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.gaoshiqi.otakumap.data.api.BangumiClient
import com.gaoshiqi.otakumap.data.bean.SearchFilter
import com.gaoshiqi.otakumap.data.bean.SearchRequest
import com.gaoshiqi.otakumap.data.bean.SearchSubject
import com.gaoshiqi.room.SearchHistoryEntity
import com.gaoshiqi.room.SearchHistoryRepository
import kotlinx.coroutines.launch
import java.util.ArrayList

/**
 * Created by gaoshiqi
 * on 2025/7/31 17:40
 * email: gaoshiqi@bilibili.com
 */
class SearchViewModel(application: Application): AndroidViewModel(application) {
    private val _state = MutableLiveData<SearchState>(SearchState.Idle)
    val state = _state

    private val searchHistoryRepository = SearchHistoryRepository(application)
    val searchHistory: LiveData<List<SearchHistoryEntity>> =
        searchHistoryRepository.allHistory.asLiveData()

    private var query: String? = null
    private var offset = 0
    private var limit = 10
    private var total = 0

    private var mList: List<SearchSubject> = emptyList()

    init {
        _state.value = SearchState.Idle
        query = null
    }

    fun handleIntent(intent: SearchIntent) {
        when (intent) {
            is SearchIntent.Search -> search(intent.query)
            is SearchIntent.LoadMore -> loadMore()
            is SearchIntent.ClearAllHistory -> clearAllHistory()
            is SearchIntent.DeleteHistory -> deleteHistory(intent.keyword)
        }
    }

    private fun clearAllHistory() {
        viewModelScope.launch {
            searchHistoryRepository.clearAllHistory()
        }
    }

    private fun deleteHistory(keyword: String) {
        viewModelScope.launch {
            searchHistoryRepository.deleteHistory(keyword)
        }
    }

    private fun saveHistory(keyword: String) {
        viewModelScope.launch {
            searchHistoryRepository.addHistory(keyword)
        }
    }

    private fun search(q: String?) {
        offset = 0
        query = q
        mList = emptyList()
        if (query.isNullOrEmpty()) return
        viewModelScope.launch {
            _state.value = SearchState.Loading
            try {
                val result = BangumiClient.instance.searchSubjectsV2(
                    limit = limit,
                    offset = offset,
                    request = SearchRequest(
                        keyword = query!!,
                        filter = SearchFilter(type = listOf(2))
                    )
                )
                total = result.total ?: 0
                mList = result.data ?: emptyList()
                _state.value = SearchState.Success(mList)
                saveHistory(query!!)
            } catch (e: Exception) {
                _state.value = SearchState.Error(e.message ?: "搜索失败")
            }
        }
    }

    private fun loadMore() {
        // 已加载的数量 >= 总数，没有更多数据
        if (offset + limit >= total || query.isNullOrEmpty()) {
            return
        }
        val nextOffset = offset + limit
        viewModelScope.launch {
            _state.value = SearchState.LoadMore
            try {
                val result = BangumiClient.instance.searchSubjectsV2(
                    limit = limit,
                    offset = nextOffset,
                    request = SearchRequest(
                        keyword = query!!,
                        filter = SearchFilter(type = listOf(2))
                    )
                )
                offset = nextOffset
                total = result.total ?: total
                result.data?.let { mList = mList + it }

                _state.value = SearchState.Success(mList)
            } catch (e: Exception) {
                _state.value = SearchState.LoadMoreError(e.message ?: "加载失败")
            }
        }
    }
}

sealed class SearchIntent {
    data class Search(val query: String?): SearchIntent()
    data object LoadMore: SearchIntent()
    data object ClearAllHistory: SearchIntent()
    data class DeleteHistory(val keyword: String): SearchIntent()
}

sealed class SearchState {
    data object Idle: SearchState()
    data object Loading: SearchState()
    data class Success(val data: List<SearchSubject>): SearchState()
    data class Error(val message: String): SearchState()
    data object LoadMore: SearchState()
    data class LoadMoreError(val message: String): SearchState()
}