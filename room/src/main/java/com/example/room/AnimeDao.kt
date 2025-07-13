package com.example.room

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface AnimeDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(anime: AnimeEntity)

    @Delete
    suspend fun delete(anime: AnimeEntity)

    @Query("SELECT * FROM bookmarked_anime ORDER BY bookmarkTime DESC")
    fun getAll(): Flow<List<AnimeEntity>>

    @Query("SELECT EXISTS(SELECT * FROM bookmarked_anime WHERE id = :animeId)")
    suspend fun isBookmarked(animeId: Int): Boolean
}