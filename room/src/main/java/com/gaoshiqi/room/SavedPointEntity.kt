package com.gaoshiqi.room

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "saved_points")
data class SavedPointEntity(
    @PrimaryKey
    val id: String,                    // 主键：{subjectId}_{pointId}
    val subjectId: Int,                // 番剧 ID
    val subjectName: String,           // 番剧名称
    val subjectCover: String,          // 番剧封面 URL
    val pointId: String,               // 地点 ID
    val pointName: String,             // 地点原名
    val pointNameCn: String,           // 地点中文名
    val pointImage: String,            // 地点图片 URL
    val lat: Double,                   // 纬度
    val lng: Double,                   // 经度
    val episode: String?,              // 出现集数
    val timeInEpisode: String?,        // 出现时间（秒）
    val savedTime: Long = System.currentTimeMillis()  // 收藏时间戳
) {
    companion object {
        fun generateId(subjectId: Int, pointId: String): String {
            return "${subjectId}_${pointId}"
        }
    }
}
