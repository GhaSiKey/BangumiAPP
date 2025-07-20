package com.example.bangumi.schedule

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bangumi.data.api.BangumiClient
import com.example.bangumi.data.bean.CalendarResponse
import kotlinx.coroutines.launch

/**
 * Created by gaoshiqi
 * on 2025/5/30 11:43
 * email: gaoshiqi@bilibili.com
 */
class CalendarViewModel: ViewModel() {

    private val _calendarData = MutableLiveData<List<CalendarResponse>>(emptyList())
    val calendarData: LiveData<List<CalendarResponse>> = _calendarData

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String?>(null)
    val errorMessage: LiveData<String?> = _errorMessage

    fun loadCalendarData() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            try {
                val data = BangumiClient.instance.getCalendar()
                _calendarData.value = data
            } catch (e: Exception) {
                _errorMessage.value = "加载失败: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
}