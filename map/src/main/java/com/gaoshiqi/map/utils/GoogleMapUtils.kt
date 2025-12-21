package com.gaoshiqi.map.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import com.gaoshiqi.map.R

/**
 * Google Maps 跳转工具类
 */
object GoogleMapUtils {

    private const val GOOGLE_MAPS_PACKAGE = "com.google.android.apps.maps"

    /**
     * 跳转到 Google Maps 定位指定坐标
     * @param context Context
     * @param lat 纬度
     * @param lng 经度
     * @param label 地点名称（可选，用于标记显示）
     */
    fun openInGoogleMaps(context: Context, lat: Double, lng: Double, label: String? = null) {
        // 构建 URI: geo:0,0?q=lat,lng(label)
        val uriString = if (!label.isNullOrBlank()) {
            "geo:0,0?q=$lat,$lng(${Uri.encode(label)})"
        } else {
            "geo:0,0?q=$lat,$lng"
        }

        val gmmIntentUri = Uri.parse(uriString)
        val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri).apply {
            setPackage(GOOGLE_MAPS_PACKAGE)
        }

        // 检查是否安装了 Google Maps
        if (mapIntent.resolveActivity(context.packageManager) != null) {
            context.startActivity(mapIntent)
        } else {
            // 降级方案：使用浏览器打开
            openInBrowser(context, lat, lng, label)
        }
    }

    /**
     * 使用浏览器打开 Google Maps（降级方案）
     */
    private fun openInBrowser(context: Context, lat: Double, lng: Double, label: String?) {
        val url = if (!label.isNullOrBlank()) {
            "https://www.google.com/maps/search/?api=1&query=$lat,$lng&query_place_id=${Uri.encode(label)}"
        } else {
            "https://www.google.com/maps/search/?api=1&query=$lat,$lng"
        }

        try {
            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            context.startActivity(browserIntent)
        } catch (e: Exception) {
            Toast.makeText(context, R.string.open_maps_failed, Toast.LENGTH_SHORT).show()
        }
    }
}
