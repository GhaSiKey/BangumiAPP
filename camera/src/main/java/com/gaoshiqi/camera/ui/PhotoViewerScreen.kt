package com.gaoshiqi.camera.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculatePan
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.ui.input.pointer.positionChanged
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.gaoshiqi.camera.R
import com.gaoshiqi.camera.viewmodel.PhotoItem

@OptIn(ExperimentalMaterial3Api::class, ExperimentalGlideComposeApi::class)
@Composable
fun PhotoViewerScreen(
    photos: List<PhotoItem>,
    initialIndex: Int,
    showDeleteDialog: Boolean,
    onPhotoChanged: (PhotoItem) -> Unit,
    onBack: () -> Unit,
    onDelete: (PhotoItem) -> Unit,
    onConfirmDelete: () -> Unit,
    onCancelDelete: () -> Unit
) {
    val pagerState = rememberPagerState(
        initialPage = initialIndex,
        pageCount = { photos.size }
    )

    // 当前显示的照片
    val currentPhoto = photos.getOrNull(pagerState.currentPage) ?: return

    // 监听页面变化，通知外部
    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.currentPage }
            .collect { page ->
                photos.getOrNull(page)?.let { onPhotoChanged(it) }
            }
    }

    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing,
        topBar = {
            TopAppBar(
                title = { Text(currentPhoto.fileName) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_back),
                            contentDescription = stringResource(R.string.camera_back)
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { onDelete(currentPhoto) }) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_delete),
                            contentDescription = stringResource(R.string.camera_delete)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Black,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White,
                    actionIconContentColor = Color.White
                )
            )
        },
        containerColor = Color.Black
    ) { paddingValues ->
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            key = { photos[it].uri }
        ) { page ->
            val photo = photos[page]
            PhotoPage(photo = photo)
        }
    }

    if (showDeleteDialog) {
        DeleteConfirmDialog(
            onConfirm = onConfirmDelete,
            onDismiss = onCancelDelete
        )
    }
}

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
private fun PhotoPage(photo: PhotoItem) {
    var scale by remember { mutableFloatStateOf(1f) }
    var offsetX by remember { mutableFloatStateOf(0f) }
    var offsetY by remember { mutableFloatStateOf(0f) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                awaitEachGesture {
                    awaitFirstDown(requireUnconsumed = false)
                    do {
                        val event = awaitPointerEvent()
                        val pointerCount = event.changes.count { it.pressed }

                        if (pointerCount >= 2) {
                            // 双指：只缩放，不切换图片
                            val zoom = event.calculateZoom()
                            scale = (scale * zoom).coerceIn(1f, 5f)

                            if (scale <= 1f) {
                                offsetX = 0f
                                offsetY = 0f
                            }

                            event.changes.forEach { if (it.positionChanged()) it.consume() }
                        } else if (pointerCount == 1) {
                            val pan = event.calculatePan()

                            if (scale > 1f) {
                                // 已放大：计算边界
                                val maxOffsetX = (scale - 1f) * size.width / 2
                                val maxOffsetY = (scale - 1f) * size.height / 2

                                val atLeftEdge = offsetX >= maxOffsetX
                                val atRightEdge = offsetX <= -maxOffsetX
                                val swipingRight = pan.x > 0
                                val swipingLeft = pan.x < 0

                                // 判断是否应该让 Pager 处理切换
                                val shouldPassToPager = (atLeftEdge && swipingRight) || (atRightEdge && swipingLeft)

                                if (!shouldPassToPager) {
                                    // 还可以拖拽，更新偏移
                                    offsetX = (offsetX + pan.x).coerceIn(-maxOffsetX, maxOffsetX)
                                    offsetY = (offsetY + pan.y).coerceIn(-maxOffsetY, maxOffsetY)
                                    event.changes.forEach { if (it.positionChanged()) it.consume() }
                                }
                                // shouldPassToPager 时不消费事件，让 Pager 处理
                            }
                            // scale == 1f 时不消费事件，让 Pager 处理切换
                        }
                    } while (event.changes.any { it.pressed })
                }
            }
            .pointerInput(Unit) {
                detectTapGestures(
                    onDoubleTap = {
                        // 双击切换缩放
                        if (scale > 1f) {
                            scale = 1f
                            offsetX = 0f
                            offsetY = 0f
                        } else {
                            scale = 2.5f
                        }
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        GlideImage(
            model = photo.uri,
            contentDescription = photo.fileName,
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer(
                    scaleX = scale,
                    scaleY = scale,
                    translationX = offsetX,
                    translationY = offsetY
                ),
            contentScale = ContentScale.Fit
        )
    }
}

@Composable
private fun DeleteConfirmDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.camera_delete_confirm_title)) },
        text = { Text(stringResource(R.string.camera_delete_confirm_message)) },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(
                    text = stringResource(R.string.camera_delete),
                    color = Color.Red
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(android.R.string.cancel))
            }
        }
    )
}
