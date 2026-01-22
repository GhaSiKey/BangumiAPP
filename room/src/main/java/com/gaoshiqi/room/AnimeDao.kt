package com.gaoshiqi.room

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface AnimeDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(anime: AnimeEntity)

    @Update
    suspend fun update(anime: AnimeEntity)

    @Delete
    suspend fun delete(anime: AnimeEntity)

    @Query("DELETE FROM bookmarked_anime WHERE id = :animeId")
    suspend fun deleteById(animeId: Int)

    @Query("SELECT * FROM bookmarked_anime ORDER BY bookmarkTime DESC")
    fun getAll(): Flow<List<AnimeEntity>>

    @Query("SELECT * FROM bookmarked_anime WHERE id = :animeId")
    suspend fun getById(animeId: Int): AnimeEntity?

    @Query("SELECT EXISTS(SELECT * FROM bookmarked_anime WHERE id = :animeId)")
    suspend fun isBookmarked(animeId: Int): Boolean

    // 按收藏状态查询
    @Query("SELECT * FROM bookmarked_anime WHERE collectionStatus = :status ORDER BY bookmarkTime DESC")
    fun getByStatus(status: Int): Flow<List<AnimeEntity>>

    // 按名称排序（中文名优先）
    @Query("SELECT * FROM bookmarked_anime ORDER BY CASE WHEN nameCn = '' THEN name ELSE nameCn END ASC")
    fun getAllSortedByName(): Flow<List<AnimeEntity>>

    // 按状态查询并按名称排序
    @Query("SELECT * FROM bookmarked_anime WHERE collectionStatus = :status ORDER BY CASE WHEN nameCn = '' THEN name ELSE nameCn END ASC")
    fun getByStatusSortedByName(status: Int): Flow<List<AnimeEntity>>

    // 更新收藏状态
    @Query("UPDATE bookmarked_anime SET collectionStatus = :status WHERE id = :animeId")
    suspend fun updateStatus(animeId: Int, status: Int)

    // 更新观看进度
    @Query("UPDATE bookmarked_anime SET watchedEpisodes = :episodes WHERE id = :animeId")
    suspend fun updateProgress(animeId: Int, episodes: Int)

    // 更新总集数
    @Query("UPDATE bookmarked_anime SET totalEpisodes = :total WHERE id = :animeId")
    suspend fun updateTotalEpisodes(animeId: Int, total: Int)
}
