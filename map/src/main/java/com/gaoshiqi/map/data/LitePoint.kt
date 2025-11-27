package com.gaoshiqi.map.data

/**
 * Created by gaoshiqi
 * on 2025/6/22 20:42
 * email: gaoshiqi@bilibili.com
 */
// 轻量版地标信息数据类
data class LitePoint(
    val id: String,                // 地标 ID
    val cn: String?,               // 地标中文译名（可为空）
    val name: String,              // 地标原名
    val image: String,             // 地标缩略图 URL
    val ep: String?,               // 出现集数（可为空）
    val s: String?,                // 截图时间点（秒，可为空）
    val geo: List<Double>,         // GPS 位置信息
    // 关联番剧信息（在 ViewModel 请求接口后绑定）
    var subjectId: Int = 0,
    var subjectName: String = "",
    var subjectCover: String = ""
) {
    /** 获取展示用的地点名称，优先使用中文名 */
    fun displayName(): String = cn?.takeIf { it.isNotEmpty() } ?: name

    /** 获取纬度 */
    fun lat(): Double = geo.getOrElse(0) { 0.0 }

    /** 获取经度 */
    fun lng(): Double = geo.getOrElse(1) { 0.0 }

    /** 格式化出现时间，如 "EP1 00:30" */
    fun formatEpisodeTime(): String {
        val epStr = ep?.let { "EP$it" } ?: ""
        val timeStr = s?.toIntOrNull()?.let { seconds ->
            val min = seconds / 60
            val sec = seconds % 60
            String.format("%02d:%02d", min, sec)
        } ?: ""
        return listOf(epStr, timeStr).filter { it.isNotEmpty() }.joinToString(" ")
    }
}

