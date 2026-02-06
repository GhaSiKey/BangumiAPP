package com.gaoshiqi.otakumap.comparison

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.gaoshiqi.room.SavedPointEntity
import com.gaoshiqi.room.SavedPointRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * 圣地选择页面的 UI 状态
 */
sealed class SavedPointPickerState {
    data object Loading : SavedPointPickerState()
    data class Success(val points: List<SavedPointEntity>) : SavedPointPickerState()
    data class Empty(val message: String) : SavedPointPickerState()
    data class Error(val message: String) : SavedPointPickerState()
}

/**
 * 圣地选择 ViewModel
 * 从 Room 数据库读取已收藏的圣地列表
 */
class SavedPointPickerViewModel(
    context: Context
) : ViewModel() {

    private val repository = SavedPointRepository(context)

    private val _uiState = MutableStateFlow<SavedPointPickerState>(SavedPointPickerState.Loading)
    val uiState: StateFlow<SavedPointPickerState> = _uiState.asStateFlow()

    init {
        loadSavedPoints()
    }

    private fun loadSavedPoints() {
        viewModelScope.launch {
            repository.allSavedPoints.collect { points ->
                _uiState.value = if (points.isEmpty()) {
                    SavedPointPickerState.Empty("No saved locations yet")
                } else {
                    // 过滤掉没有图片的圣地
                    val validPoints = points.filter { it.pointImage.isNotBlank() }
                    if (validPoints.isEmpty()) {
                        SavedPointPickerState.Empty("No locations with images available")
                    } else {
                        SavedPointPickerState.Success(validPoints)
                    }
                }
            }
        }
    }

    class Factory(private val context: Context) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(SavedPointPickerViewModel::class.java)) {
                return SavedPointPickerViewModel(context.applicationContext) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
