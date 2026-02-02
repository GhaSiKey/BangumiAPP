package com.gaoshiqi.camera.comparison.viewmodel

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import androidx.camera.core.AspectRatio
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.bumptech.glide.Glide
import com.gaoshiqi.camera.util.BitmapComposer
import com.gaoshiqi.camera.util.PhotoManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

/**
 * 对比拍照 ViewModel
 * 负责相机控制、参考图加载、图片合成等核心逻辑
 */
class ComparisonCameraViewModel(
    private val context: Context,
    private val referenceImageUrl: String,
    private val pointName: String,
    private val subjectName: String
) : ViewModel() {

    companion object {
        private const val TAG = "ComparisonCameraVM"
    }

    private val _uiState = MutableStateFlow(
        ComparisonCameraUiState(
            referenceImageUrl = referenceImageUrl,
            pointName = pointName,
            subjectName = subjectName
        )
    )
    val uiState: StateFlow<ComparisonCameraUiState> = _uiState.asStateFlow()

    private val photoManager = PhotoManager(context)
    private var cameraProvider: ProcessCameraProvider? = null
    private var camera: Camera? = null
    private var imageCapture: ImageCapture? = null
    private var preview: Preview? = null
    private var previewView: PreviewView? = null
    private val cameraExecutor: ExecutorService = Executors.newSingleThreadExecutor()

    /** 缓存的参考图 Bitmap */
    private var referenceBitmap: Bitmap? = null

    /** 当前预览的合成图（用于保存） */
    private var currentComposedBitmap: Bitmap? = null

    init {
        loadReferenceImage()
    }

    fun handleIntent(intent: ComparisonCameraIntent) {
        when (intent) {
            is ComparisonCameraIntent.TakePhoto -> takePhoto()
            is ComparisonCameraIntent.SwitchCamera -> switchCamera()
            is ComparisonCameraIntent.ConfirmPhoto -> confirmPhoto()
            is ComparisonCameraIntent.RetakePhoto -> retakePhoto()
            is ComparisonCameraIntent.NavigateBack -> navigateBack()
            is ComparisonCameraIntent.CameraReady -> onCameraReady()
            is ComparisonCameraIntent.CameraError -> onCameraError(intent.message)
            is ComparisonCameraIntent.RetryLoadReference -> loadReferenceImage()
        }
    }

    /**
     * 加载参考图片
     */
    private fun loadReferenceImage() {
        _uiState.update { it.copy(referenceState = ReferenceImageState.Loading) }

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val bitmap = Glide.with(context)
                    .asBitmap()
                    .load(referenceImageUrl)
                    .submit()
                    .get()

                referenceBitmap = bitmap
                _uiState.update { it.copy(referenceState = ReferenceImageState.Success(bitmap)) }
                Log.d(TAG, "Reference image loaded: ${bitmap.width}x${bitmap.height}")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to load reference image", e)
                _uiState.update {
                    it.copy(referenceState = ReferenceImageState.Error(e.message ?: "Failed to load reference image"))
                }
            }
        }
    }

    /**
     * 绑定相机
     * 使用 16:9 比例以便与参考图保持一致
     */
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
                handleIntent(ComparisonCameraIntent.CameraReady)
            } catch (e: Exception) {
                Log.e(TAG, "Camera initialization failed", e)
                handleIntent(ComparisonCameraIntent.CameraError(e.message ?: "Camera initialization failed"))
            }
        }, ContextCompat.getMainExecutor(context))
    }

    private fun bindCameraUseCases(
        lifecycleOwner: LifecycleOwner,
        previewView: PreviewView
    ) {
        val provider = cameraProvider ?: return

        val lensFacing = when (_uiState.value.lensFacing) {
            ComparisonLensFacing.BACK -> CameraSelector.LENS_FACING_BACK
            ComparisonLensFacing.FRONT -> CameraSelector.LENS_FACING_FRONT
        }

        val cameraSelector = CameraSelector.Builder()
            .requireLensFacing(lensFacing)
            .build()

        // 使用 16:9 比例
        preview = Preview.Builder()
            .setTargetAspectRatio(AspectRatio.RATIO_16_9)
            .build()
            .also {
                it.surfaceProvider = previewView.surfaceProvider
            }

        imageCapture = ImageCapture.Builder()
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
            .setTargetAspectRatio(AspectRatio.RATIO_16_9)
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
            handleIntent(ComparisonCameraIntent.CameraError(e.message ?: "Camera binding failed"))
        }
    }

    fun rebindCamera(lifecycleOwner: LifecycleOwner, previewView: PreviewView) {
        bindCameraUseCases(lifecycleOwner, previewView)
    }

    fun onCameraSwitchComplete() {
        _uiState.update { it.copy(isSwitchingCamera = false) }
    }

    /**
     * 拍照并合成
     */
    private fun takePhoto() {
        val refBitmap = referenceBitmap ?: return
        val cameraBitmap = previewView?.bitmap ?: run {
            _uiState.update {
                it.copy(captureState = CaptureState.Error("Failed to capture preview"))
            }
            return
        }

        _uiState.update { it.copy(captureState = CaptureState.Capturing) }

        // 在后台线程合成图片
        viewModelScope.launch(Dispatchers.Default) {
            try {
                _uiState.update { it.copy(captureState = CaptureState.Composing) }

                val isFrontCamera = _uiState.value.lensFacing == ComparisonLensFacing.FRONT
                val composedBitmap = BitmapComposer.composeVertically(
                    cameraImage = cameraBitmap,
                    referenceImage = refBitmap,
                    mirrorCamera = isFrontCamera
                )

                // 回收相机截图
                cameraBitmap.recycle()

                // 缓存合成图用于保存
                currentComposedBitmap = composedBitmap

                withContext(Dispatchers.Main) {
                    _uiState.update {
                        it.copy(
                            captureState = CaptureState.Idle,
                            screenState = ComparisonScreenState.PhotoPreview(composedBitmap)
                        )
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to compose images", e)
                withContext(Dispatchers.Main) {
                    _uiState.update {
                        it.copy(captureState = CaptureState.Error(e.message ?: "Failed to compose"))
                    }
                }
            }
        }
    }

    private fun switchCamera() {
        _uiState.update {
            it.copy(
                isSwitchingCamera = true,
                lensFacing = when (it.lensFacing) {
                    ComparisonLensFacing.BACK -> ComparisonLensFacing.FRONT
                    ComparisonLensFacing.FRONT -> ComparisonLensFacing.BACK
                }
            )
        }
    }

    /**
     * 确认保存照片
     */
    private fun confirmPhoto() {
        val composedBitmap = currentComposedBitmap ?: return

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val file = photoManager.createPhotoFile()
                file.outputStream().use { out ->
                    composedBitmap.compress(Bitmap.CompressFormat.JPEG, 95, out)
                }
                Log.d(TAG, "Photo saved: ${file.absolutePath}")

                withContext(Dispatchers.Main) {
                    _uiState.update {
                        it.copy(
                            captureState = CaptureState.Saved,
                            screenState = ComparisonScreenState.Camera
                        )
                    }
                    // 重置状态
                    currentComposedBitmap = null
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to save photo", e)
                withContext(Dispatchers.Main) {
                    _uiState.update {
                        it.copy(captureState = CaptureState.Error(e.message ?: "Failed to save"))
                    }
                }
            }
        }
    }

    /**
     * 重新拍照
     */
    private fun retakePhoto() {
        // 不要 recycle bitmap，因为 Compose 可能还在绘制
        // Bitmap 会在 GC 时自动回收
        currentComposedBitmap = null
        _uiState.update {
            it.copy(
                screenState = ComparisonScreenState.Camera,
                captureState = CaptureState.Idle
            )
        }
    }

    private fun navigateBack() {
        when (_uiState.value.screenState) {
            is ComparisonScreenState.PhotoPreview -> {
                retakePhoto()
            }
            is ComparisonScreenState.Camera -> {
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

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    override fun onCleared() {
        super.onCleared()
        cameraExecutor.shutdown()
        // 不要 recycle bitmap：
        // 1. referenceBitmap 由 Glide 管理，不应手动回收
        // 2. currentComposedBitmap 可能还在被 Compose 绘制
        // 它们会在 GC 时自动回收
    }

    class Factory(
        private val context: Context,
        private val referenceImageUrl: String,
        private val pointName: String,
        private val subjectName: String
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(ComparisonCameraViewModel::class.java)) {
                return ComparisonCameraViewModel(
                    context.applicationContext,
                    referenceImageUrl,
                    pointName,
                    subjectName
                ) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
