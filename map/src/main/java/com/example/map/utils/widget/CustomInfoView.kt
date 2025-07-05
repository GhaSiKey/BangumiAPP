package com.example.map.utils.widget

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import com.bumptech.glide.Glide
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
            .placeholder(com.example.map.R.drawable.ic_cover_placeholder_36)
            .into(mBinding.cover)
    }
}