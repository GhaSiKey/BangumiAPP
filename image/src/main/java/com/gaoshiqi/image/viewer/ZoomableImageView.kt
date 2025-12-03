package com.gaoshiqi.image.viewer

import android.content.Context
import android.graphics.Matrix
import android.graphics.PointF
import android.graphics.RectF
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import androidx.appcompat.widget.AppCompatImageView

/**
 * 支持双指缩放、双击缩放、拖拽平移的 ImageView
 */
class ZoomableImageView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AppCompatImageView(context, attrs, defStyleAttr) {

    companion object {
        private const val MAX_SCALE = 4.0f
        private const val DOUBLE_TAP_SCALE = 2.5f
    }

    private val displayMatrix = Matrix()
    private val matrixValues = FloatArray(9)

    // 基础缩放比例（fit center 时的缩放）
    private var baseScale = 1.0f
    // 最小缩放 = 基础缩放
    private var minScale = 1.0f

    // 是否已初始化
    private var isInitialized = false

    // 手势检测器
    private val scaleGestureDetector: ScaleGestureDetector
    private val gestureDetector: GestureDetector

    // 拖拽相关
    private val lastTouchPoint = PointF()
    private var activePointerId = MotionEvent.INVALID_POINTER_ID

    // 长按回调
    private var onLongClickListener: OnLongClickListener? = null

    init {
        scaleType = ScaleType.MATRIX
        scaleGestureDetector = ScaleGestureDetector(context, ScaleGestureListener())
        gestureDetector = GestureDetector(context, GestureListener())
    }

    override fun setImageDrawable(drawable: Drawable?) {
        super.setImageDrawable(drawable)
        isInitialized = false
        if (width > 0 && height > 0 && drawable != null) {
            post { initializeMatrix() }
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        if (w > 0 && h > 0 && drawable != null) {
            initializeMatrix()
        }
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        if (!isInitialized && width > 0 && height > 0 && drawable != null) {
            initializeMatrix()
        }
    }

    private fun initializeMatrix() {
        val d = drawable ?: return
        val viewWidth = width.toFloat()
        val viewHeight = height.toFloat()
        val drawableWidth = d.intrinsicWidth.toFloat()
        val drawableHeight = d.intrinsicHeight.toFloat()

        if (viewWidth <= 0 || viewHeight <= 0 || drawableWidth <= 0 || drawableHeight <= 0) {
            return
        }

        // 计算 fit center 的缩放比例
        val scaleX = viewWidth / drawableWidth
        val scaleY = viewHeight / drawableHeight
        baseScale = minOf(scaleX, scaleY)
        minScale = baseScale

        // 计算居中偏移
        val scaledWidth = drawableWidth * baseScale
        val scaledHeight = drawableHeight * baseScale
        val translateX = (viewWidth - scaledWidth) / 2f
        val translateY = (viewHeight - scaledHeight) / 2f

        // 设置矩阵
        displayMatrix.reset()
        displayMatrix.postScale(baseScale, baseScale)
        displayMatrix.postTranslate(translateX, translateY)

        imageMatrix = displayMatrix
        isInitialized = true
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        scaleGestureDetector.onTouchEvent(event)
        gestureDetector.onTouchEvent(event)

        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                activePointerId = event.getPointerId(0)
                lastTouchPoint.set(event.x, event.y)
                parent?.requestDisallowInterceptTouchEvent(true)
            }

            MotionEvent.ACTION_MOVE -> {
                if (!scaleGestureDetector.isInProgress && activePointerId != MotionEvent.INVALID_POINTER_ID) {
                    val pointerIndex = event.findPointerIndex(activePointerId)
                    if (pointerIndex >= 0) {
                        val x = event.getX(pointerIndex)
                        val y = event.getY(pointerIndex)
                        val dx = x - lastTouchPoint.x
                        val dy = y - lastTouchPoint.y

                        // 只有放大后才能拖动
                        if (getCurrentScale() > baseScale + 0.01f) {
                            translateImage(dx, dy)
                        }

                        lastTouchPoint.set(x, y)
                    }
                }
            }

            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                activePointerId = MotionEvent.INVALID_POINTER_ID
                parent?.requestDisallowInterceptTouchEvent(false)
            }

