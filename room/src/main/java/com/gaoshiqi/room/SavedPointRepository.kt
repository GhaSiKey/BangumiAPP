package com.gaoshiqi.room

import android.content.Context
import kotlinx.coroutines.flow.Flow

class SavedPointRepository(context: Context) {
    private val savedPointDao = AppDatabase.getDatabase(context).savedPointDao()

    val allSavedPoints: Flow<List<SavedPointEntity>> = savedPointDao.getAll()

    fun getSavedPointsBySubject(subjectId: Int): Flow<List<SavedPointEntity>> {
        return savedPointDao.getBySubjectId(subjectId)
    }

    suspend fun savePoint(point: SavedPointEntity) {
        savedPointDao.insert(point)
    }

    suspend fun removePoint(point: SavedPointEntity) {
        savedPointDao.delete(point)
    }

    suspend fun removePointById(id: String) {
        savedPointDao.deleteById(id)
    }

    suspend fun isSaved(subjectId: Int, pointId: String): Boolean {
        return savedPointDao.isSaved(subjectId, pointId)
    }

    suspend fun isSaved(id: String): Boolean {
        return savedPointDao.isSaved(id)
    }
}
