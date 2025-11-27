package com.gaoshiqi.room

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "bookmarked_anime")
data class AnimeEntity(
    @PrimaryKey
    val id: Int,
    val name: String,
    val nameCn: String,
    val imageUrl: String,
    val bookmarkTime: Long = System.currentTimeMillis()
)