            MotionEvent.ACTION_POINTER_UP -> {
                val pointerIndex = event.actionIndex
                val pointerId = event.getPointerId(pointerIndex)
                if (pointerId == activePointerId) {
                    val newPointerIndex = if (pointerIndex == 0) 1 else 0
                    if (newPointerIndex < event.pointerCount) {
                        lastTouchPoint.set(event.getX(newPointerIndex), event.getY(newPointerIndex))
                        activePointerId = event.getPointerId(newPointerIndex)
                    }
                }
            }
        }

        return true
    }

    private fun getCurrentScale(): Float {
        displayMatrix.getValues(matrixValues)
        return matrixValues[Matrix.MSCALE_X]
    }

    private fun translateImage(dx: Float, dy: Float) {
        displayMatrix.postTranslate(dx, dy)
        constrainMatrix()
        imageMatrix = displayMatrix
    }

    private fun scaleImage(scaleFactor: Float, focusX: Float, focusY: Float) {
        val currentScale = getCurrentScale()
        var newScale = currentScale * scaleFactor

        // 限制缩放范围
        when {
            newScale < minScale -> newScale = minScale
            newScale > MAX_SCALE -> newScale = MAX_SCALE
        }

        val adjustedFactor = newScale / currentScale
        displayMatrix.postScale(adjustedFactor, adjustedFactor, focusX, focusY)
        constrainMatrix()
        imageMatrix = displayMatrix
    }

    private fun constrainMatrix() {
        val d = drawable ?: return

        val imageRect = RectF(0f, 0f, d.intrinsicWidth.toFloat(), d.intrinsicHeight.toFloat())
        displayMatrix.mapRect(imageRect)

        val viewWidth = width.toFloat()
        val viewHeight = height.toFloat()

        var dx = 0f
        var dy = 0f

        // 水平方向约束
        when {
            imageRect.width() <= viewWidth -> {
                // 图片宽度小于视图，居中
                dx = (viewWidth - imageRect.width()) / 2f - imageRect.left
            }
            imageRect.left > 0 -> {
                // 左边有空隙，左移
                dx = -imageRect.left
            }
            imageRect.right < viewWidth -> {
                // 右边有空隙，右移
                dx = viewWidth - imageRect.right
            }
        }

        // 垂直方向约束
        when {
            imageRect.height() <= viewHeight -> {
                // 图片高度小于视图，居中
                dy = (viewHeight - imageRect.height()) / 2f - imageRect.top
            }
            imageRect.top > 0 -> {
                // 上边有空隙，上移
                dy = -imageRect.top
            }
            imageRect.bottom < viewHeight -> {
                // 下边有空隙，下移
                dy = viewHeight - imageRect.bottom
            }
        }

        if (dx != 0f || dy != 0f) {
            displayMatrix.postTranslate(dx, dy)
        }
    }

    private fun resetToOriginal() {
        initializeMatrix()
    }

    private fun zoomToScale(targetScale: Float, focusX: Float, focusY: Float) {
        val currentScale = getCurrentScale()
        val scaleFactor = targetScale / currentScale
        displayMatrix.postScale(scaleFactor, scaleFactor, focusX, focusY)
        constrainMatrix()
        imageMatrix = displayMatrix
    }

    private inner class ScaleGestureListener : ScaleGestureDetector.SimpleOnScaleGestureListener() {
        override fun onScale(detector: ScaleGestureDetector): Boolean {
            scaleImage(detector.scaleFactor, detector.focusX, detector.focusY)
            return true
        }
    }

    private inner class GestureListener : GestureDetector.SimpleOnGestureListener() {
        override fun onDoubleTap(e: MotionEvent): Boolean {
            val currentScale = getCurrentScale()
            if (currentScale > baseScale + 0.01f) {
                // 已放大，恢复原始大小
                resetToOriginal()
            } else {
                // 未放大，放大到指定倍数
                zoomToScale(baseScale * DOUBLE_TAP_SCALE, e.x, e.y)
            }
            return true
        }

        override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
            performClick()
            return true
        }

        override fun onLongPress(e: MotionEvent) {
            onLongClickListener?.onLongClick(this@ZoomableImageView)
        }
    }

    override fun setOnLongClickListener(listener: OnLongClickListener?) {
        onLongClickListener = listener
    }

    override fun performClick(): Boolean {
        return super.performClick()
    }
}
