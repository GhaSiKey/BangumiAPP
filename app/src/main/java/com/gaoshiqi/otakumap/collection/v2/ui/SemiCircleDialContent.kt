package com.gaoshiqi.otakumap.collection.v2.ui

import android.annotation.SuppressLint
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.paneTitle
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.annotation.StringRes
import androidx.compose.ui.res.stringResource
import com.gaoshiqi.otakumap.R
import com.gaoshiqi.room.AnimeEntity
import com.gaoshiqi.room.CollectionStatus
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * 半圆形仪表盘内容布局
 * 设计：半圆形背景，5个按钮沿圆弧顶部排列，大封面在中心偏下
 */
@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun SemiCircleDialContent(
    anime: AnimeEntity,
    onSectorSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    // 封面尺寸
    val centerCoverSize = 200.dp

    // 获取导航栏高度用于 Edge-to-Edge 适配
    val density = LocalDensity.current
    val navigationBarHeight = with(density) {
        WindowInsets.navigationBars.getBottom(this).toDp()
    }

    // 背景色 - 适配日夜间模式（使用 surfaceContainerHigh 在夜间模式下更亮一些）
    val backgroundColor = MaterialTheme.colorScheme.surfaceContainerHigh
    // 边框色 - 用于区分弹窗与背景
    val outlineColor = MaterialTheme.colorScheme.outlineVariant

    // 状态管理
    var selectedSector by remember { mutableIntStateOf(-1) }
    var isExpanded by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    // 扇形数据定义 - 5个状态按钮沿上半圆弧排列
    val sectors = remember {
        listOf(
            SectorData(-1, R.string.collection_dropped, MacaronColors.Dropped, 170.0),
            SectorData(CollectionStatus.ON_HOLD, R.string.collection_on_hold, MacaronColors.OnHold, 130.0),
            SectorData(CollectionStatus.DOING, R.string.collection_doing, MacaronColors.Watching, 90.0),
            SectorData(CollectionStatus.WISH, R.string.collection_wish, MacaronColors.Wish, 50.0),
            SectorData(CollectionStatus.COLLECT, R.string.collection_collect, MacaronColors.Completed, 10.0)
        )
    }

    // 每个扇区的动画状态
    val sectorAnimations = remember {
        sectors.map { Animatable(0f) }
    }

    // 触发展开动画
    LaunchedEffect(Unit) {
        isExpanded = true
        sectorAnimations.forEachIndexed { index, animatable ->
            launch {
                delay(index * 50L)
                animatable.animateTo(
                    targetValue = 1f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = 500f
                    )
                )
            }
        }
    }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .semantics { paneTitle = "收藏状态选择" }
    ) {
        val boxWidth = maxWidth
        // 半圆半径 = 弹窗宽度的一半
        val arcRadius = boxWidth / 2
        // 按钮排列半径 - 紧贴半圆弧内侧
        val buttonRadius = arcRadius - 40.dp
        // 顶部预留空间 - 用于容纳按钮弹跳动画溢出
        val topPadding = 20.dp
        // 底部内容区高度（标题 + 拖拽条）
        val bottomContentHeight = 70.dp
        // 整体高度：顶部预留 + 半圆高度 + 底部内容 + 导航栏高度（Edge-to-Edge 适配）
        val totalHeight = topPadding + arcRadius + bottomContentHeight + navigationBarHeight

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(totalHeight)
                .drawBehind {
                    // 绘制半圆形背景（向下偏移 topPadding）
                    val radiusPx = arcRadius.toPx()
                    val topPaddingPx = topPadding.toPx()
                    val strokeWidth = 1.dp.toPx()

                    // 填充半圆背景
                    drawArc(
                        color = backgroundColor,
                        startAngle = 180f,
                        sweepAngle = 180f,
                        useCenter = true,
                        topLeft = Offset(0f, topPaddingPx),
                        size = Size(radiusPx * 2, radiusPx * 2),
                        style = Fill
                    )

                    // 绘制半圆边框（仅顶部弧线，增强层次感）
                    drawArc(
                        color = outlineColor,
                        startAngle = 180f,
                        sweepAngle = 180f,
                        useCenter = false,
                        topLeft = Offset(0f, topPaddingPx),
                        size = Size(radiusPx * 2, radiusPx * 2),
                        style = Stroke(width = strokeWidth)
                    )

                    // 底部矩形填充（包含导航栏区域，实现 Edge-to-Edge）
                    drawRect(
                        color = backgroundColor,
                        topLeft = Offset(0f, topPaddingPx + radiusPx),
                        size = Size(size.width, size.height - topPaddingPx - radiusPx)
                    )
                }
        ) {
            // 按钮层 - 沿半圆弧排列
            sectors.forEachIndexed { index, sector ->
                val animProgress = sectorAnimations[index].value

                if (animProgress > 0f) {
                    // 圆心在半圆底部中央（向下偏移 topPadding）
                    val centerX = boxWidth / 2
                    val centerY = topPadding + arcRadius

                    // 将角度转换为弧度
                    val angleRad = sector.angle * PI / 180.0

                    // 计算按钮中心位置
                    val buttonX = centerX + buttonRadius * cos(angleRad).toFloat() * animProgress
                    val buttonY = centerY - buttonRadius * sin(angleRad).toFloat() * animProgress

                    // 按钮尺寸估算
                    val buttonWidth = 56.dp
                    val buttonHeight = 66.dp

                    val label = stringResource(sector.labelResId)
                    StatusSector(
                        status = sector.status,
                        label = label,
                        color = sector.color,
                        icon = StatusIcons.getIconForStatus(sector.status),
                        isSelected = selectedSector == index,
                        onClick = {
                            selectedSector = index
                            scope.launch {
                                delay(150)
                                onSectorSelected(sector.status)
                            }
                        },
                        modifier = Modifier
                            .offset(
                                x = buttonX - buttonWidth / 2,
                                y = buttonY - buttonHeight / 2
                            )
                            .scale(animProgress)
                            .alpha(animProgress)
                    )
                }
            }

            // 封面 - 使用绝对定位，圆心在半圆圆心位置，底部向下延伸
            AnimeCoverCenter(
                imageUrl = anime.imageUrl,
                animeName = anime.displayName,
                size = centerCoverSize,
                isVisible = isExpanded,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .offset(
                        x = 0.dp,
                        // 封面顶部位置 = topPadding + 半圆圆心Y - 封面半径，使封面圆心与半圆圆心对齐
                        y = topPadding + arcRadius - centerCoverSize / 2
                    )
            )
        }
    }
}

/**
 * 扇形数据类
 */
private data class SectorData(
    val status: Int,
    @param:StringRes val labelResId: Int,
    val color: Color,
    val angle: Double
)

@Preview(showBackground = true, backgroundColor = 0xFF888888)
@Composable
private fun SemiCircleDialContentPreview() {
    val mockAnime = AnimeEntity(
        id = 1,
        name = "Bocchi the Rock!",
        nameCn = "孤独摇滚！",
        imageUrl = "https://example.com/cover.jpg",
        collectionStatus = CollectionStatus.DOING,
        watchedEpisodes = 6,
        totalEpisodes = 12
    )
    SemiCircleDialContent(
        anime = mockAnime,
        onSectorSelected = {}
    )
}
