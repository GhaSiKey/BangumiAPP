package com.gaoshiqi.otakumap.demo

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.gaoshiqi.room.SavedPointEntity
import com.gaoshiqi.room.SavedPointRepository
import kotlinx.coroutines.flow.Flow

/**
 * UI Demo ViewModel - 提供圣地巡礼数据
 */
class UiDemoViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = SavedPointRepository(application)

    val savedPoints: Flow<List<SavedPointEntity>> = repository.allSavedPoints
}
