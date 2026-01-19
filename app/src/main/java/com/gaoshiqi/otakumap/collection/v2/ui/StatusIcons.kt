package com.gaoshiqi.otakumap.collection.v2.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Star
import androidx.compose.ui.graphics.vector.ImageVector
import com.gaoshiqi.room.CollectionStatus

/**
 * 收藏状态图标映射
 */
object StatusIcons {
    /**
     * 根据收藏状态返回对应的 Material Icon
     * @param status 收藏状态,使用 -1 表示取消收藏(抛弃)
     */
    fun getIconForStatus(status: Int): ImageVector = when (status) {
        CollectionStatus.DOING -> Icons.Default.Info                // 信息图标 - 在看
        CollectionStatus.WISH -> Icons.Default.FavoriteBorder       // 心形边框 - 想看
        CollectionStatus.COLLECT -> Icons.Default.Star              // 星形 - 看过
        CollectionStatus.ON_HOLD -> Icons.Default.Check             // 对勾 - 搁置
        -1 -> Icons.Default.Close                                   // 叉号 - 抛弃
        else -> Icons.Default.Favorite                              // 默认
    }
}
