package com.gaoshiqi.camera.util

import android.content.ContentValues
import android.content.Context
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import com.gaoshiqi.camera.viewmodel.PhotoItem
import java.io.File
import java.io.FileInputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * 照片管理器
 * 负责照片的存储、读取和删除
 */
class PhotoManager(private val context: Context) {

    companion object {
        private const val PHOTO_DIR = "camera_photos"
        private const val PHOTO_PREFIX = "IMG_"
        private const val PHOTO_EXTENSION = ".jpg"
        private const val DATE_FORMAT = "yyyyMMdd_HHmmss"
    }

    private val photoDir: File by lazy {
        File(context.filesDir, PHOTO_DIR).apply {
            if (!exists()) {
                mkdirs()
            }
        }
    }

    /**
     * 创建新照片文件
     * @param suffix 可选的文件名后缀，如 "_original" 或 "_comparison"
     */
    fun createPhotoFile(suffix: String = ""): File {
        val timestamp = SimpleDateFormat(DATE_FORMAT, Locale.getDefault()).format(Date())
        val fileName = "$PHOTO_PREFIX$timestamp$suffix$PHOTO_EXTENSION"
        return File(photoDir, fileName)
    }

    /**
     * 获取所有照片，按时间倒序排列
     */
    fun getAllPhotos(): List<PhotoItem> {
        val files = photoDir.listFiles { file ->
            file.isFile && file.name.endsWith(PHOTO_EXTENSION)
        } ?: emptyArray()

        return files
            .sortedByDescending { it.lastModified() }
            .map { file ->
                PhotoItem(
                    uri = file.absolutePath,
                    timestamp = file.lastModified(),
                    fileName = file.name
                )
            }
    }

    /**
     * 获取最新的照片
     */
    fun getLatestPhoto(): PhotoItem? {
        return getAllPhotos().firstOrNull()
    }

    /**
     * 删除照片
     */
    fun deletePhoto(uri: String): Boolean {
        val file = File(uri)
        return if (file.exists() && file.parentFile?.absolutePath == photoDir.absolutePath) {
            file.delete()
        } else {
            false
        }
    }

    /**
     * 获取照片数量
     */
    fun getPhotoCount(): Int {
        return photoDir.listFiles { file ->
            file.isFile && file.name.endsWith(PHOTO_EXTENSION)
        }?.size ?: 0
    }

    /**
     * 清空所有照片
     */
    fun clearAllPhotos(): Boolean {
        val files = photoDir.listFiles() ?: return true
        return files.all { it.delete() }
    }

    /**
     * 保存照片到系统相册
     *
     * @param photoPath 照片文件路径
     * @return 保存结果，成功返回 Result.success(uri)，失败返回 Result.failure(exception)
     */
    fun saveToSystemGallery(photoPath: String): Result<String> {
        val sourceFile = File(photoPath)
        if (!sourceFile.exists()) {
            return Result.failure(IllegalArgumentException("Photo file not found: $photoPath"))
        }

        return try {
            val fileName = sourceFile.name
            val mimeType = "image/jpeg"

            val contentValues = ContentValues().apply {
                put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
                put(MediaStore.Images.Media.MIME_TYPE, mimeType)
                put(MediaStore.Images.Media.DATE_ADDED, System.currentTimeMillis() / 1000)
                put(MediaStore.Images.Media.DATE_MODIFIED, System.currentTimeMillis() / 1000)

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    // Android 10+ 使用 Scoped Storage
                    put(MediaStore.Images.Media.RELATIVE_PATH, "${Environment.DIRECTORY_PICTURES}/OtakuMap")
                    put(MediaStore.Images.Media.IS_PENDING, 1)
                }
            }

            val resolver = context.contentResolver
            val collection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
            } else {
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            }

            val insertedUri = resolver.insert(collection, contentValues)
                ?: return Result.failure(IllegalStateException("Failed to create MediaStore entry"))

            // 复制文件内容
            resolver.openOutputStream(insertedUri)?.use { outputStream ->
                FileInputStream(sourceFile).use { inputStream ->
                    inputStream.copyTo(outputStream)
                }
            } ?: return Result.failure(IllegalStateException("Failed to open output stream"))

            // Android 10+ 标记为非挂起状态
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                contentValues.clear()
                contentValues.put(MediaStore.Images.Media.IS_PENDING, 0)
                resolver.update(insertedUri, contentValues, null, null)
            }

            Result.success(insertedUri.toString())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
