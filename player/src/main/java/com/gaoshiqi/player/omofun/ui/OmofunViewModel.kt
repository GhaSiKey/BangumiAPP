package com.gaoshiqi.player.omofun.ui

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.gaoshiqi.player.omofun.data.OmofunRepository
import com.gaoshiqi.player.omofun.data.model.OmofunAnimeDetail
import com.gaoshiqi.player.omofun.data.model.OmofunEpisode
import com.gaoshiqi.player.omofun.data.model.OmofunSearchResult
import com.gaoshiqi.player.omofun.extractor.VideoExtractor
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Omofun 功能的共用 ViewModel
 * 管理搜索、详情、视频提取的状态
 */
class OmofunViewModel(application: Application) : AndroidViewModel(application) {

    companion object {
        private const val TAG = "OmofunViewModel"
    }

    private val repository = OmofunRepository()
    private val videoExtractor = VideoExtractor(application.applicationContext)

    // UI 状态
    private val _uiState = MutableStateFlow(OmofunUiState())
    val uiState: StateFlow<OmofunUiState> = _uiState.asStateFlow()

    // 搜索结果
    private val _searchResults = MutableStateFlow<List<OmofunSearchResult>>(emptyList())
    val searchResults: StateFlow<List<OmofunSearchResult>> = _searchResults.asStateFlow()

    // 番剧详情
    private val _animeDetail = MutableStateFlow<OmofunAnimeDetail?>(null)
    val animeDetail: StateFlow<OmofunAnimeDetail?> = _animeDetail.asStateFlow()

    // 播放事件 (一次性事件，用于跳转播放器)
    private val _playEvent = MutableSharedFlow<PlayEvent>()
    val playEvent: SharedFlow<PlayEvent> = _playEvent.asSharedFlow()

    /**
     * 搜索番剧
     */
    fun search(keyword: String) {
        if (keyword.isBlank()) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            repository.search(keyword)
                .onSuccess { results ->
                    Log.d(TAG, "搜索成功，找到 ${results.size} 个结果")
                    _searchResults.value = results
                    _uiState.update { it.copy(isLoading = false) }
                }
                .onFailure { e ->
                    Log.e(TAG, "搜索失败", e)
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = "搜索失败: ${e.message}"
                        )
                    }
                }
        }
    }

    /**
     * 加载番剧详情
     */
    fun loadDetail(detailUrl: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            repository.getDetail(detailUrl)
                .onSuccess { detail ->
                    Log.d(TAG, "获取详情成功: ${detail.title}")
                    _animeDetail.value = detail
                    _uiState.update { it.copy(isLoading = false) }
                }
                .onFailure { e ->
                    Log.e(TAG, "获取详情失败", e)
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = "获取详情失败: ${e.message}"
                        )
                    }
                }
        }
    }

    /**
     * 播放指定集数
     * 1. 显示提取中状态
     * 2. 使用 WebView 提取视频 URL
     * 3. 成功后发送播放事件
     */
    fun playEpisode(episode: OmofunEpisode) {
        viewModelScope.launch {
            _uiState.update { it.copy(isExtracting = true, error = null) }
            Log.d(TAG, "开始提取视频: ${episode.name} - ${episode.playUrl}")

            videoExtractor.extract(episode.playUrl)
                .onSuccess { videoUrl ->
                    Log.d(TAG, "视频提取成功: $videoUrl")
                    _uiState.update { it.copy(isExtracting = false) }

                    // 发送播放事件
                    val title = buildString {
                        animeDetail.value?.title?.let { append(it) }
                        append(" - ")
                        append(episode.name)
                    }
                    // 将播放页 URL 作为 Referer 传递，用于绕过 CDN 防盗链
                    _playEvent.emit(PlayEvent(
                        videoUrl = videoUrl,
                        title = title,
                        referer = episode.playUrl
                    ))
                }
                .onFailure { e ->
                    Log.e(TAG, "视频提取失败", e)
                    _uiState.update {
                        it.copy(
                            isExtracting = false,
                            error = "视频提取失败: ${e.message}"
                        )
                    }
                }
        }
    }

    /**
     * 清除错误信息
     */
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    /**
     * 清除详情数据 (返回搜索页时)
     */
    fun clearDetail() {
        _animeDetail.value = null
    }

    override fun onCleared() {
        super.onCleared()
        videoExtractor.release()
        Log.d(TAG, "ViewModel 已清理，VideoExtractor 已释放")
    }
}

/**
 * UI 状态
 */
data class OmofunUiState(
    /** 是否正在加载 (搜索/详情) */
    val isLoading: Boolean = false,
    /** 是否正在提取视频 */
    val isExtracting: Boolean = false,
    /** 错误信息 */
    val error: String? = null
)

/**
 * 播放事件
 * 使用 SharedFlow 作为一次性事件传递给 UI 层
 */
data class PlayEvent(
    /** 视频直链 */
    val videoUrl: String,
    /** 视频标题 */
    val title: String,
    /** Referer URL，用于绕过 CDN 防盗链 */
    val referer: String? = null
)
