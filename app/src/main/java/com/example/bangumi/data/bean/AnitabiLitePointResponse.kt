package com.example.bangumi.data.bean

/**
 * Created by gaoshiqi
 * on 2025/6/22 20:42
 * email: gaoshiqi@bilibili.com
 */
// 轻量版地标信息数据类
data class LitePoint(
    val id: String,           // 地标 ID
    val cn: String,            // 地标中文译名
    val name: String,          // 地标原名
    val image: String,         // 地标缩略图 URL
    val ep: String,               // 出现集数
    val s: String,                // 截图时间点（秒）
    val geo: List<Double>          // GPS 位置信息
)

