package com.gaoshiqi.otakumap.detail.adapter

import com.gaoshiqi.map.data.LitePoint

/**
 * Created by gaoshiqi
 * on 2025/8/12
 * email: gaoshiqi@bilibili.com
 */
sealed class PointListItem {
    data class Header(val episode: String) : PointListItem()
    data class Point(val litePoint: LitePoint) : PointListItem()
}