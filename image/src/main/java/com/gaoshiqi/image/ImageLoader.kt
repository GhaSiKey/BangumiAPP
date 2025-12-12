package com.gaoshiqi.image

import android.graphics.drawable.Drawable
import android.widget.ImageView
import androidx.annotation.DrawableRes
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target

/**
 * 统一图片加载工具
 * 封装 Glide，提供统一的缓存策略和配置
 */
object ImageLoader {

    /**
     * 加载列表封面图（带内存缓存，centerCrop）
     */
    fun loadCover(
        imageView: ImageView,
        url: String?,
        @DrawableRes placeholder: Int
    ) {
        Glide.with(imageView.context)
            .load(url)
            .apply(coverOptions(placeholder))
            .transition(DrawableTransitionOptions.withCrossFade(150))
            .into(imageView)
    }

    /**
     * 加载头像（带内存缓存，圆形裁剪）
     */
    fun loadAvatar(
        imageView: ImageView,
        url: String?,
        @DrawableRes placeholder: Int
    ) {
        Glide.with(imageView.context)
            .load(url)
            .apply(avatarOptions(placeholder))
            .transition(DrawableTransitionOptions.withCrossFade(150))
            .into(imageView)
    }

    /**
     * 加载详情大图（带内存缓存，centerCrop）
     */
    fun loadDetail(
        imageView: ImageView,
        url: String?,
        @DrawableRes placeholder: Int
    ) {
        Glide.with(imageView.context)
            .load(url)
            .apply(detailOptions(placeholder))
            .transition(DrawableTransitionOptions.withCrossFade(200))
            .into(imageView)
    }

    /**
     * 加载全屏查看图片（不使用内存缓存，避免大图占用内存）
     */
    fun loadFullScreen(
        imageView: ImageView,
        url: String?,
        onSuccess: (() -> Unit)? = null,
        onError: (() -> Unit)? = null
    ) {
        Glide.with(imageView.context)
            .load(url)
            .apply(fullScreenOptions())
            .listener(object : RequestListener<Drawable> {
                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any?,
                    target: Target<Drawable>,
                    isFirstResource: Boolean
                ): Boolean {
                    onError?.invoke()
                    return false
                }

                override fun onResourceReady(
                    resource: Drawable,
                    model: Any,
                    target: Target<Drawable>?,
                    dataSource: DataSource,
                    isFirstResource: Boolean
                ): Boolean {
                    onSuccess?.invoke()
                    return false
                }
            })
            .into(imageView)
    }

    /**
     * 加载自适应高度的图片（保持原始宽高比）
     */
    fun loadWithOriginalRatio(
        imageView: ImageView,
        url: String?,
        @DrawableRes placeholder: Int,
        width: Int
    ) {
        Glide.with(imageView.context)
            .load(url)
            .apply(
                RequestOptions()
                    .placeholder(placeholder)
                    .override(width, Target.SIZE_ORIGINAL)
                    .diskCacheStrategy(DiskCacheStrategy.DATA)
                    .skipMemoryCache(false)
            )
            .transition(DrawableTransitionOptions.withCrossFade(150))
            .into(imageView)
    }

    /**
     * 加载封面图并在加载完成后回调（用于 Google Maps InfoWindow 等需要刷新的场景）
     */
    fun loadCoverWithCallback(
        imageView: ImageView,
        url: String?,
        @DrawableRes placeholder: Int,
        onSuccess: (() -> Unit)? = null,
        onError: (() -> Unit)? = null
    ) {
        Glide.with(imageView.context)
            .load(url)
            .apply(coverOptions(placeholder))
            .listener(object : RequestListener<Drawable> {
                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any?,
                    target: Target<Drawable>,
                    isFirstResource: Boolean
                ): Boolean {
                    onError?.invoke()
                    return false
                }

                override fun onResourceReady(
                    resource: Drawable,
                    model: Any,
                    target: Target<Drawable>?,
                    dataSource: DataSource,
                    isFirstResource: Boolean
                ): Boolean {
                    onSuccess?.invoke()
                    return false
                }
            })
            .into(imageView)
    }

    // ============ RequestOptions 配置 ============

    private fun coverOptions(@DrawableRes placeholder: Int) = RequestOptions()
        .placeholder(placeholder)
        .centerCrop()
        .diskCacheStrategy(DiskCacheStrategy.DATA)
        .skipMemoryCache(false)

    private fun avatarOptions(@DrawableRes placeholder: Int) = RequestOptions()
        .placeholder(placeholder)
        .circleCrop()
        .diskCacheStrategy(DiskCacheStrategy.DATA)
        .skipMemoryCache(false)

    private fun detailOptions(@DrawableRes placeholder: Int) = RequestOptions()
        .placeholder(placeholder)
        .centerCrop()
        .diskCacheStrategy(DiskCacheStrategy.DATA)
        .skipMemoryCache(false)

    private fun fullScreenOptions() = RequestOptions()
        .diskCacheStrategy(DiskCacheStrategy.DATA)
        .skipMemoryCache(true) // 大图不占用内存缓存
}
