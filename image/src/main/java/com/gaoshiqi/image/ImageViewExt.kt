package com.gaoshiqi.image

import android.widget.ImageView
import androidx.annotation.DrawableRes

/**
 * ImageView 扩展函数，简化图片加载调用
 */

fun ImageView.loadCover(url: String?, @DrawableRes placeholder: Int) {
    ImageLoader.loadCover(this, url, placeholder)
}

fun ImageView.loadAvatar(url: String?, @DrawableRes placeholder: Int) {
    ImageLoader.loadAvatar(this, url, placeholder)
}

fun ImageView.loadDetail(url: String?, @DrawableRes placeholder: Int) {
    ImageLoader.loadDetail(this, url, placeholder)
}

fun ImageView.loadWithOriginalRatio(url: String?, @DrawableRes placeholder: Int, width: Int) {
    ImageLoader.loadWithOriginalRatio(this, url, placeholder, width)
}
