package com.gaoshiqi.otakumap.collection.v2.ui

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.bumptech.glide.integration.compose.placeholder
import com.gaoshiqi.otakumap.R

/**
 * 仪表盘中心番剧封面组件
 * 特性:
 * - 圆形裁剪 + 白色边框
 * - 弹出动画: 与整体仪表盘同步
 * - 阴影效果: 营造浮层感
 * - Glide 加载: 支持占位图和错误图
 *
 * @param imageUrl 封面图片 URL
 * @param animeName 番剧名称 (用于无障碍描述)
 * @param size 封面尺寸,默认 120.dp
 * @param isVisible 是否已展开 (控制动画)
 */
@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun AnimeCoverCenter(
    imageUrl: String,
    animeName: String,
    size: Dp = 120.dp,
    isVisible: Boolean = true,
    modifier: Modifier = Modifier
) {
    // 弹出缩放动画
    val scale by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0.5f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,  // 0.75
            stiffness = Spring.StiffnessMedium                // 400
        ),
        label = "coverScale"
    )

    Box(
        modifier = modifier
            .size(size)
            .scale(scale)
            .shadow(
                elevation = 12.dp,
                shape = CircleShape
            )
            .clip(CircleShape)
            .border(
                width = 4.dp,
                color = Color.White.copy(alpha = 0.8f),
                shape = CircleShape
            )
            .semantics {
                contentDescription = "番剧封面:$animeName"
            }
    ) {
        GlideImage(
            model = imageUrl,
            contentDescription = null,  // 外层已提供语义描述
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
            loading = placeholder(R.drawable.ic_cover_placeholder_36),
            failure = placeholder(R.drawable.ic_cover_placeholder_36)
        )
    }
}
