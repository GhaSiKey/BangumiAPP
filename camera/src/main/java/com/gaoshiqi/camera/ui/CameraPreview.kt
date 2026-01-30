package com.gaoshiqi.camera.ui

import android.view.ViewGroup
import androidx.camera.view.PreviewView
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
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
            scaleType = PreviewView.ScaleType.FILL_CENTER
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

    Box(modifier = modifier) {
        AndroidView(
            factory = { previewView },
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTapGestures { offset ->
                        viewModel.focusOnPoint(offset.x, offset.y)
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

        // 聚焦框
        focusPoint?.let { point ->
            FocusIndicator(
                x = point.x,
                y = point.y
            )
        }
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

    // 动画效果：从大到小
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

        // 绘制四个角
        val color = Color.White

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
