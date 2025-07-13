package com.example.room

import android.content.Context
import kotlinx.coroutines.flow.Flow

class AnimeMarkRepository(context: Context) {
    private val animeDao = AppDatabase.getDatabase(context).animeDao()

    val allAnimeMarks: Flow<List<AnimeEntity>> = animeDao.getAll()

    suspend fun addAnimeMark(anime: AnimeEntity) {
        animeDao.insert(anime)
    }

    suspend fun removeAnimeMark(anime: AnimeEntity) {
        animeDao.delete(anime)
    }

    suspend fun isBookmarked(animeId: Int): Boolean {
        return animeDao.isBookmarked(animeId)
    }
}