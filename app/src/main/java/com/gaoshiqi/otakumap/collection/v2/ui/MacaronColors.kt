package com.gaoshiqi.otakumap.collection.v2.ui

import androidx.compose.ui.graphics.Color
import com.gaoshiqi.room.CollectionStatus

/**
 * 马卡龙色系配色方案 - 小清新风格
 * 设计原则:
 * - 饱和度 < 50%: 避免视觉疲劳
 * - 明度 > 70%: 营造轻盈感
 * - 色相分布均匀: 避免色彩混淆
 * - 支持暗黑模式: 保持 20% 透明度叠加
 */
object MacaronColors {
    // 状态色彩 - 饱和度降低,明度提高,营造柔和感
    val Watching = Color(0xFFB8E6CC)    // 薄荷绿 (RGB: 184, 230, 204)
    val Wish = Color(0xFFF9C7D9)        // 樱花粉 (RGB: 249, 199, 217)
    val Completed = Color(0xFFB7D8F6)   // 天空蓝 (RGB: 183, 216, 246)
    val OnHold = Color(0xFFDCC8E8)      // 薰衣草紫 (RGB: 220, 200, 232)
    val Dropped = Color(0xFFFFBAAD)     // 珊瑚红 (RGB: 255, 186, 173)

    // 毛玻璃效果 - 半透明背景
    val GlassmorphismBg = Color.White.copy(alpha = 0.3f)
    val GlassmorphismBorder = Color.White.copy(alpha = 0.8f)

    /**
     * 根据收藏状态获取对应的马卡龙色彩
     * @param status 收藏状态,使用 -1 表示取消收藏(抛弃)
     */
    fun getColorForStatus(status: Int): Color = when (status) {
        CollectionStatus.DOING -> Watching
        CollectionStatus.WISH -> Wish
        CollectionStatus.COLLECT -> Completed
        CollectionStatus.ON_HOLD -> OnHold
        -1 -> Dropped  // 取消收藏
        else -> Color.Gray
    }

    /**
     * 获取状态的中文标签
     */
    fun getLabelForStatus(status: Int): String = when (status) {
        CollectionStatus.DOING -> "在看"
        CollectionStatus.WISH -> "想看"
        CollectionStatus.COLLECT -> "看过"
        CollectionStatus.ON_HOLD -> "搁置"
        -1 -> "抛弃"
        else -> "未知"
    }
}
