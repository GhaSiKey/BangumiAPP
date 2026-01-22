package com.gaoshiqi.otakumap.collection.v2.viewmodel

import com.gaoshiqi.room.AnimeEntity

/**
 * 用户意图定义
 */
sealed class CollectionIntent {
    data class SwitchTab(val tabIndex: Int) : CollectionIntent()
    data class UpdateStatus(val animeId: Int, val newStatus: Int) : CollectionIntent()
    data class UpdateProgress(val animeId: Int, val episodes: Int) : CollectionIntent()
    data class RemoveAnime(val animeId: Int) : CollectionIntent()
    data class ToggleSortOrder(val sortByName: Boolean) : CollectionIntent()
    data class NavigateToDetail(val anime: AnimeEntity) : CollectionIntent()
}
