package com.gaoshiqi.map.utils

import com.gaoshiqi.map.data.LitePoint

/**
 * Created by gaoshiqi
 * on 2025/7/10 21:30
 * email: gaoshiqi@bilibili.com
 */
object PointListSingleton {

    private var pointList: List<LitePoint>? = null

    fun setPointList(list: List<LitePoint>) {
        pointList = list
    }

    fun getPointList(): List<LitePoint>? {
        return pointList
    }

    fun clear() {
        pointList = null
    }
}