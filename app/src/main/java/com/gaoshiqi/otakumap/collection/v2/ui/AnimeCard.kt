package com.gaoshiqi.otakumap.collection.v2.ui

import android.view.HapticFeedbackConstants
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.bumptech.glide.integration.compose.placeholder
import com.gaoshiqi.otakumap.R
import com.gaoshiqi.room.AnimeEntity
import com.gaoshiqi.room.CollectionStatus

/**
 * 径向菜单配置
 */
private val radialMenuItems = listOf(
    RadialMenuItem(CollectionStatus.DOING, "在看", Color(0xFF4CAF50)),   // 绿色
    RadialMenuItem(CollectionStatus.WISH, "想看", Color(0xFF2196F3)),    // 蓝色
    RadialMenuItem(CollectionStatus.COLLECT, "看过", Color(0xFF9C27B0)), // 紫色
    RadialMenuItem(CollectionStatus.ON_HOLD, "搁置", Color(0xFFFF9800)), // 橙色
    RadialMenuItem(-1, "抛弃", Color(0xFFF44336))                         // 红色 - 取消收藏
)

/**
 * 状态角标颜色映射
 */
private fun getStatusBadgeInfo(status: Int): Pair<String, Color>? = when (status) {
    CollectionStatus.DOING -> "在看" to Color(0xFF4CAF50)
    CollectionStatus.WISH -> "想看" to Color(0xFF2196F3)
    CollectionStatus.COLLECT -> "看过" to Color(0xFF9C27B0)
    CollectionStatus.ON_HOLD -> "搁置" to Color(0xFFFF9800)
    else -> null
}

/**
 * 状态角标组件
 */
@Composable
fun StatusBadge(
    status: Int,
    modifier: Modifier = Modifier
) {
    val badgeInfo = getStatusBadgeInfo(status) ?: return

    Box(
        modifier = modifier
            .padding(4.dp)
            .background(badgeInfo.second, RoundedCornerShape(4.dp))
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Text(
            text = badgeInfo.first,
            color = Color.White,
            fontSize = 10.sp
        )
    }
}

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun AnimeCard(
    anime: AnimeEntity,
    onClick: () -> Unit,
    onStatusChange: (Int) -> Unit,
    onRemove: () -> Unit,
    showStatusBadge: Boolean = false,
    modifier: Modifier = Modifier
) {
    val view = LocalView.current

    // 径向菜单状态
    var showRadialMenu by remember { mutableStateOf(false) }
    var selectedMenuIndex by remember { mutableIntStateOf(-1) }
    var cardCenterX by remember { mutableFloatStateOf(0f) }
    var cardCenterY by remember { mutableFloatStateOf(0f) }
    var touchX by remember { mutableFloatStateOf(0f) }
    var touchY by remember { mutableFloatStateOf(0f) }
    var showRemoveDialog by remember { mutableStateOf(false) }

    Box(modifier = modifier) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .onGloballyPositioned { coordinates ->
                    val position = coordinates.positionInRoot()
                    cardCenterX = position.x + coordinates.size.width / 2f
                    cardCenterY = position.y + coordinates.size.height / 2f
                }
                .pointerInput(Unit) {
                    detectTapGestures(
                        onTap = { onClick() },
                        onLongPress = { offset ->
                            // 触发震动反馈
                            view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
                            showRadialMenu = true
                            touchX = offset.x
                            touchY = offset.y
                        }
                    )
                }
                .pointerInput(showRadialMenu) {
                    if (showRadialMenu) {
                        awaitPointerEventScope {
                            while (true) {
                                val event = awaitPointerEvent()
                                val change = event.changes.firstOrNull() ?: continue

                                if (change.pressed) {
                                    // 更新触摸位置并计算选中项
                                    touchX = change.position.x
                                    touchY = change.position.y

                                    // 计算相对于卡片中心的选中项
                                    val cardWidth = size.width.toFloat()
                                    val cardHeight = size.height.toFloat()
                                    selectedMenuIndex = calculateSelectedIndex(
                                        touchX = touchX,
                                        touchY = touchY,
                                        centerX = cardWidth / 2,
                                        centerY = cardHeight / 2,
                                        itemCount = radialMenuItems.size,
                                        radius = 150f,
                                        selectionRadius = 50f
                                    )
                                } else {
                                    // 手指抬起，执行选中的操作
                                    if (selectedMenuIndex >= 0) {
                                        val selectedItem = radialMenuItems[selectedMenuIndex]
                                        view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                                        if (selectedItem.id == -1) {
                                            // 取消收藏需要二次确认
                                            showRemoveDialog = true
                                        } else {
                                            // 切换状态
                                            onStatusChange(selectedItem.id)
                                        }
                                    }
                                    showRadialMenu = false
                                    selectedMenuIndex = -1
                                    break
                                }
                            }
                        }
                    }
                },
            shape = RoundedCornerShape(8.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            colors = CardDefaults.cardColors(containerColor = Color.Transparent)
        ) {
            Column {
                // 封面图片
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(3f / 4f)
                        .clip(RoundedCornerShape(8.dp))
                ) {
                    GlideImage(
                        model = anime.imageUrl,
                        contentDescription = anime.displayName,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                        loading = placeholder(R.drawable.ic_cover_placeholder_36),
                        failure = placeholder(R.drawable.ic_cover_placeholder_36)
                    )

                    // 状态角标（仅在「全部」Tab 中显示）
                    if (showStatusBadge) {
                        StatusBadge(
                            status = anime.collectionStatus,
                            modifier = Modifier.align(Alignment.TopEnd)
                        )
                    }

                    // 进度条（如果有总集数）
                    if (anime.totalEpisodes > 0) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .fillMaxWidth()
                                .background(Color.Black.copy(alpha = 0.6f))
                                .padding(horizontal = 4.dp, vertical = 2.dp)
                        ) {
                            Column {
                                Text(
                                    text = "${anime.watchedEpisodes}/${anime.totalEpisodes}",
                                    color = Color.White,
                                    fontSize = 10.sp,
                                    modifier = Modifier.align(Alignment.End)
                                )
                                LinearProgressIndicator(
                                    progress = { anime.watchedEpisodes.toFloat() / anime.totalEpisodes },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(3.dp)
                                        .clip(RoundedCornerShape(2.dp)),
                                    color = MaterialTheme.colorScheme.primary,
                                    trackColor = Color.White.copy(alpha = 0.3f)
                                )
                            }
                        }
                    }
                }

                // 标题（中文名优先，完整展示）
                Text(
                    text = anime.displayName,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp, vertical = 4.dp),
                    fontSize = 12.sp,
                    lineHeight = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        // 径向菜单覆盖层
        if (showRadialMenu) {
            RadialMenu(
                items = radialMenuItems,
                isVisible = true,
                selectedIndex = selectedMenuIndex,
                centerX = 0f,  // 相对于 Box 的中心
                centerY = 0f,
                radius = 150f,
                modifier = Modifier.align(Alignment.Center)
            )
        }
    }

    if (showRemoveDialog) {
        AlertDialog(
            onDismissRequest = { showRemoveDialog = false },
            title = { Text(stringResource(R.string.collection_remove_confirm_title)) },
            text = { Text(stringResource(R.string.collection_remove_confirm_message, anime.displayName)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showRemoveDialog = false
                        onRemove()
                    }
                ) {
                    Text(
                        stringResource(R.string.confirm),
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showRemoveDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}
