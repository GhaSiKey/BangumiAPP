package com.gaoshiqi.room

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface SavedPointDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(point: SavedPointEntity)

    @Delete
    suspend fun delete(point: SavedPointEntity)

    @Query("DELETE FROM saved_points WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("SELECT * FROM saved_points ORDER BY savedTime DESC")
    fun getAll(): Flow<List<SavedPointEntity>>

    @Query("SELECT * FROM saved_points WHERE subjectId = :subjectId ORDER BY savedTime DESC")
    fun getBySubjectId(subjectId: Int): Flow<List<SavedPointEntity>>

    @Query("SELECT EXISTS(SELECT * FROM saved_points WHERE id = :id)")
    suspend fun isSaved(id: String): Boolean

    @Query("SELECT EXISTS(SELECT * FROM saved_points WHERE subjectId = :subjectId AND pointId = :pointId)")
    suspend fun isSaved(subjectId: Int, pointId: String): Boolean
}
