package com.example.map.widget

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.example.map.R
import com.example.map.databinding.CustomInfoViewBinding
import com.google.android.gms.maps.model.Marker

class CustomInfoView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
): LinearLayout(context, attrs) {

    private val mBinding = CustomInfoViewBinding.inflate(LayoutInflater.from(context), this, true)

    fun setMarker(marker: Marker) {
        mBinding.pointName.text = marker.title
        mBinding.tvTitle.text = marker.snippet

        // Google Maps InfoWindow是静态快照，异步加载图片时需要手动刷新InfoWindow，否则第一次看不到图片。
        Glide.with(context)
            .load("https://anitabi.cn/images/points/276/50cjwj3ie_1741166630984.jpg?plan=h360")
            .placeholder(R.drawable.ic_cover_placeholder_36)
            .listener(object : RequestListener<Drawable> {
                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any?,
                    target: Target<Drawable?>,
                    isFirstResource: Boolean
                ): Boolean {
                    return false
                }

                override fun onResourceReady(
                    resource: Drawable,
                    model: Any,
                    target: Target<Drawable?>?,
                    dataSource: DataSource,
                    isFirstResource: Boolean
                ): Boolean {
                    if (marker.isInfoWindowShown) {
                        marker.hideInfoWindow()
                        marker.showInfoWindow()
                    }
                    return false
                }

            })
            .into(mBinding.cover)
    }
}