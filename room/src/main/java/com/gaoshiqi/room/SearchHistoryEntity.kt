package com.gaoshiqi.room

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "search_history")
data class SearchHistoryEntity(
    @PrimaryKey
    val keyword: String,
    val searchTime: Long = System.currentTimeMillis()
)
