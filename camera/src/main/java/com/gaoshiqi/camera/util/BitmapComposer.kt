package com.gaoshiqi.camera.util

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Typeface
import android.text.TextPaint
import android.text.TextUtils
import kotlin.math.abs

/**
 * 拍立得风格合成所需的元数据
 */
data class PolaroidMetadata(
    val pointName: String,      // 圣地名称
    val subjectName: String,    // 番剧名称
    val episode: String?,       // 集数（可选）
    val lat: Double,            // 纬度
    val lng: Double             // 经度
)

/**
 * 图片合成工具
 * 用于将相机拍摄的照片和参考图片进行上下拼接
 */
object BitmapComposer {

    private const val DEFAULT_TARGET_WIDTH = 1080

    // 拍立得风格颜色
    private const val COLOR_POLAROID_BG = 0xFFD4EDDA.toInt()  // 浅绿色背景
    private const val COLOR_INFO_BG = 0xFFFFFFFF.toInt()       // 白色信息栏
    private const val COLOR_TITLE_TEXT = 0xFF212529.toInt()    // 深灰色标题
    private const val COLOR_TAG_BG = 0xFF28A745.toInt()        // 绿色标签背景
    private const val COLOR_TAG_TEXT = 0xFFFFFFFF.toInt()      // 白色标签文字
    private const val COLOR_SUBTITLE_TEXT = 0xFF6C757D.toInt() // 灰色副标题

    // 尺寸常量
    private const val PADDING = 40f
    private const val CORNER_RADIUS = 24f
    private const val IMAGE_GAP = 8f  // 两张图片之间的间隔
    private const val INFO_BAR_HEIGHT = 180f
    private const val COVER_WIDTH = 120f
    private const val COVER_HEIGHT = 160f
    private const val COVER_CORNER_RADIUS = 12f

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

    /**
     * 镜像翻转图片（水平翻转）
     * 用于前置摄像头拍摄的照片
     *
     * @param source 源图片（不会被回收）
     * @return 镜像后的新图片，如果不需要翻转则返回源图片
     */
    fun mirrorBitmap(source: Bitmap): Bitmap {
        val matrix = Matrix().apply {
            preScale(-1f, 1f)
        }
        return Bitmap.createBitmap(
            source,
            0,
            0,
            source.width,
            source.height,
            matrix,
            true
        )
    }

