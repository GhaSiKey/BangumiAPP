package com.gaoshiqi.camera.gallery

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.gaoshiqi.camera.util.PhotoManager
import com.gaoshiqi.camera.viewmodel.PhotoItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * 相册页面 Intent
 */
sealed class GalleryIntent {
    data class SelectPhoto(val uri: String) : GalleryIntent()
    data class ViewPhoto(val photo: PhotoItem) : GalleryIntent()
    data object EnterSelectionMode : GalleryIntent()
    data object ExitSelectionMode : GalleryIntent()
    data class TogglePhotoSelection(val uri: String) : GalleryIntent()
    data object RequestDeleteSelected : GalleryIntent()
    data object ConfirmDeleteSelected : GalleryIntent()
    data object CancelDeleteSelected : GalleryIntent()
    data class RequestDeletePhoto(val photo: PhotoItem) : GalleryIntent()
    data object ConfirmDeletePhoto : GalleryIntent()
    data object CancelDeletePhoto : GalleryIntent()
    data object NavigateBack : GalleryIntent()
}

/**
 * 相册页面屏幕状态
 */
sealed class GalleryScreenState {
    data object Gallery : GalleryScreenState()
    data class PhotoViewer(val photo: PhotoItem, val index: Int) : GalleryScreenState()
}

/**
 * 相册页面 UI 状态
 */
data class GalleryUiState(
    val screenState: GalleryScreenState = GalleryScreenState.Gallery,
    val photos: List<PhotoItem> = emptyList(),
    val isSelectionMode: Boolean = false,
    val selectedPhotos: Set<String> = emptySet(),
    val showDeleteDialog: Boolean = false,
    val showDeletePhotoDialog: Boolean = false,
    val pendingDeletePhoto: PhotoItem? = null,
    val shouldClose: Boolean = false
)

/**
 * 独立相册 ViewModel
 */
class GalleryViewModel(
    context: Context
) : ViewModel() {

    private val photoManager = PhotoManager(context)

    private val _uiState = MutableStateFlow(GalleryUiState())
    val uiState: StateFlow<GalleryUiState> = _uiState.asStateFlow()

    init {
        loadPhotos()
    }

    fun handleIntent(intent: GalleryIntent) {
        when (intent) {
            is GalleryIntent.SelectPhoto -> selectPhoto(intent.uri)
            is GalleryIntent.ViewPhoto -> viewPhoto(intent.photo)
            is GalleryIntent.EnterSelectionMode -> enterSelectionMode()
            is GalleryIntent.ExitSelectionMode -> exitSelectionMode()
            is GalleryIntent.TogglePhotoSelection -> togglePhotoSelection(intent.uri)
            is GalleryIntent.RequestDeleteSelected -> requestDeleteSelected()
            is GalleryIntent.ConfirmDeleteSelected -> confirmDeleteSelected()
            is GalleryIntent.CancelDeleteSelected -> cancelDeleteSelected()
            is GalleryIntent.RequestDeletePhoto -> requestDeletePhoto(intent.photo)
            is GalleryIntent.ConfirmDeletePhoto -> confirmDeletePhoto()
            is GalleryIntent.CancelDeletePhoto -> cancelDeletePhoto()
            is GalleryIntent.NavigateBack -> navigateBack()
        }
    }

    private fun loadPhotos() {
        viewModelScope.launch {
            val photos = withContext(Dispatchers.IO) {
                photoManager.getAllPhotos()
            }
            _uiState.update { it.copy(photos = photos) }
        }
    }

    private fun selectPhoto(uri: String) {
        val photos = _uiState.value.photos
        val index = photos.indexOfFirst { it.uri == uri }
        if (index >= 0) {
            _uiState.update {
                it.copy(screenState = GalleryScreenState.PhotoViewer(photos[index], index))
            }
        }
    }

    private fun viewPhoto(photo: PhotoItem) {
        val photos = _uiState.value.photos
        val index = photos.indexOfFirst { it.uri == photo.uri }
        val currentScreen = _uiState.value.screenState
        if (currentScreen is GalleryScreenState.PhotoViewer && index >= 0) {
            _uiState.update {
                it.copy(screenState = GalleryScreenState.PhotoViewer(photo, index))
            }
        }
    }

    private fun enterSelectionMode() {
        _uiState.update { it.copy(isSelectionMode = true, selectedPhotos = emptySet()) }
    }

    private fun exitSelectionMode() {
        _uiState.update {
            it.copy(
                isSelectionMode = false,
                selectedPhotos = emptySet(),
                showDeleteDialog = false
            )
        }
    }

    private fun togglePhotoSelection(uri: String) {
        _uiState.update { state ->
            val newSelection = if (uri in state.selectedPhotos) {
                state.selectedPhotos - uri
            } else {
                state.selectedPhotos + uri
            }
            if (newSelection.isEmpty()) {
                state.copy(isSelectionMode = false, selectedPhotos = emptySet())
            } else {
                state.copy(selectedPhotos = newSelection)
            }
        }
    }

    private fun requestDeleteSelected() {
        if (_uiState.value.selectedPhotos.isNotEmpty()) {
            _uiState.update { it.copy(showDeleteDialog = true) }
        }
    }

    private fun confirmDeleteSelected() {
        viewModelScope.launch {
            val selectedUris = _uiState.value.selectedPhotos.toList()
            withContext(Dispatchers.IO) {
                selectedUris.forEach { uri ->
                    photoManager.deletePhoto(uri)
                }
            }
            val photos = withContext(Dispatchers.IO) {
                photoManager.getAllPhotos()
            }
            _uiState.update {
                it.copy(
                    photos = photos,
                    isSelectionMode = false,
                    selectedPhotos = emptySet(),
                    showDeleteDialog = false
                )
            }
        }
    }

    private fun cancelDeleteSelected() {
        _uiState.update { it.copy(showDeleteDialog = false) }
    }

    private fun requestDeletePhoto(photo: PhotoItem) {
        _uiState.update {
            it.copy(
                pendingDeletePhoto = photo,
                showDeletePhotoDialog = true
            )
        }
    }

    private fun confirmDeletePhoto() {
        val photo = _uiState.value.pendingDeletePhoto ?: return
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                photoManager.deletePhoto(photo.uri)
            }
            val photos = withContext(Dispatchers.IO) {
                photoManager.getAllPhotos()
            }
            _uiState.update {
                it.copy(
                    photos = photos,
                    pendingDeletePhoto = null,
                    showDeletePhotoDialog = false,
                    screenState = GalleryScreenState.Gallery
                )
            }
        }
    }

    private fun cancelDeletePhoto() {
        _uiState.update {
            it.copy(
                pendingDeletePhoto = null,
                showDeletePhotoDialog = false
            )
        }
    }

    private fun navigateBack() {
        when (_uiState.value.screenState) {
            is GalleryScreenState.PhotoViewer -> {
                _uiState.update { it.copy(screenState = GalleryScreenState.Gallery) }
            }
            is GalleryScreenState.Gallery -> {
                _uiState.update { it.copy(shouldClose = true) }
            }
        }
    }

    class Factory(private val context: Context) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(GalleryViewModel::class.java)) {
                return GalleryViewModel(context.applicationContext) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
