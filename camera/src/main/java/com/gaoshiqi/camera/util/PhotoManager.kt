package com.gaoshiqi.camera.util

import android.content.Context
import com.gaoshiqi.camera.viewmodel.PhotoItem
import java.io.File
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
}
