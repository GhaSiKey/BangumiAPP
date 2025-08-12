package com.example.bangumi.detail.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bangumi.data.api.AnitabiClient
import com.example.bangumi.detail.adapter.PointListItem
import com.example.map.data.LitePoint
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
                val processedItems = handlePoints(result)
                _state.value = BangumiPointsState.SUCCESS(processedItems)
            } catch (e: Exception) {
                _state.value = BangumiPointsState.ERROR("加载失败: ${e.message?: "未知错误"}")
            }
        }
    }

    private fun handlePoints(points: List<LitePoint>): List<PointListItem> {
        if (points.isEmpty()) return emptyList()

        // 按集数分组
        val groupedByEpisode = points.groupBy { normalizeEpisode(it.ep) }
        
        // 对集数进行排序
        val sortedEpisodes = groupedByEpisode.keys.sortedWith(compareBy {
            when {
                it == "其他" -> Int.MAX_VALUE  // "其他" 排在最后
                it.matches("\\d+".toRegex()) -> it.toInt()  // 数字按数值排序
                else -> 1000 + it.hashCode()  // 其他字符串排在数字后面
            }
        })

        val result = mutableListOf<PointListItem>()
        
        for (episode in sortedEpisodes) {
            // 添加集数标题
            result.add(PointListItem.Header(episode))
            
            // 添加该集数下的所有地点，按时间点排序
            val episodePoints = groupedByEpisode[episode]?.sortedBy { 
                it.s?.toIntOrNull() ?: Int.MAX_VALUE 
            } ?: emptyList()
            
            episodePoints.forEach { point ->
                result.add(PointListItem.Point(point))
            }
        }
        
        return result
    }
    
    private fun normalizeEpisode(ep: String?): String {
        return when {
            ep.isNullOrBlank() || ep == "null" -> "其他"
            ep.matches("\\d+".toRegex()) -> ep  // 纯数字保持原样
            else -> ep  // 其他格式保持原样
        }
    }
    
    // 为Fragment提供原始LitePoint列表的方法（用于地图功能）
    fun getRawPoints(): List<LitePoint> {
        return when (val currentState = _state.value) {
            is BangumiPointsState.SUCCESS -> {
                currentState.data.filterIsInstance<PointListItem.Point>()
                    .map { it.litePoint }
            }
            else -> emptyList()
        }
    }
}