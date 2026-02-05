package com.gaoshiqi.camera.gallery

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.gaoshiqi.camera.ui.GalleryScreen
import com.gaoshiqi.camera.ui.PhotoViewerScreen

/**
 * 独立相册 Activity
 * 用于浏览和管理拍摄的照片
 */
class GalleryActivity : ComponentActivity() {

    companion object {
        fun start(context: Context) {
            val intent = Intent(context, GalleryActivity::class.java)
            context.startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val viewModel: GalleryViewModel = viewModel(
                        factory = GalleryViewModel.Factory(applicationContext)
                    )
                    val uiState by viewModel.uiState.collectAsState()

                    LaunchedEffect(uiState.shouldClose) {
                        if (uiState.shouldClose) {
                            finish()
                        }
                    }

                    BackHandler {
                        viewModel.handleIntent(GalleryIntent.NavigateBack)
                    }

                    when (val screenState = uiState.screenState) {
                        is GalleryScreenState.Gallery -> {
                            GalleryScreen(
                                photos = uiState.photos,
                                isSelectionMode = uiState.isSelectionMode,
                                selectedPhotos = uiState.selectedPhotos,
                                showDeleteDialog = uiState.showDeleteDialog,
                                onPhotoClick = { uri ->
                                    if (uiState.isSelectionMode) {
                                        viewModel.handleIntent(GalleryIntent.TogglePhotoSelection(uri))
                                    } else {
                                        viewModel.handleIntent(GalleryIntent.SelectPhoto(uri))
                                    }
                                },
                                onPhotoLongClick = { uri ->
                                    viewModel.handleIntent(GalleryIntent.EnterSelectionMode)
                                    viewModel.handleIntent(GalleryIntent.TogglePhotoSelection(uri))
                                },
                                onToggleSelection = { uri ->
                                    viewModel.handleIntent(GalleryIntent.TogglePhotoSelection(uri))
                                },
                                onDeleteSelected = {
                                    viewModel.handleIntent(GalleryIntent.RequestDeleteSelected)
                                },
                                onConfirmDelete = {
                                    viewModel.handleIntent(GalleryIntent.ConfirmDeleteSelected)
                                },
                                onCancelDelete = {
                                    viewModel.handleIntent(GalleryIntent.CancelDeleteSelected)
                                },
                                onExitSelectionMode = {
                                    viewModel.handleIntent(GalleryIntent.ExitSelectionMode)
                                },
                                onBack = {
                                    viewModel.handleIntent(GalleryIntent.NavigateBack)
                                }
                            )
                        }
                        is GalleryScreenState.PhotoViewer -> {
                            PhotoViewerScreen(
                                photos = uiState.photos,
                                initialIndex = screenState.index,
                                showDeleteDialog = uiState.showDeletePhotoDialog,
                                onPhotoChanged = { photo ->
                                    viewModel.handleIntent(GalleryIntent.ViewPhoto(photo))
                                },
                                onBack = {
                                    viewModel.handleIntent(GalleryIntent.NavigateBack)
                                },
                                onDelete = { photo ->
                                    viewModel.handleIntent(GalleryIntent.RequestDeletePhoto(photo))
                                },
                                onConfirmDelete = {
                                    viewModel.handleIntent(GalleryIntent.ConfirmDeletePhoto)
                                },
                                onCancelDelete = {
                                    viewModel.handleIntent(GalleryIntent.CancelDeletePhoto)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
