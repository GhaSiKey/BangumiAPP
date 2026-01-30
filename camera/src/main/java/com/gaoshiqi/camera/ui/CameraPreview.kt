package com.gaoshiqi.camera.ui

import android.view.ViewGroup
import androidx.camera.view.PreviewView
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.gaoshiqi.camera.viewmodel.CameraViewModel
import com.gaoshiqi.camera.viewmodel.FocusPoint
import com.gaoshiqi.camera.viewmodel.LensFacing

/** 拍摄宽高比 4:3 */
private const val CAPTURE_ASPECT_RATIO = 4f / 3f

@Composable
fun CameraPreview(
    modifier: Modifier = Modifier,
    viewModel: CameraViewModel,
    lensFacing: LensFacing,
    focusPoint: FocusPoint?
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val previewView = remember {
        PreviewView(context).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            // FIT_CENTER 保持宽高比，确保预览内容完整显示
            scaleType = PreviewView.ScaleType.FIT_CENTER
        }
    }

    var currentZoom by remember { mutableFloatStateOf(1f) }

    DisposableEffect(lensFacing) {
        viewModel.rebindCamera(lifecycleOwner, previewView)
        currentZoom = 1f
        onDispose { }
    }

    DisposableEffect(Unit) {
        viewModel.bindCamera(lifecycleOwner, previewView)
        onDispose { }
    }

    BoxWithConstraints(
        modifier = modifier.background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        val screenWidth = constraints.maxWidth.toFloat()
        val screenHeight = constraints.maxHeight.toFloat()
        val screenRatio = screenWidth / screenHeight

        // 计算预览区域的实际尺寸
        // 预览是竖屏的，所以实际比例是 3:4（高度 > 宽度）
        val previewRatio = 3f / 4f  // 宽/高

        val (previewWidth, previewHeight) = if (screenRatio > previewRatio) {
            // 屏幕更宽，以高度为准
            val h = screenHeight
            val w = h * previewRatio
            w to h
        } else {
            // 屏幕更高，以宽度为准
            val w = screenWidth
            val h = w / previewRatio
            w to h
        }

        // 预览区域的偏移量
        val previewOffsetX = (screenWidth - previewWidth) / 2
        val previewOffsetY = (screenHeight - previewHeight) / 2

        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            // 相机预览
            AndroidView(
                factory = { previewView },
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectTapGestures { offset ->
                            // 转换触摸坐标到预览坐标
                            val adjustedX = offset.x - previewOffsetX
                            val adjustedY = offset.y - previewOffsetY
                            if (adjustedX in 0f..previewWidth && adjustedY in 0f..previewHeight) {
                                viewModel.focusOnPoint(offset.x, offset.y)
                            }
                        }
                    }
                    .pointerInput(Unit) {
                        detectTransformGestures { _, _, zoom, _ ->
                            val newZoom = currentZoom * zoom
                            val minZoom = viewModel.getMinZoomRatio()
                            val maxZoom = viewModel.getMaxZoomRatio()
                            currentZoom = newZoom.coerceIn(minZoom, maxZoom)
                            viewModel.setZoomRatio(currentZoom)
                        }
                    }
            )

            // 顶部遮罩
            if (previewOffsetY > 0) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.TopCenter)
                        .background(Color.Black.copy(alpha = 0.6f))
                        .let { mod ->
                            with(LocalDensity.current) {
                                mod.size(
                                    width = screenWidth.toDp(),
                                    height = previewOffsetY.toDp()
                                )
                            }
                        }
                )
            }

            // 底部遮罩
            if (previewOffsetY > 0) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .background(Color.Black.copy(alpha = 0.6f))
                        .let { mod ->
                            with(LocalDensity.current) {
                                mod.size(
                                    width = screenWidth.toDp(),
                                    height = previewOffsetY.toDp()
                                )
                            }
                        }
                )
            }

            // 取景框边框
            ViewfinderFrame(
                width = previewWidth,
                height = previewHeight
            )

            // 聚焦框 - 使用 fillMaxSize + 绝对定位，避免居中对齐导致的偏移问题
            focusPoint?.let { point ->
                Box(modifier = Modifier.fillMaxSize()) {
                    FocusIndicator(
                        x = point.x,
                        y = point.y
                    )
                }
            }
        }
    }
}

