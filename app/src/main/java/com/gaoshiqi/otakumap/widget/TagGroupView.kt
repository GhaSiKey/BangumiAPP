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

    interface OnTagClickListener {
        fun onTagClick(tag: Tag, position: Int)
    }

    data class Tag(
        val text: String,
        @DrawableRes val iconRes: Int? = null
    )

    private var tagClickListener: OnTagClickListener? = null

    fun setOnTagClickListener(listener: OnTagClickListener?) {
        tagClickListener = listener
    }

    private var tagSpacing = dpToPx(8f) // 标签之间的间距
    private var lineSpacing = dpToPx(8f) // 行间距
    private var tagRadius = dpToPx(8f) // 标签圆角半径
    private var tagTextSize = 0f // 标签文字大小，0表示使用默认值
    private var tagPaddingHorizontal = -1 // 标签左右边距，-1表示使用默认值
    private var tagPaddingVertical = -1 // 标签上下边距，-1表示使用默认值
    private var tagHeight = -1 // 标签高度，-1表示使用默认值
    private var maxLines = Int.MAX_VALUE // 最大行数，默认不限制
    private var defaultTextColor = ContextCompat.getColor(context, com.gaoshiqi.otakumap.R.color.black_85)
    private var defaultBgColor = ContextCompat.getColor(context, com.gaoshiqi.otakumap.R.color.transparent)

    private val tags = mutableListOf<Tag>()
    private var isExpanded = false // 是否展开
    private var totalLineCount = 0 // 实际总行数
    
    init {
        context.obtainStyledAttributes(attrs, com.gaoshiqi.otakumap.R.styleable.TagGroupView).apply {
            tagSpacing = getDimensionPixelSize(com.gaoshiqi.otakumap.R.styleable.TagGroupView_tagSpacing, tagSpacing)
            lineSpacing = getDimensionPixelSize(com.gaoshiqi.otakumap.R.styleable.TagGroupView_lineSpacing, lineSpacing)
            tagTextSize = getDimension(com.gaoshiqi.otakumap.R.styleable.TagGroupView_tagTextSize, tagTextSize)
            tagPaddingHorizontal = getDimensionPixelSize(com.gaoshiqi.otakumap.R.styleable.TagGroupView_tagPaddingHorizontal, tagPaddingHorizontal)
            tagPaddingVertical = getDimensionPixelSize(com.gaoshiqi.otakumap.R.styleable.TagGroupView_tagPaddingVertical, tagPaddingVertical)
            tagRadius = getDimensionPixelSize(com.gaoshiqi.otakumap.R.styleable.TagGroupView_tagRadius, tagRadius)
            tagHeight = getDimensionPixelSize(com.gaoshiqi.otakumap.R.styleable.TagGroupView_tagHeight, tagHeight)
            maxLines = getInt(com.gaoshiqi.otakumap.R.styleable.TagGroupView_maxLines, maxLines)
            defaultTextColor = getColor(com.gaoshiqi.otakumap.R.styleable.TagGroupView_defaultTextColor, defaultTextColor)
            defaultBgColor = getColor(com.gaoshiqi.otakumap.R.styleable.TagGroupView_defaultBgColor, defaultBgColor)
            recycle()
        }
    }

    /**
     * 设置是否展开
     */
    fun setExpanded(expanded: Boolean) {
        if (isExpanded != expanded) {
            isExpanded = expanded
            requestLayout()
        }
    }

    /**
     * 是否可展开（总行数超过最大行数）
     */
    fun isExpandable(): Boolean = totalLineCount > maxLines

    /**
     * 当前是否已展开
     */
    fun isExpanded(): Boolean = isExpanded

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
        tags.forEachIndexed { index, tag ->
            addTagView(tag, index)
        }
    }

    private fun addTagView(tag: Tag, index: Int) {
        val mBinding = ItemTagBinding.inflate(LayoutInflater.from(context), this, false)

        // 设置标签文字
        mBinding.tagText.apply {
            text = tag.text
            setTextColor(defaultTextColor)
            if (tagTextSize > 0) {
                setTextSize(android.util.TypedValue.COMPLEX_UNIT_PX, tagTextSize)
            }
        }
        // 设置标签边距
        val paddingH = if (tagPaddingHorizontal >= 0) tagPaddingHorizontal else mBinding.tagContainer.paddingLeft
        val paddingV = if (tagPaddingVertical >= 0) tagPaddingVertical else mBinding.tagContainer.paddingTop
        mBinding.tagContainer.setPadding(paddingH, paddingV, paddingH, paddingV)
        // 设置标签高度
        if (tagHeight > 0) {
            mBinding.tagContainer.layoutParams.height = tagHeight
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
        // 设置点击事件
        mBinding.root.setOnClickListener {
            tagClickListener?.onTagClick(tag, index)
        }
        addView(mBinding.root)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val widthSize = MeasureSpec.getSize(widthMeasureSpec)
        val widthMode = MeasureSpec.getMode(widthMeasureSpec)

        var totalHeight = 0
        var currentLineWidth = 0
        var currentLineHeight = 0
        var currentLineStartIndex = 0
        var lineCount = 1
        val effectiveMaxLines = if (isExpanded) Int.MAX_VALUE else maxLines

        for (i in 0 until childCount) {
            val child = getChildAt(i)
            measureChild(child, widthMeasureSpec, heightMeasureSpec)

            val childWidth = child.measuredWidth
            val childHeight = child.measuredHeight

            // 检查是否需要换行
            if (currentLineWidth + childWidth > widthSize - paddingLeft - paddingRight && currentLineWidth > 0) {
                // 测量当前行高度
                totalHeight += currentLineHeight
                if (totalHeight > 0) totalHeight += lineSpacing
                // 重置当前行
                currentLineWidth = 0
                currentLineHeight = 0
                currentLineStartIndex = i
                lineCount++

                // 如果超过最大行数限制，停止测量
                if (lineCount > effectiveMaxLines) {
                    break
                }
            }

            // 添加当前标签
            currentLineWidth += childWidth + if (i > currentLineStartIndex) tagSpacing else 0
            currentLineHeight = maxOf(currentLineHeight, childHeight)
        }

        // 记录实际总行数（需要完整计算）
        totalLineCount = calculateTotalLineCount(widthSize)

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

    private fun calculateTotalLineCount(widthSize: Int): Int {
        var currentLineWidth = 0
        var lineCount = 1
        var currentLineStartIndex = 0

        for (i in 0 until childCount) {
            val child = getChildAt(i)
            val childWidth = child.measuredWidth

            if (currentLineWidth + childWidth > widthSize - paddingLeft - paddingRight && currentLineWidth > 0) {
                currentLineWidth = 0
                currentLineStartIndex = i
                lineCount++
            }

            currentLineWidth += childWidth + if (i > currentLineStartIndex) tagSpacing else 0
        }

        return lineCount
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        val width = r - l
        var currentLeft = paddingLeft
        var currentTop = paddingTop
        var currentLineHeight = 0
        var lineCount = 1
        val effectiveMaxLines = if (isExpanded) Int.MAX_VALUE else maxLines

        for (i in 0 until childCount) {
            val child = getChildAt(i)

            val childWidth = child.measuredWidth
            val childHeight = child.measuredHeight

            // 检查是否需要换行
            if (currentLeft + childWidth > width - paddingRight && currentLeft > paddingLeft) {
                currentLeft = paddingLeft
                currentTop += currentLineHeight + lineSpacing
                currentLineHeight = 0
                lineCount++

                // 如果超过最大行数限制，隐藏剩余子 View
                if (lineCount > effectiveMaxLines) {
                    for (j in i until childCount) {
                        getChildAt(j).visibility = View.GONE
                    }
                    return
                }
            }

            child.visibility = View.VISIBLE
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

    private fun dpToPx(dp: Float): Int {
        return (dp * resources.displayMetrics.density).toInt()
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