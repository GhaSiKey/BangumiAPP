package com.gaoshiqi.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface SearchHistoryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(history: SearchHistoryEntity)

    @Query("SELECT * FROM search_history ORDER BY searchTime DESC LIMIT :limit")
    fun getRecentHistory(limit: Int = 10): Flow<List<SearchHistoryEntity>>

    @Query("DELETE FROM search_history")
    suspend fun clearAll()

    @Query("""
        DELETE FROM search_history
        WHERE keyword NOT IN (
            SELECT keyword FROM search_history
            ORDER BY searchTime DESC
            LIMIT :limit
        )
    """)
    suspend fun trimToLimit(limit: Int = 10)
}
