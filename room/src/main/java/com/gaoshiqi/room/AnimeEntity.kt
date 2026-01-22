package com.gaoshiqi.room

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 收藏状态常量
 */
object CollectionStatus {
    const val WISH = 1      // 想看
    const val COLLECT = 2   // 看过
    const val DOING = 3     // 在看
    const val ON_HOLD = 4   // 搁置
}

@Entity(tableName = "bookmarked_anime")
data class AnimeEntity(
    @PrimaryKey
    val id: Int,
    val name: String,
    val nameCn: String,
    val imageUrl: String,
    val bookmarkTime: Long = System.currentTimeMillis(),
    val collectionStatus: Int = CollectionStatus.WISH,  // 默认「想看」
    val watchedEpisodes: Int = 0,   // 已看集数
    val totalEpisodes: Int = 0      // 总集数
) {
    /**
     * 获取显示用的标题，优先中文名
     */
    val displayName: String
        get() = nameCn.ifEmpty { name }
}
