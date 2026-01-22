package com.gaoshiqi.otakumap.collection.v2.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.gaoshiqi.room.AnimeEntity
import com.gaoshiqi.room.AnimeMarkRepository
import com.gaoshiqi.room.CollectionStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class CollectionViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = AnimeMarkRepository(application)

    private val _state = MutableStateFlow(CollectionState())
    val state: StateFlow<CollectionState> = _state.asStateFlow()

    init {
        // 为每个 Tab 订阅对应状态的数据
        observeAllTabs()
    }

    private fun observeAllTabs() {
        // 订阅「全部」列表
        viewModelScope.launch {
            repository.getAllAnime().collect { list ->
                _state.update { currentState ->
                    val newMap = currentState.animeListByTab.toMutableMap()
                    newMap[STATUS_ALL] = sortList(list, currentState.sortOrder)
                    currentState.copy(animeListByTab = newMap)
                }
            }
        }

        // 订阅各状态列表
        val statuses = listOf(
            CollectionStatus.DOING,
            CollectionStatus.WISH,
            CollectionStatus.COLLECT,
            CollectionStatus.ON_HOLD
        )

        statuses.forEach { status ->
            viewModelScope.launch {
                repository.getAnimeByStatus(status).collect { list ->
                    _state.update { currentState ->
                        val newMap = currentState.animeListByTab.toMutableMap()
                        newMap[status] = sortList(list, currentState.sortOrder)
                        currentState.copy(animeListByTab = newMap)
                    }
                }
            }
        }
    }

    fun processIntent(intent: CollectionIntent) {
        when (intent) {
            is CollectionIntent.SwitchTab -> {
                _state.update { it.copy(currentTabIndex = intent.tabIndex) }
            }

            is CollectionIntent.UpdateStatus -> {
                // 乐观更新策略
                viewModelScope.launch {
                    try {
                        // 立即在 Repository 层更新,由于使用 Flow,UI 会自动响应更新
                        repository.updateCollectionStatus(intent.animeId, intent.newStatus)
                    } catch (e: Exception) {
                        // 错误处理:显示错误消息
                        Log.e("CollectionViewModel", "更新收藏状态失败", e)
                        _state.update { it.copy(errorMessage = "更新状态失败: ${e.message}") }
                    }
                }
            }

            is CollectionIntent.UpdateProgress -> {
                viewModelScope.launch {
                    repository.updateWatchedEpisodes(intent.animeId, intent.episodes)
                }
            }

            is CollectionIntent.RemoveAnime -> {
                viewModelScope.launch {
                    try {
                        repository.removeAnimeMarkById(intent.animeId)
                    } catch (e: Exception) {
                        Log.e("CollectionViewModel", "删除收藏失败", e)
                        _state.update { it.copy(errorMessage = "删除失败: ${e.message}") }
                    }
                }
            }

            is CollectionIntent.ToggleSortOrder -> {
                val newOrder = if (intent.sortByName) SortOrder.BY_NAME else SortOrder.BY_TIME
                _state.update { currentState ->
                    // 重新排序所有列表
                    val newMap = currentState.animeListByTab.mapValues { (_, list) ->
                        sortList(list, newOrder)
                    }
                    currentState.copy(sortOrder = newOrder, animeListByTab = newMap)
                }
            }

            is CollectionIntent.NavigateToDetail -> {
                _state.update { it.copy(navigateToDetail = intent.anime) }
            }
        }
    }

    fun onNavigatedToDetail() {
        _state.update { it.copy(navigateToDetail = null) }
    }

    /**
     * 清除错误消息
     */
    fun clearError() {
        _state.update { it.copy(errorMessage = null) }
    }

    private fun sortList(list: List<AnimeEntity>, order: SortOrder): List<AnimeEntity> {
        return when (order) {
            SortOrder.BY_TIME -> list.sortedByDescending { it.bookmarkTime }
            SortOrder.BY_NAME -> list.sortedBy { it.displayName }
        }
    }
}
