package com.gaoshiqi.otakumap.collection.v2.viewmodel

import com.gaoshiqi.room.AnimeEntity
import com.gaoshiqi.room.CollectionStatus

/**
 * Tab 定义
 */
data class CollectionTab(
    val status: Int,
    val titleResId: Int
)

/**
 * 排序方式
 */
enum class SortOrder {
    BY_TIME,    // 按收藏时间
    BY_NAME     // 按名称
}

/**
 * UI 状态定义
 */
data class CollectionState(
    val currentTabIndex: Int = 0,
    val tabs: List<CollectionTab> = defaultTabs,
    val animeListByTab: Map<Int, List<AnimeEntity>> = emptyMap(),
    val sortOrder: SortOrder = SortOrder.BY_TIME,
    val isLoading: Boolean = false,
    val navigateToDetail: AnimeEntity? = null
) {
    companion object {
        val defaultTabs = listOf(
            CollectionTab(CollectionStatus.DOING, com.gaoshiqi.otakumap.R.string.collection_doing),
            CollectionTab(CollectionStatus.WISH, com.gaoshiqi.otakumap.R.string.collection_wish),
            CollectionTab(CollectionStatus.COLLECT, com.gaoshiqi.otakumap.R.string.collection_collect),
            CollectionTab(CollectionStatus.ON_HOLD, com.gaoshiqi.otakumap.R.string.collection_on_hold)
        )
    }

    val currentTab: CollectionTab
        get() = tabs[currentTabIndex]

    val currentAnimeList: List<AnimeEntity>
        get() = animeListByTab[currentTab.status] ?: emptyList()
}
