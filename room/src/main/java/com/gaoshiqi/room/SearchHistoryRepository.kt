package com.gaoshiqi.room

import android.content.Context
import kotlinx.coroutines.flow.Flow

class SearchHistoryRepository(context: Context) {

    companion object {
        const val MAX_HISTORY_COUNT = 10
    }

    private val searchHistoryDao = AppDatabase.getDatabase(context).searchHistoryDao()

    val allHistory: Flow<List<SearchHistoryEntity>> =
        searchHistoryDao.getRecentHistory(MAX_HISTORY_COUNT)

    suspend fun addHistory(keyword: String) {
        val trimmedKeyword = keyword.trim()
        if (trimmedKeyword.isNotEmpty()) {
            searchHistoryDao.insert(SearchHistoryEntity(trimmedKeyword))
            searchHistoryDao.trimToLimit(MAX_HISTORY_COUNT)
        }
    }

    suspend fun clearAllHistory() {
        searchHistoryDao.clearAll()
    }
}
