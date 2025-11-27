package com.gaoshiqi.otakumap.widget

import android.content.Context
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import com.gaoshiqi.otakumap.databinding.ItemTagBinding

/**
 * Created by gaoshiqi
 * on 2025/6/1 21:23
 * email: gaoshiqi@bilibili.com
 */
class TagGroupView @JvmOverloads constructor(
    context: Context,
    attrs: android.util.AttributeSet? = null,
    defStyleAttr: Int = 0
): ViewGroup(context, attrs, defStyleAttr) {

    data class Tag(
        val text: String,
        @DrawableRes val iconRes: Int? = null
    )

    private var tagSpacing = dpToPx(8f) // 标签之间的间距
    private var lineSpacing = dpToPx(8f) // 行间距
    private var tagRadius = 8 // 标签圆角半径
    private var defaultTextColor = ContextCompat.getColor(context, com.gaoshiqi.otakumap.R.color.black_85)
    private var defaultBgColor = ContextCompat.getColor(context, com.gaoshiqi.otakumap.R.color.transparent)

    private val tags = mutableListOf<Tag>()
    
    init {
        context.obtainStyledAttributes(attrs, com.gaoshiqi.otakumap.R.styleable.TagGroupView).apply {
            tagSpacing = getDimensionPixelSize(com.gaoshiqi.otakumap.R.styleable.TagGroupView_tagSpacing, tagSpacing)
            lineSpacing = getDimensionPixelSize(com.gaoshiqi.otakumap.R.styleable.TagGroupView_lineSpacing, lineSpacing)
            defaultTextColor = getColor(com.gaoshiqi.otakumap.R.styleable.TagGroupView_defaultTextColor, defaultTextColor)
            defaultBgColor = getColor(com.gaoshiqi.otakumap.R.styleable.TagGroupView_defaultBgColor, defaultBgColor)
            recycle()
        }
    }

    /**
     * 设置标签数据
     */
    fun setTags(tagList: List<Tag>) {
        tags.clear()
        tags.addAll(tagList)
        removeAllViews()
        createTagViews()
        requestLayout()
    }
    
    fun clearTags() {
        tags.clear()
        removeAllViews()
        requestLayout()
    }
    
    private fun createTagViews() {
        for (tag in tags) {
            addTagView(tag)
        }
    }

    private fun addTagView(tag: Tag) {
        val mBinding = ItemTagBinding.inflate(LayoutInflater.from(context), this, false)

        // 设置标签文字
        mBinding.tagText.apply {
            text = tag.text
            setTextColor(defaultTextColor)
        }
        // 设置标签图标
        if (tag.iconRes != null) {
            mBinding.tagIcon.apply {
                visibility = View.VISIBLE
                val drawable = ContextCompat.getDrawable(context, tag.iconRes)?.mutate()
                drawable?.let {
                    setImageDrawable(it)
                }
            }
        } else {
            mBinding.tagIcon.visibility = View.GONE
        }
        // 设置标签背景
        mBinding.tagContainer.background = createRoundRectDrawable(defaultBgColor, tagRadius)
        addView(mBinding.root)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val widthSize = MeasureSpec.getSize(widthMeasureSpec)
        val widthMode = MeasureSpec.getMode(widthMeasureSpec)

        var totalHeight = 0
        var currentLineWidth = 0
        var currentLineHeight = 0
        var currentLineStartIndex = 0

        for (i in 0 until childCount) {
            val child = getChildAt(i)
            measureChild(child, widthMeasureSpec, heightMeasureSpec)

            val childWidth = child.measuredWidth
            val childHeight = child.measuredHeight

            // 检查是否需要换行
            if (currentLineWidth + childWidth > widthSize - paddingLeft - paddingRight) {
                // 测量当前行高度
                totalHeight += currentLineHeight
                if (totalHeight > 0) totalHeight += lineSpacing
                // 重置当前行
                currentLineWidth = 0
                currentLineHeight = 0
                currentLineStartIndex = i
            }

            // 添加当前标签
            currentLineWidth += childWidth + if (i > currentLineStartIndex) tagSpacing else 0
            currentLineHeight = maxOf(currentLineHeight, childHeight)
        }

        // 添加最后一行高度
        totalHeight += currentLineHeight + paddingTop + paddingBottom
        // 设置最后尺寸
        val measuredWidth = if (widthMode == MeasureSpec.EXACTLY) {
            widthSize
        } else {
            minOf(widthSize, currentLineWidth + paddingLeft + paddingRight)
        }

        val measuredHeight = resolveSize(totalHeight, heightMeasureSpec)
        setMeasuredDimension(measuredWidth, measuredHeight)
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        val width = r - l
        var currentLeft = paddingLeft
        var currentTop = paddingTop
        var currentLineHeight = 0

        for (i in 0 until childCount) {
            val child = getChildAt(i)
            if (child.visibility == View.GONE) continue

            val childWidth = child.measuredWidth
            val childHeight = child.measuredHeight

            // 检查是否需要换行
            if (currentLeft + childWidth > width - paddingRight) {
                currentLeft = paddingLeft
                currentTop += currentLineHeight + lineSpacing
                currentLineHeight = 0
            }

            child.layout(
                currentLeft,
                currentTop,
                currentLeft + childWidth,
                currentTop + childHeight
            )

            currentLeft += childWidth + tagSpacing
            currentLineHeight = maxOf(currentLineHeight, childHeight)
        }
    }

    private fun dpToPx(dp: Float): Int{
        return (dp * resources.displayMetrics.density).toInt()
    }

    private fun pxToDp(px: Int): Float{
        return px / resources.displayMetrics.density
    }

    private fun createRoundRectDrawable(@ColorInt color: Int, cornerRadius: Int): Drawable {
        val drawable = GradientDrawable()
        drawable.shape = GradientDrawable.RECTANGLE
        drawable.cornerRadius = cornerRadius.toFloat()
        drawable.setColor(color)
        drawable.setStroke(3, ContextCompat.getColor(context, com.gaoshiqi.otakumap.R.color.black_85))
        return drawable
    }
}