    /**
     * 拍立得风格合成
     * 将相机照片和参考图合成为带有装饰边框和信息栏的精美卡片
     *
     * 布局：
     * ┌─────────────────────────────────────┐  ← 浅绿色背景
     * │  ┌───────────────────────────────┐  │
     * │  │    相机照片 (16:9 圆角)        │  │
     * │  ├───────────────────────────────┤  │
     * │  │    参考图 (16:9 圆角)          │  │
     * │  └───────────────────────────────┘  │
     * │  ┌───────────────────────────────┐  │
     * │  │ ┌─────┐  位置名称              │  │  ← 信息栏（白色背景）
     * │  │ │封面 │  番剧名称              │  │
     * │  │ │缩略 │  EP1 · 35.6°N 139.7°E │  │
     * │  │ └─────┘                        │  │
     * │  └───────────────────────────────┘  │
     * └─────────────────────────────────────┘
     *
     * @param cameraImage 相机拍摄的原图
     * @param referenceImage 参考图片
     * @param coverImage 番剧封面（可选，为 null 则不显示）
     * @param metadata 元数据（位置名称、番剧名称、集数、坐标）
     * @param targetWidth 目标宽度，默认 1080px
     * @param mirrorCamera 是否镜像翻转相机图片（前置摄像头需要）
     * @return 合成后的拍立得风格图片
     */
    fun composePolaroidStyle(
        cameraImage: Bitmap,
        referenceImage: Bitmap,
        coverImage: Bitmap?,
        metadata: PolaroidMetadata,
        targetWidth: Int = DEFAULT_TARGET_WIDTH,
        mirrorCamera: Boolean = false
    ): Bitmap {
        // 计算各区域尺寸
        val imageAreaWidth = targetWidth - PADDING * 2
        val singleImageHeight = (imageAreaWidth * 9f / 16f)
        val totalImageHeight = singleImageHeight * 2 + IMAGE_GAP
        val totalHeight = (PADDING + totalImageHeight + INFO_BAR_HEIGHT + PADDING).toInt()

        // 创建结果画布
        val resultBitmap = Bitmap.createBitmap(
            targetWidth,
            totalHeight,
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(resultBitmap)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)

        // 1. 绘制浅绿色背景
        canvas.drawColor(COLOR_POLAROID_BG)

        // 2. 裁剪并缩放图片
        val scaledCamera = scaleCenterCrop(
            cameraImage,
            imageAreaWidth.toInt(),
            singleImageHeight.toInt(),
            mirrorCamera
        )
        val scaledReference = scaleCenterCrop(
            referenceImage,
            imageAreaWidth.toInt(),
            singleImageHeight.toInt(),
            mirror = false
        )

        // 3. 绘制带圆角的对比图区域
        val imageAreaRect = RectF(
            PADDING,
            PADDING,
            PADDING + imageAreaWidth,
            PADDING + totalImageHeight
        )

        // 保存画布状态
        canvas.save()

        // 创建圆角裁剪路径
        val imagePath = Path().apply {
            addRoundRect(imageAreaRect, CORNER_RADIUS, CORNER_RADIUS, Path.Direction.CW)
        }
        canvas.clipPath(imagePath)

        // 绘制相机图片（上半部分）
        canvas.drawBitmap(scaledCamera, PADDING, PADDING, paint)

        // 绘制参考图片（下半部分）
        canvas.drawBitmap(
            scaledReference,
            PADDING,
            PADDING + singleImageHeight + IMAGE_GAP,
            paint
        )

        // 恢复画布状态（取消裁剪）
        canvas.restore()

        // 回收临时图片
        scaledCamera.recycle()
        scaledReference.recycle()

        // 4. 绘制白色信息栏
        val infoBarTop = PADDING + totalImageHeight
        val infoBarRect = RectF(
            PADDING,
            infoBarTop,
            PADDING + imageAreaWidth,
            infoBarTop + INFO_BAR_HEIGHT
        )
        paint.color = COLOR_INFO_BG
        canvas.drawRoundRect(infoBarRect, CORNER_RADIUS, CORNER_RADIUS, paint)

        // 5. 绘制封面缩略图（如果有）
        val textStartX: Float
        if (coverImage != null) {
            val coverLeft = PADDING + 20f
            val coverTop = infoBarTop + (INFO_BAR_HEIGHT - COVER_HEIGHT) / 2
            val coverRect = RectF(
                coverLeft,
                coverTop,
                coverLeft + COVER_WIDTH,
                coverTop + COVER_HEIGHT
            )

            // 缩放封面
            val scaledCover = scaleCenterCrop(
                coverImage,
                COVER_WIDTH.toInt(),
                COVER_HEIGHT.toInt(),
                mirror = false
            )

            // 绘制带圆角的封面
            canvas.save()
            val coverPath = Path().apply {
                addRoundRect(coverRect, COVER_CORNER_RADIUS, COVER_CORNER_RADIUS, Path.Direction.CW)
            }
            canvas.clipPath(coverPath)
            canvas.drawBitmap(scaledCover, coverLeft, coverTop, paint)
            canvas.restore()

            scaledCover.recycle()
            textStartX = coverLeft + COVER_WIDTH + 20f
        } else {
            textStartX = PADDING + 24f
        }

        // 6. 绘制文字信息
        val textMaxWidth = PADDING + imageAreaWidth - textStartX - 24f

        // 番剧名称（第一行，粗体大字）
        val subjectPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = COLOR_TITLE_TEXT
            textSize = 48f
            typeface = Typeface.DEFAULT_BOLD
        }
        val ellipsizedSubject = TextUtils.ellipsize(
            metadata.subjectName,
            subjectPaint,
            textMaxWidth,
            TextUtils.TruncateAt.END
        ).toString()
        canvas.drawText(
            ellipsizedSubject,
            textStartX,
            infoBarTop + 54f,
            subjectPaint
        )

        // 位置名称（第二行，正常黑字）
        val pointPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = COLOR_TITLE_TEXT
            textSize = 36f
            typeface = Typeface.DEFAULT
        }
        val ellipsizedPoint = TextUtils.ellipsize(
            metadata.pointName,
            pointPaint,
            textMaxWidth,
            TextUtils.TruncateAt.END
        ).toString()
        canvas.drawText(
            ellipsizedPoint,
            textStartX,
            infoBarTop + 100f,
            pointPaint
        )

        // 集数和坐标（第三行，灰色小字）
        val subtitlePaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = COLOR_SUBTITLE_TEXT
            textSize = 28f
        }
        val subtitleText = buildSubtitleText(metadata)
        val ellipsizedSubtitle = TextUtils.ellipsize(
            subtitleText,
            subtitlePaint,
            textMaxWidth,
            TextUtils.TruncateAt.END
        ).toString()
        canvas.drawText(
            ellipsizedSubtitle,
            textStartX,
            infoBarTop + INFO_BAR_HEIGHT - 28f,
            subtitlePaint
        )

        return resultBitmap
    }

    /**
     * 构建副标题文本（集数 + 坐标）
     */
    private fun buildSubtitleText(metadata: PolaroidMetadata): String {
        val parts = mutableListOf<String>()

        // 集数
        if (!metadata.episode.isNullOrBlank()) {
            parts.add("EP${metadata.episode}")
        }

        // 坐标
        if (metadata.lat != 0.0 || metadata.lng != 0.0) {
            val latDir = if (metadata.lat >= 0) "N" else "S"
            val lngDir = if (metadata.lng >= 0) "E" else "W"
            val coordText = "%.2f°%s %.2f°%s".format(
                abs(metadata.lat), latDir,
                abs(metadata.lng), lngDir
            )
            parts.add(coordText)
        }

        return parts.joinToString(" · ")
    }
}