@Composable
private fun ViewfinderFrame(
    width: Float,
    height: Float
) {
    val density = LocalDensity.current

    Canvas(
        modifier = Modifier
            .let { mod ->
                with(density) {
                    mod.size(width.toDp(), height.toDp())
                }
            }
    ) {
        val strokeWidth = 2.dp.toPx()
        val cornerLength = 24.dp.toPx()
        val color = Color.White.copy(alpha = 0.8f)

        // 左上角
        drawLine(
            color = color,
            start = Offset(0f, cornerLength),
            end = Offset(0f, 0f),
            strokeWidth = strokeWidth
        )
        drawLine(
            color = color,
            start = Offset(0f, 0f),
            end = Offset(cornerLength, 0f),
            strokeWidth = strokeWidth
        )

        // 右上角
        drawLine(
            color = color,
            start = Offset(size.width - cornerLength, 0f),
            end = Offset(size.width, 0f),
            strokeWidth = strokeWidth
        )
        drawLine(
            color = color,
            start = Offset(size.width, 0f),
            end = Offset(size.width, cornerLength),
            strokeWidth = strokeWidth
        )

        // 左下角
        drawLine(
            color = color,
            start = Offset(0f, size.height - cornerLength),
            end = Offset(0f, size.height),
            strokeWidth = strokeWidth
        )
        drawLine(
            color = color,
            start = Offset(0f, size.height),
            end = Offset(cornerLength, size.height),
            strokeWidth = strokeWidth
        )

        // 右下角
        drawLine(
            color = color,
            start = Offset(size.width - cornerLength, size.height),
            end = Offset(size.width, size.height),
            strokeWidth = strokeWidth
        )
        drawLine(
            color = color,
            start = Offset(size.width, size.height - cornerLength),
            end = Offset(size.width, size.height),
            strokeWidth = strokeWidth
        )
    }
}

@Composable
private fun FocusIndicator(
    x: Float,
    y: Float
) {
    val density = LocalDensity.current
    val size = 72.dp
    val sizePx = with(density) { size.toPx() }

    val animatedScale by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(durationMillis = 300),
        label = "focus_scale"
    )

    val offsetX = with(density) { (x - sizePx / 2).toDp() }
    val offsetY = with(density) { (y - sizePx / 2).toDp() }

    Canvas(
        modifier = Modifier
            .offset { IntOffset(offsetX.roundToPx(), offsetY.roundToPx()) }
            .size(size)
    ) {
        val strokeWidth = 2.dp.toPx()
        val cornerLength = 16.dp.toPx()
        val boxSize = this.size.width * animatedScale
        val offset = (this.size.width - boxSize) / 2
        val color = Color.Yellow

        // 左上角
        drawLine(
            color = color,
            start = Offset(offset, offset + cornerLength),
            end = Offset(offset, offset),
            strokeWidth = strokeWidth
        )
        drawLine(
            color = color,
            start = Offset(offset, offset),
            end = Offset(offset + cornerLength, offset),
            strokeWidth = strokeWidth
        )

        // 右上角
        drawLine(
            color = color,
            start = Offset(offset + boxSize - cornerLength, offset),
            end = Offset(offset + boxSize, offset),
            strokeWidth = strokeWidth
        )
        drawLine(
            color = color,
            start = Offset(offset + boxSize, offset),
            end = Offset(offset + boxSize, offset + cornerLength),
            strokeWidth = strokeWidth
        )

        // 左下角
        drawLine(
            color = color,
            start = Offset(offset, offset + boxSize - cornerLength),
            end = Offset(offset, offset + boxSize),
            strokeWidth = strokeWidth
        )
        drawLine(
            color = color,
            start = Offset(offset, offset + boxSize),
            end = Offset(offset + cornerLength, offset + boxSize),
            strokeWidth = strokeWidth
        )

        // 右下角
        drawLine(
            color = color,
            start = Offset(offset + boxSize - cornerLength, offset + boxSize),
            end = Offset(offset + boxSize, offset + boxSize),
            strokeWidth = strokeWidth
        )
        drawLine(
            color = color,
            start = Offset(offset + boxSize, offset + boxSize - cornerLength),
            end = Offset(offset + boxSize, offset + boxSize),
            strokeWidth = strokeWidth
        )
    }
}
