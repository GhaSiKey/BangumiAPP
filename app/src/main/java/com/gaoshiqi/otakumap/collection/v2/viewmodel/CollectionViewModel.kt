package com.gaoshiqi.otakumap.collection.v2.viewmodel

import android.app.Application
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
                viewModelScope.launch {
                    repository.updateCollectionStatus(intent.animeId, intent.newStatus)
                }
            }

            is CollectionIntent.UpdateProgress -> {
                viewModelScope.launch {
                    repository.updateWatchedEpisodes(intent.animeId, intent.episodes)
                }
            }

            is CollectionIntent.RemoveAnime -> {
                viewModelScope.launch {
                    repository.removeAnimeMarkById(intent.animeId)
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

    private fun sortList(list: List<AnimeEntity>, order: SortOrder): List<AnimeEntity> {
        return when (order) {
            SortOrder.BY_TIME -> list.sortedByDescending { it.bookmarkTime }
            SortOrder.BY_NAME -> list.sortedBy { it.displayName }
        }
    }
}
