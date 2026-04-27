package com.gaoshiqi.map.widget

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import com.gaoshiqi.image.ImageLoader
import com.gaoshiqi.map.R
import com.gaoshiqi.map.databinding.CustomInfoViewBinding
import com.google.android.gms.maps.model.Marker

class CustomInfoView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
): LinearLayout(context, attrs) {

    private val mBinding = CustomInfoViewBinding.inflate(LayoutInflater.from(context), this, true)
    private val mainHandler = Handler(Looper.getMainLooper())
    private var lastImageUrl: String? = null
    private var hasRefreshed = false

    fun setMarker(marker: Marker, imageUrl: String?) {
        mBinding.pointName.text = marker.title
        mBinding.tvTitle.text = marker.snippet

        // 如果是同一个 URL 且已经刷新过，不再重复刷新
        if (imageUrl == lastImageUrl && hasRefreshed) {
            return
        }

        lastImageUrl = imageUrl
        hasRefreshed = false

        // Google Maps InfoWindow是静态快照，异步加载图片时需要手动刷新InfoWindow，否则第一次看不到图片。
        ImageLoader.loadCoverWithCallback(
            imageView = mBinding.cover,
            url = imageUrl,
            placeholder = R.drawable.placeholder_landscape,
            onSuccess = {
                // 使用 Handler.post 延迟到下一帧刷新，避免在 Glide 回调中触发新的加载导致崩溃
                // 只刷新一次，避免无限循环
                if (!hasRefreshed) {
                    hasRefreshed = true
                    mainHandler.post {
                        // 检查 Activity 是否还存活
                        if (isActivityAlive() && marker.isInfoWindowShown) {
                            marker.hideInfoWindow()
                            marker.showInfoWindow()
                        }
                    }
                }
            }
        )
    }

    private fun isActivityAlive(): Boolean {
        var context = context
        while (context is ContextWrapper) {
            if (context is Activity) {
                return !context.isDestroyed && !context.isFinishing
            }
            context = context.baseContext
        }
        return false
    }
}
