package com.gaoshiqi.room

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

    suspend fun removeAnimeMarkById(animeId: Int) {
        animeDao.deleteById(animeId)
    }

    suspend fun isBookmarked(animeId: Int): Boolean {
        return animeDao.isBookmarked(animeId)
    }

    suspend fun getAnimeById(animeId: Int): AnimeEntity? {
        return animeDao.getById(animeId)
    }

    // 按收藏状态查询
    fun getAnimeByStatus(status: Int): Flow<List<AnimeEntity>> {
        return animeDao.getByStatus(status)
    }

    // 按名称排序的全部收藏
    fun getAllSortedByName(): Flow<List<AnimeEntity>> {
        return animeDao.getAllSortedByName()
    }

    // 按状态查询并按名称排序
    fun getByStatusSortedByName(status: Int): Flow<List<AnimeEntity>> {
        return animeDao.getByStatusSortedByName(status)
    }

    // 更新收藏状态
    suspend fun updateCollectionStatus(animeId: Int, status: Int) {
        animeDao.updateStatus(animeId, status)
    }

    // 更新观看进度
    suspend fun updateWatchedEpisodes(animeId: Int, episodes: Int) {
        animeDao.updateProgress(animeId, episodes)
    }

    // 更新总集数
    suspend fun updateTotalEpisodes(animeId: Int, total: Int) {
        animeDao.updateTotalEpisodes(animeId, total)
    }
}
