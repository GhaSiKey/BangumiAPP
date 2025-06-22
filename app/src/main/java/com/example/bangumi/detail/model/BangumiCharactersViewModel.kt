package com.example.bangumi.detail.model

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bangumi.data.api.BangumiClient
import kotlinx.coroutines.launch

/**
 * Created by gaoshiqi
 * on 2025/6/9 18:55
 * email: gaoshiqi@bilibili.com
 */
class BangumiCharactersViewModel: ViewModel() {

    private val _state = MutableLiveData<BangumiCharacterState>(null)
    val state = _state

    fun loadCharacters(subjectId: Int) {
        _state.value = BangumiCharacterState.LOADING
        viewModelScope.launch {
            try {
                val result = BangumiClient.instance.getSubjectCharacters(subjectId)
                _state.value = BangumiCharacterState.SUCCESS(result)
            } catch (e: Exception) {
                _state.value = BangumiCharacterState.ERROR("加载失败: ${e.message ?: "未知错误"}")
            }
        }
    }
}