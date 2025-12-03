package com.gaoshiqi.image.util

import android.content.ContentValues
import android.content.Context
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import com.bumptech.glide.Glide
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

/**
 * 图片保存工具类
 * 支持将网络图片保存到相册
 */
object ImageSaver {

    private const val ALBUM_NAME = "OtakuMap"

    /**
     * 保存图片到相册
     * @param context Context
     * @param imageUrl 图片URL
     * @return 保存结果，成功返回 Uri，失败返回异常
     */
    suspend fun saveImage(context: Context, imageUrl: String): Result<Uri> {
        return withContext(Dispatchers.IO) {
            try {
                // 1. 使用 Glide 下载图片到缓存
                val file = Glide.with(context)
                    .asFile()
                    .load(imageUrl)
                    .submit()
                    .get()

                // 2. 根据系统版本保存到相册
                val uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    saveWithMediaStore(context, file)
                } else {
                    saveWithLegacy(context, file)
                }

                Result.success(uri)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    /**
     * Android 10+ 使用 MediaStore API 保存
     */
    private fun saveWithMediaStore(context: Context, sourceFile: File): Uri {
        val fileName = generateFileName()
        val contentValues = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
            put(MediaStore.Images.Media.RELATIVE_PATH, "${Environment.DIRECTORY_PICTURES}/$ALBUM_NAME")
            put(MediaStore.Images.Media.IS_PENDING, 1)
        }

        val resolver = context.contentResolver
        val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
            ?: throw Exception("Failed to create MediaStore entry")

        resolver.openOutputStream(uri)?.use { outputStream ->
            FileInputStream(sourceFile).use { inputStream ->
                inputStream.copyTo(outputStream)
            }
        } ?: throw Exception("Failed to open output stream")

        // 标记写入完成
        contentValues.clear()
        contentValues.put(MediaStore.Images.Media.IS_PENDING, 0)
        resolver.update(uri, contentValues, null, null)

        return uri
    }

    /**
     * Android 9及以下使用传统文件写入方式
     */
    private fun saveWithLegacy(context: Context, sourceFile: File): Uri {
        val picturesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
        val albumDir = File(picturesDir, ALBUM_NAME)
        if (!albumDir.exists()) {
            albumDir.mkdirs()
        }

        val fileName = generateFileName()
        val destFile = File(albumDir, fileName)

        FileInputStream(sourceFile).use { inputStream ->
            FileOutputStream(destFile).use { outputStream ->
                inputStream.copyTo(outputStream)
            }
        }

        // 通知媒体库扫描新文件
        MediaScannerConnection.scanFile(
            context,
            arrayOf(destFile.absolutePath),
            arrayOf("image/jpeg"),
            null
        )

        return Uri.fromFile(destFile)
    }

    /**
     * 生成文件名
     */
    private fun generateFileName(): String {
        return "IMG_${System.currentTimeMillis()}.jpg"
    }
}
