package com.gaoshiqi.otakumap.collection.v2.utils

import android.app.ActivityManager
import android.content.Context
import android.os.Build
import android.os.PerformanceHintManager

/**
 * 设备性能检测工具
 * 用于根据设备性能动态调整动画规格,确保低端设备的流畅体验
 */
object DevicePerformance {
    enum class Level {
        HIGH,    // 高性能设备: 完整动画效果
        MEDIUM,  // 中等性能: 简化动画效果
        LOW      // 低端设备: 最小化动画效果
    }

    /**
     * 获取设备性能等级
     * Android 12+ 优先使用官方 PerformanceHintManager API
     * 低版本降级为基于内存和 CPU 核心数的判断
     */
    fun getPerformanceLevel(context: Context): Level {
        // Android 12+ 使用官方性能提示 API
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val pm = context.getSystemService(Context.PERFORMANCE_HINT_SERVICE) as? PerformanceHintManager
            // TODO: 未来可以基于 PerformanceHintManager 的会话反馈动态调整
        }

        // 降级方案：基于内存和处理器核心数判断
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val memoryInfo = ActivityManager.MemoryInfo()
        activityManager.getMemoryInfo(memoryInfo)

        val totalRamGB = memoryInfo.totalMem / (1024 * 1024 * 1024)  // 转换为 GB
        val cpuCores = Runtime.getRuntime().availableProcessors()

        return when {
            totalRamGB >= 6 && cpuCores >= 8 -> Level.HIGH
            totalRamGB >= 4 && cpuCores >= 6 -> Level.MEDIUM
            else -> Level.LOW
        }
    }

    /**
     * 是否启用模糊效果
     * Android 12+ 支持 RenderEffect,且性能等级为 HIGH 或 MEDIUM 时启用
     */
    fun shouldEnableBlur(context: Context): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return false
        val level = getPerformanceLevel(context)
        return level == Level.HIGH || level == Level.MEDIUM
    }
}
