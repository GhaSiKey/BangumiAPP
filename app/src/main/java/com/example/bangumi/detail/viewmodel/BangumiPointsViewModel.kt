package com.example.bangumi.detail.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bangumi.data.api.AnitabiClient
import kotlinx.coroutines.launch

/**
 * Created by gaoshiqi
 * on 2025/6/22 21:07
 * email: gaoshiqi@bilibili.com
 */
class BangumiPointsViewModel: ViewModel() {
    private val _state = MutableLiveData<BangumiPointsState>(null)
    val state = _state

    fun loadPoints(subjectId: Int) {
        _state.value = BangumiPointsState.LOADING
        viewModelScope.launch {
            try {
                val result = AnitabiClient.instance.getSubjectPoints(subjectId)
                _state.value = BangumiPointsState.SUCCESS(result)
            } catch (e: Exception) {
                _state.value = BangumiPointsState.ERROR("加载失败: ${e.message?: "未知错误"}")
            }
        }
    }
}