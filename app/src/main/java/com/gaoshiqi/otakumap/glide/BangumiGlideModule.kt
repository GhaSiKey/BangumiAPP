package com.gaoshiqi.otakumap.glide

import android.content.Context
import com.bumptech.glide.GlideBuilder
import com.bumptech.glide.annotation.GlideModule
import com.bumptech.glide.load.DecodeFormat
import com.bumptech.glide.load.engine.cache.InternalCacheDiskCacheFactory
import com.bumptech.glide.load.engine.cache.LruResourceCache
import com.bumptech.glide.module.AppGlideModule
import com.bumptech.glide.request.RequestOptions

@GlideModule
class BangumiGlideModule : AppGlideModule() {

    override fun applyOptions(context: Context, builder: GlideBuilder) {
        // 磁盘缓存：250MB
        val diskCacheSizeBytes = 250L * 1024 * 1024
        builder.setDiskCache(
            InternalCacheDiskCacheFactory(context, DISK_CACHE_DIR, diskCacheSizeBytes)
        )

        // 内存缓存：使用可用内存的 1/6（比默认 1/8 略大）
        val maxMemory = Runtime.getRuntime().maxMemory()
        val memorySize = maxMemory / 6
        builder.setMemoryCache(LruResourceCache(memorySize))

        // 默认解码格式：ARGB_8888（更高质量）
        builder.setDefaultRequestOptions(
            RequestOptions().format(DecodeFormat.PREFER_ARGB_8888)
        )
    }

    // 禁用清单解析，提升初始化速度
    override fun isManifestParsingEnabled(): Boolean = false

    companion object {
        private const val DISK_CACHE_DIR = "glide_cache"
    }
}
