package com.gaoshiqi.player.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class PlayerViewModel(
    private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(PlayerUiState())
    val uiState: StateFlow<PlayerUiState> = _uiState.asStateFlow()

    private var exoPlayer: ExoPlayer? = null
    private var progressJob: Job? = null

    private val playerListener = object : Player.Listener {
        override fun onPlaybackStateChanged(playbackState: Int) {
            when (playbackState) {
                Player.STATE_IDLE -> {
                    _uiState.update { it.copy(playerState = PlayerState.Idle) }
                }
                Player.STATE_BUFFERING -> {
                    _uiState.update { it.copy(playerState = PlayerState.Loading) }
                }
                Player.STATE_READY -> {
                    val player = exoPlayer ?: return
                    val duration = player.duration.coerceAtLeast(0L)
                    if (player.isPlaying) {
                        _uiState.update {
                            it.copy(
                                playerState = PlayerState.Playing(
                                    currentPosition = player.currentPosition,
                                    duration = duration,
                                    bufferedPosition = player.bufferedPosition
                                ),
                                isPlaying = true,
                                duration = duration
                            )
                        }
                        startProgressUpdate()
                    } else {
                        _uiState.update {
                            it.copy(
                                playerState = PlayerState.Paused(
                                    currentPosition = player.currentPosition,
                                    duration = duration
                                ),
                                isPlaying = false,
                                duration = duration
                            )
                        }
                        stopProgressUpdate()
                    }
                }
                Player.STATE_ENDED -> {
                    _uiState.update {
                        it.copy(
                            playerState = PlayerState.Paused(
                                currentPosition = it.duration,
                                duration = it.duration
                            ),
                            isPlaying = false,
                            currentPosition = it.duration
                        )
                    }
                    stopProgressUpdate()
                }
            }
        }

        override fun onIsPlayingChanged(isPlaying: Boolean) {
            val player = exoPlayer ?: return
            _uiState.update {
                it.copy(
                    isPlaying = isPlaying,
                    playerState = if (isPlaying) {
                        PlayerState.Playing(
                            currentPosition = player.currentPosition,
                            duration = player.duration.coerceAtLeast(0L),
                            bufferedPosition = player.bufferedPosition
                        )
                    } else {
                        PlayerState.Paused(
                            currentPosition = player.currentPosition,
                            duration = player.duration.coerceAtLeast(0L)
                        )
                    }
                )
            }
            if (isPlaying) {
                startProgressUpdate()
            } else {
                stopProgressUpdate()
            }
        }

        override fun onPlayerError(error: PlaybackException) {
            _uiState.update {
                it.copy(
                    playerState = PlayerState.Error(
                        error.localizedMessage ?: "播放出错"
                    ),
                    isPlaying = false
                )
            }
            stopProgressUpdate()
        }
    }

    fun handleIntent(intent: PlayerIntent) {
        when (intent) {
            is PlayerIntent.SetUrl -> setVideoUrl(intent.url)
            is PlayerIntent.Play -> play()
            is PlayerIntent.Pause -> pause()
            is PlayerIntent.TogglePlayPause -> togglePlayPause()
            is PlayerIntent.SeekTo -> seekTo(intent.position)
            is PlayerIntent.Retry -> retry()
            is PlayerIntent.Release -> release()
        }
    }

    private fun setVideoUrl(url: String) {
        _uiState.update { it.copy(videoUrl = url) }
    }

    private fun play() {
        val url = _uiState.value.videoUrl
        if (url.isBlank()) {
            _uiState.update {
                it.copy(playerState = PlayerState.Error("请输入视频地址"))
            }
            return
        }

        _uiState.update { it.copy(playerState = PlayerState.Loading) }

        if (exoPlayer == null) {
            exoPlayer = ExoPlayer.Builder(context).build().apply {
                addListener(playerListener)
            }
        }

        exoPlayer?.apply {
            val mediaItem = MediaItem.fromUri(url)
            setMediaItem(mediaItem)
            prepare()
            playWhenReady = true
        }
    }

    private fun pause() {
        exoPlayer?.pause()
    }

    private fun togglePlayPause() {
        val player = exoPlayer
        if (player == null) {
            play()
            return
        }
        if (player.isPlaying) {
            player.pause()
        } else {
            player.play()
        }
    }

    private fun seekTo(position: Long) {
        exoPlayer?.seekTo(position)
        _uiState.update { it.copy(currentPosition = position) }
    }

    private fun retry() {
        release()
        play()
    }

    private fun release() {
        stopProgressUpdate()
        exoPlayer?.apply {
            removeListener(playerListener)
            release()
        }
        exoPlayer = null
        _uiState.update {
            it.copy(
                playerState = PlayerState.Idle,
                isPlaying = false,
                currentPosition = 0L,
                duration = 0L
            )
        }
    }

    private fun startProgressUpdate() {
        stopProgressUpdate()
        progressJob = viewModelScope.launch {
            while (isActive) {
                exoPlayer?.let { player ->
                    _uiState.update {
                        it.copy(
                            currentPosition = player.currentPosition,
                            bufferedPosition = player.bufferedPosition
                        )
                    }
                }
                delay(500L)
            }
        }
    }

    private fun stopProgressUpdate() {
        progressJob?.cancel()
        progressJob = null
    }

    fun getPlayer(): ExoPlayer? = exoPlayer

    override fun onCleared() {
        super.onCleared()
        release()
    }

    class Factory(private val context: Context) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(PlayerViewModel::class.java)) {
                return PlayerViewModel(context.applicationContext) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
