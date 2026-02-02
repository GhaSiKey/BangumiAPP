package com.gaoshiqi.camera.util

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Rect

/**
 * 图片合成工具
 * 用于将相机拍摄的照片和参考图片进行上下拼接
 */
object BitmapComposer {

    private const val DEFAULT_TARGET_WIDTH = 1080

    /**
     * 垂直拼接两张图片
     * 上半部分：相机拍摄的照片（裁剪为 16:9）
     * 下半部分：参考图片（裁剪为 16:9）
     *
     * @param cameraImage 相机拍摄的原图
     * @param referenceImage 参考图片
     * @param targetWidth 目标宽度，默认 1080px
     * @param mirrorCamera 是否镜像翻转相机图片（前置摄像头需要）
     * @return 合成后的图片
     */
    fun composeVertically(
        cameraImage: Bitmap,
        referenceImage: Bitmap,
        targetWidth: Int = DEFAULT_TARGET_WIDTH,
        mirrorCamera: Boolean = false
    ): Bitmap {
        // 16:9 比例的高度
        val targetHeight = (targetWidth * 9f / 16f).toInt()

        // 裁剪并缩放为 16:9
        val scaledCamera = scaleCenterCrop(cameraImage, targetWidth, targetHeight, mirrorCamera)
        val scaledReference = scaleCenterCrop(referenceImage, targetWidth, targetHeight, mirror = false)

        // 创建合成画布（高度为两张图片之和）
        val composedBitmap = Bitmap.createBitmap(
            targetWidth,
            targetHeight * 2,
            Bitmap.Config.ARGB_8888
        )

        val canvas = Canvas(composedBitmap)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)

        // 绘制相机图片在上半部分
        canvas.drawBitmap(scaledCamera, 0f, 0f, paint)

        // 绘制参考图片在下半部分
        canvas.drawBitmap(scaledReference, 0f, targetHeight.toFloat(), paint)

        // 回收临时 Bitmap
        scaledCamera.recycle()
        scaledReference.recycle()

        return composedBitmap
    }

    /**
     * 中心裁剪并缩放图片到指定尺寸
     * 注意：此方法不会修改或回收源 Bitmap
     *
     * @param source 源图片（不会被回收）
     * @param targetWidth 目标宽度
     * @param targetHeight 目标高度
     * @param mirror 是否镜像翻转
     * @return 处理后的新图片
     */
    private fun scaleCenterCrop(
        source: Bitmap,
        targetWidth: Int,
        targetHeight: Int,
        mirror: Boolean = false
    ): Bitmap {
        val sourceWidth = source.width
        val sourceHeight = source.height
        val sourceRatio = sourceWidth.toFloat() / sourceHeight
        val targetRatio = targetWidth.toFloat() / targetHeight

        // 计算裁剪区域
        val cropRect: Rect = if (sourceRatio > targetRatio) {
            // 源图更宽，需要裁剪左右
            val cropWidth = (sourceHeight * targetRatio).toInt()
            val left = (sourceWidth - cropWidth) / 2
            Rect(left, 0, left + cropWidth, sourceHeight)
        } else {
            // 源图更高，需要裁剪上下
            val cropHeight = (sourceWidth / targetRatio).toInt()
            val top = (sourceHeight - cropHeight) / 2
            Rect(0, top, sourceWidth, top + cropHeight)
        }

        // 裁剪（可能返回 source 本身，如果区域相同）
        val croppedBitmap = Bitmap.createBitmap(
            source,
            cropRect.left,
            cropRect.top,
            cropRect.width(),
            cropRect.height()
        )
        val needRecycleCropped = croppedBitmap !== source

        // 缩放到目标尺寸（可能返回 croppedBitmap 本身）
        val scaledBitmap = Bitmap.createScaledBitmap(
            croppedBitmap,
            targetWidth,
            targetHeight,
            true
        )
        val needRecycleScaled = scaledBitmap !== croppedBitmap && scaledBitmap !== source

        // 回收 croppedBitmap（如果是新创建的且不等于 scaledBitmap）
        if (needRecycleCropped && croppedBitmap !== scaledBitmap) {
            croppedBitmap.recycle()
        }

        // 如果需要镜像翻转
        val resultBitmap = if (mirror) {
            val matrix = Matrix().apply {
                preScale(-1f, 1f) // 水平翻转
            }
            val mirroredBitmap = Bitmap.createBitmap(
                scaledBitmap,
                0,
                0,
                scaledBitmap.width,
                scaledBitmap.height,
                matrix,
                true
            )
            // 回收 scaledBitmap（如果是新创建的）
            if (needRecycleScaled) {
                scaledBitmap.recycle()
            }
            mirroredBitmap
        } else {
            scaledBitmap
        }

        return resultBitmap
    }
}
