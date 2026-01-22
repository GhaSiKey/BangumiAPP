package com.gaoshiqi.otakumap.collection.v2.ui

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.gaoshiqi.room.AnimeEntity

/**
 * 收藏状态仪表盘底部弹窗
 * 特性:
 * - Material3 ModalBottomSheet 容器
 * - 透明背景 + 半透明遮罩
 * - 支持下滑手势关闭
 * - 点击背景区域关闭
 *
 * @param anime 番剧数据实体
 * @param onStatusSelected 状态选中回调 (status: Int)
 * @param onDismiss 关闭回调
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatusDialBottomSheet(
    anime: AnimeEntity,
    onStatusSelected: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true  // 直接完全展开,跳过部分展开状态
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color.Transparent,  // 透明背景,由内容层提供背景色
        scrimColor = Color.Black.copy(alpha = 0.4f),  // 背景遮罩
        dragHandle = null,  // 隐藏默认拖拽把手
        contentWindowInsets = { WindowInsets(0) }  // 禁用默认 insets，由内容层自行处理 Edge-to-Edge
    ) {
        SemiCircleDialContent(
            anime = anime,
            onSectorSelected = { status ->
                onStatusSelected(status)
                // 注意: onDismiss 在外层调用,延迟以展示选中动画
            }
        )
    }
}
