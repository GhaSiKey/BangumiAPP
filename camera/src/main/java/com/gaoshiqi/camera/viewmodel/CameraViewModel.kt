package com.gaoshiqi.camera.viewmodel

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.FocusMeteringAction
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.gaoshiqi.camera.util.PhotoManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class CameraViewModel(
    private val context: Context
) : ViewModel() {

    companion object {
        private const val TAG = "CameraViewModel"
    }

    private val _uiState = MutableStateFlow(CameraUiState())
    val uiState: StateFlow<CameraUiState> = _uiState.asStateFlow()

    private val photoManager = PhotoManager(context)
    private var cameraProvider: ProcessCameraProvider? = null
    private var camera: Camera? = null
    private var imageCapture: ImageCapture? = null
    private var preview: Preview? = null
    private var previewView: PreviewView? = null
    private val cameraExecutor: ExecutorService = Executors.newSingleThreadExecutor()

    init {
        loadGalleryPhotos()
    }

    fun handleIntent(intent: CameraIntent) {
        when (intent) {
            is CameraIntent.TakePhoto -> takePhoto()
            is CameraIntent.SwitchCamera -> switchCamera()
            is CameraIntent.ConfirmPhoto -> confirmPhoto()
            is CameraIntent.RetakePhoto -> retakePhoto()
            is CameraIntent.OpenGallery -> openGallery()
            is CameraIntent.CloseGallery -> closeGallery()
            is CameraIntent.SelectPhoto -> selectPhoto(intent.uri)
            is CameraIntent.DeletePhoto -> requestDeletePhoto(intent.uri)
            is CameraIntent.ConfirmDeletePhoto -> confirmDeletePhoto()
            is CameraIntent.CancelDeletePhoto -> cancelDeletePhoto()
            is CameraIntent.NavigateBack -> navigateBack()
            is CameraIntent.CameraReady -> onCameraReady()
            is CameraIntent.CameraError -> onCameraError(intent.message)
        }
    }

    fun bindCamera(
        lifecycleOwner: LifecycleOwner,
        previewView: PreviewView
    ) {
        this.previewView = previewView
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        cameraProviderFuture.addListener({
            try {
                cameraProvider = cameraProviderFuture.get()
                bindCameraUseCases(lifecycleOwner, previewView)
                handleIntent(CameraIntent.CameraReady)
            } catch (e: Exception) {
                Log.e(TAG, "Camera initialization failed", e)
                handleIntent(CameraIntent.CameraError(e.message ?: "Camera initialization failed"))
            }
        }, ContextCompat.getMainExecutor(context))
    }

    private fun bindCameraUseCases(
        lifecycleOwner: LifecycleOwner,
        previewView: PreviewView
    ) {
        val provider = cameraProvider ?: return

        val lensFacing = when (_uiState.value.lensFacing) {
            LensFacing.BACK -> CameraSelector.LENS_FACING_BACK
            LensFacing.FRONT -> CameraSelector.LENS_FACING_FRONT
        }

        val cameraSelector = CameraSelector.Builder()
            .requireLensFacing(lensFacing)
            .build()

        preview = Preview.Builder()
            .build()
            .also {
                it.surfaceProvider = previewView.surfaceProvider
            }

        imageCapture = ImageCapture.Builder()
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
            .build()

        try {
            provider.unbindAll()
            camera = provider.bindToLifecycle(
                lifecycleOwner,
                cameraSelector,
                preview,
                imageCapture
            )
        } catch (e: Exception) {
            Log.e(TAG, "Use case binding failed", e)
            handleIntent(CameraIntent.CameraError(e.message ?: "Camera binding failed"))
        }
    }

    /**
     * 点击聚焦
     */
    fun focusOnPoint(x: Float, y: Float) {
        val cam = camera ?: return
        val view = previewView ?: return

        val meteringPointFactory = view.meteringPointFactory
        val meteringPoint = meteringPointFactory.createPoint(x, y)

        val action = FocusMeteringAction.Builder(meteringPoint)
            .setAutoCancelDuration(3, TimeUnit.SECONDS)
            .build()

        cam.cameraControl.startFocusAndMetering(action)
        Log.d(TAG, "Focus on point: ($x, $y)")

        // 更新聚焦点位置用于 UI 显示
        _uiState.update { it.copy(focusPoint = FocusPoint(x, y)) }

        // 延迟清除聚焦点
        viewModelScope.launch {
            kotlinx.coroutines.delay(1500)
            _uiState.update { it.copy(focusPoint = null) }
        }
    }

    /**
     * 缩放
     */
    fun setZoomRatio(ratio: Float) {
        val cam = camera ?: return
        val zoomState = cam.cameraInfo.zoomState.value ?: return

        val minRatio = zoomState.minZoomRatio
        val maxRatio = zoomState.maxZoomRatio
        val clampedRatio = ratio.coerceIn(minRatio, maxRatio)

        cam.cameraControl.setZoomRatio(clampedRatio)
        _uiState.update { it.copy(currentZoomRatio = clampedRatio) }
    }

    /**
     * 获取当前缩放比例
     */
    fun getCurrentZoomRatio(): Float {
        return camera?.cameraInfo?.zoomState?.value?.zoomRatio ?: 1f
    }

    /**
     * 获取最大缩放比例
     */
    fun getMaxZoomRatio(): Float {
        return camera?.cameraInfo?.zoomState?.value?.maxZoomRatio ?: 1f
    }

    /**
     * 获取最小缩放比例
     */
    fun getMinZoomRatio(): Float {
        return camera?.cameraInfo?.zoomState?.value?.minZoomRatio ?: 1f
    }

    private fun takePhoto() {
        val capture = imageCapture ?: return

        _uiState.update { it.copy(isCapturing = true) }

        val outputFile = photoManager.createPhotoFile()
        val outputOptions = ImageCapture.OutputFileOptions.Builder(outputFile).build()

        capture.takePicture(
            outputOptions,
            cameraExecutor,
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                    val savedUri = Uri.fromFile(outputFile)
                    Log.d(TAG, "Photo saved: $savedUri")
                    viewModelScope.launch {
                        _uiState.update {
                            it.copy(
                                isCapturing = false,
                                latestPhotoUri = savedUri,
                                screenState = ScreenState.PhotoPreview(savedUri)
                            )
                        }
                        loadGalleryPhotos()
                    }
                }

                override fun onError(exception: ImageCaptureException) {
                    Log.e(TAG, "Photo capture failed", exception)
                    viewModelScope.launch {
                        _uiState.update {
                            it.copy(
                                isCapturing = false,
                                errorMessage = exception.message
                            )
                        }
                    }
                }
            }
        )
    }

    private fun switchCamera() {
        _uiState.update {
            it.copy(
                lensFacing = when (it.lensFacing) {
                    LensFacing.BACK -> LensFacing.FRONT
                    LensFacing.FRONT -> LensFacing.BACK
                }
            )
        }
    }

    private fun confirmPhoto() {
        _uiState.update { it.copy(screenState = ScreenState.Camera) }
    }

    private fun retakePhoto() {
        val currentState = _uiState.value.screenState
        if (currentState is ScreenState.PhotoPreview) {
            photoManager.deletePhoto(currentState.photoUri.path ?: "")
            loadGalleryPhotos()
        }
        _uiState.update {
            it.copy(
                screenState = ScreenState.Camera,
                latestPhotoUri = null
            )
        }
    }

    private fun openGallery() {
        loadGalleryPhotos()
        _uiState.update { it.copy(screenState = ScreenState.Gallery) }
    }

    private fun closeGallery() {
        _uiState.update { it.copy(screenState = ScreenState.Camera) }
    }

    private fun selectPhoto(uri: String) {
        val photo = _uiState.value.galleryPhotos.find { it.uri == uri }
        if (photo != null) {
            _uiState.update { it.copy(screenState = ScreenState.PhotoViewer(photo)) }
        }
    }

    private fun requestDeletePhoto(uri: String) {
        val photo = _uiState.value.galleryPhotos.find { it.uri == uri }
        if (photo != null) {
            _uiState.update { it.copy(pendingDeletePhoto = photo) }
        }
    }

    private fun confirmDeletePhoto() {
        val photo = _uiState.value.pendingDeletePhoto ?: return
        photoManager.deletePhoto(photo.uri)
        loadGalleryPhotos()

        val currentScreen = _uiState.value.screenState
        val newScreenState = if (currentScreen is ScreenState.PhotoViewer && currentScreen.photo.uri == photo.uri) {
            ScreenState.Gallery
        } else {
            currentScreen
        }

        _uiState.update {
            it.copy(
                pendingDeletePhoto = null,
                screenState = newScreenState
            )
        }
    }

    private fun cancelDeletePhoto() {
        _uiState.update { it.copy(pendingDeletePhoto = null) }
    }

    private fun navigateBack() {
        when (_uiState.value.screenState) {
            is ScreenState.PhotoPreview -> {
                retakePhoto()
            }
            is ScreenState.Gallery -> {
                closeGallery()
            }
            is ScreenState.PhotoViewer -> {
                openGallery()
            }
            is ScreenState.Camera -> {
                _uiState.update { it.copy(shouldClose = true) }
            }
        }
    }

    private fun onCameraReady() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    private fun onCameraError(message: String) {
        _uiState.update { it.copy(errorMessage = message) }
    }

    private fun loadGalleryPhotos() {
        val photos = photoManager.getAllPhotos()
        _uiState.update { it.copy(galleryPhotos = photos) }
    }

    fun rebindCamera(lifecycleOwner: LifecycleOwner, previewView: PreviewView) {
        bindCameraUseCases(lifecycleOwner, previewView)
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    override fun onCleared() {
        super.onCleared()
        cameraExecutor.shutdown()
    }

    class Factory(private val context: Context) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(CameraViewModel::class.java)) {
                return CameraViewModel(context.applicationContext) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
