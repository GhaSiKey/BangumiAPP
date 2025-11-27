package com.gaoshiqi.otakumap.detail.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gaoshiqi.otakumap.data.api.BangumiClient
import kotlinx.coroutines.launch

/**
 * Created by gaoshiqi
 * on 2025/6/1 17:23
 * email: gaoshiqi@bilibili.com
 */
class BangumiDetailViewModel: ViewModel() {

    private val _state = MutableLiveData<BangumiDetailState>(null)
    val state: LiveData<BangumiDetailState> = _state

    private var mSubjectId: Int = -1

    fun setSubjectId(id: Int) {
        mSubjectId = id
        processIntent(BangumiDetailIntent.LoadBangumiDetail)
    }

    fun processIntent(intent: BangumiDetailIntent) {
        when (intent) {
            BangumiDetailIntent.LoadBangumiDetail -> loadSubjectDetail()
            BangumiDetailIntent.Retry -> loadSubjectDetail()
        }
    }

    private fun loadSubjectDetail() {
        if (mSubjectId > 0) {
            _state.value = BangumiDetailState.LOADING
            viewModelScope.launch {
                try {
                    val result = BangumiClient.instance.getSubjectDetail(mSubjectId)
                    _state.value = BangumiDetailState.SUCCESS(result)
                } catch (e: Exception) {
                    _state.value = BangumiDetailState.ERROR("加载失败: ${e.message ?: "未知错误"}")
                }
            }
        }
    }
}