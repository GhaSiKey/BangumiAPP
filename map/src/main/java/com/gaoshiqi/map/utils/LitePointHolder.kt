package com.gaoshiqi.map.utils

import com.gaoshiqi.map.data.LitePoint

/**
 * 临时持有 LitePoint，用于 Fragment 间传递
 * 使用后自动清除，避免内存泄漏
 */
object LitePointHolder {
    private var point: LitePoint? = null

    fun set(litePoint: LitePoint) {
        point = litePoint
    }

    fun get(): LitePoint? {
        return point
    }

    fun getAndClear(): LitePoint? {
        val result = point
        point = null
        return result
    }

    fun clear() {
        point = null
    }
}
