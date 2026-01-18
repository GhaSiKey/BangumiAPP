package com.gaoshiqi.otakumap.collection.v2.ui

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

/**
 * 径向菜单选项数据
 */
data class RadialMenuItem(
    val id: Int,
    val label: String,
    val color: Color
)

/**
 * 径向悬浮菜单组件
 *
 * 长按时在卡片周围显示悬浮气泡，手指滑动到气泡上时高亮选中。
 *
 * @param items 菜单选项列表
 * @param isVisible 是否显示菜单
 * @param selectedIndex 当前选中的选项索引（-1 表示未选中）
 * @param centerX 中心点 X 坐标（相对于父容器）
 * @param centerY 中心点 Y 坐标（相对于父容器）
 * @param radius 气泡分布半径
 */
@Composable
fun RadialMenu(
    items: List<RadialMenuItem>,
    isVisible: Boolean,
    selectedIndex: Int,
    centerX: Float,
    centerY: Float,
    radius: Float = 150f,
    modifier: Modifier = Modifier
) {
    val animatedScale by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "radialMenuScale"
    )

    val animatedAlpha by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0f,
        animationSpec = spring(stiffness = Spring.StiffnessHigh),
        label = "radialMenuAlpha"
    )

    if (animatedAlpha > 0.01f) {
        Box(modifier = modifier) {
            items.forEachIndexed { index, item ->
                // 计算每个气泡的角度位置（均匀分布）
                // 从顶部开始（-90度），逆时针排列
                val angleStep = 360f / items.size
                val angleDegrees = -90f + index * angleStep
                val angleRadians = angleDegrees * PI / 180

                // 计算气泡位置偏移
                val offsetX = (cos(angleRadians) * radius * animatedScale).toFloat()
                val offsetY = (sin(angleRadians) * radius * animatedScale).toFloat()

                val isSelected = selectedIndex == index
                val bubbleScale by animateFloatAsState(
                    targetValue = if (isSelected) 1.3f else 1f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessHigh
                    ),
                    label = "bubbleScale$index"
                )

                RadialMenuBubble(
                    item = item,
                    isSelected = isSelected,
                    modifier = Modifier
                        .offset {
                            IntOffset(
                                (centerX + offsetX).roundToInt(),
                                (centerY + offsetY).roundToInt()
                            )
                        }
                        .scale(animatedScale * bubbleScale)
                        .alpha(animatedAlpha)
                )
            }
        }
    }
}

@Composable
private fun RadialMenuBubble(
    item: RadialMenuItem,
    isSelected: Boolean,
    modifier: Modifier = Modifier
) {
    val backgroundColor = if (isSelected) {
        item.color
    } else {
        item.color.copy(alpha = 0.85f)
    }

    val textColor = if (isSelected) {
        Color.White
    } else {
        Color.White.copy(alpha = 0.9f)
    }

    Box(
        modifier = modifier
            .size(72.dp)
            .shadow(
                elevation = if (isSelected) 12.dp else 6.dp,
                shape = CircleShape
            )
            .background(backgroundColor, CircleShape)
            .padding(6.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = item.label,
            color = textColor,
            fontSize = 14.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            textAlign = TextAlign.Center,
            maxLines = 2
        )
    }
}

/**
 * 根据触摸位置计算选中的菜单项索引
 *
 * @param touchX 触摸点 X 坐标
 * @param touchY 触摸点 Y 坐标
 * @param centerX 菜单中心 X 坐标
 * @param centerY 菜单中心 Y 坐标
 * @param itemCount 菜单项数量
 * @param radius 菜单半径
 * @param selectionRadius 选中判定半径（气泡大小的一半左右）
 * @return 选中的索引，-1 表示未选中任何项
 */
fun calculateSelectedIndex(
    touchX: Float,
    touchY: Float,
    centerX: Float,
    centerY: Float,
    itemCount: Int,
    radius: Float = 150f,
    selectionRadius: Float = 50f
): Int {
    for (index in 0 until itemCount) {
        val angleStep = 360f / itemCount
        val angleDegrees = -90f + index * angleStep
        val angleRadians = angleDegrees * PI / 180

        val bubbleX = centerX + (cos(angleRadians) * radius).toFloat()
        val bubbleY = centerY + (sin(angleRadians) * radius).toFloat()

        val distance = kotlin.math.sqrt(
            (touchX - bubbleX) * (touchX - bubbleX) +
            (touchY - bubbleY) * (touchY - bubbleY)
        )

        if (distance <= selectionRadius) {
            return index
        }
    }
    return -1
}
