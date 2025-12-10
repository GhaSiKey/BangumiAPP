package com.gaoshiqi.otakumap.widget

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.gaoshiqi.otakumap.R

/**
 * 搜索历史专用的 TagGroupView 子类
 * 支持：内嵌展开/收起按钮、编辑模式（显示删除按钮）
 */
class SearchHistoryTagGroupView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : TagGroupView(context, attrs, defStyleAttr) {

    interface OnHistoryTagActionListener {
        fun onTagClick(keyword: String, position: Int)
        fun onTagDelete(keyword: String, position: Int)
    }

    private var historyTagListener: OnHistoryTagActionListener? = null
    private var isEditMode = false

    // 展开/收起按钮
    private var expandButton: View? = null
    private var collapseButton: View? = null

    fun setOnHistoryTagActionListener(listener: OnHistoryTagActionListener?) {
        historyTagListener = listener
    }

    /**
     * 设置编辑模式
     * 编辑模式下：自动展开、显示删除按钮、隐藏展开/收起按钮、禁用 tag 点击
     */
    fun setEditMode(edit: Boolean) {
        if (isEditMode != edit) {
            isEditMode = edit
            if (edit) {
                // 编辑模式强制展开
                mIsExpanded = true
            }
            updateAllTagsEditMode()
            requestLayout()
        }
    }

    fun isEditMode(): Boolean = isEditMode

    /**
     * 获取有效的最大行数
     * 编辑模式或展开状态下无限制，收起状态使用设置的 maxLines
     */
    private fun getEffectiveMaxLines(): Int = when {
        isEditMode -> Int.MAX_VALUE
        mIsExpanded -> Int.MAX_VALUE
        else -> maxLines
    }

    /**
     * 获取 tag 数量（不包含展开/收起按钮）
     */
    private fun getTagCount(): Int = (childCount - 2).coerceAtLeast(0)

    override fun setTags(tagList: List<Tag>) {
        tags.clear()
        tags.addAll(tagList)
        removeAllViews()
        createTagViews()
        createExpandCollapseButtons()
        requestLayout()
    }

    override fun setExpanded(expanded: Boolean) {
        // 编辑模式下不允许收起
        if (isEditMode && !expanded) return
        if (mIsExpanded != expanded) {
            mIsExpanded = expanded
            requestLayout()
        }
    }

    override fun createTagViews() {
        tags.forEachIndexed { index, tag ->
            addHistoryTagView(tag, index)
        }
    }

    private fun addHistoryTagView(tag: Tag, index: Int) {
        val tagView = LayoutInflater.from(context).inflate(R.layout.item_history_tag, this, false)
        val tagText = tagView.findViewById<TextView>(R.id.tag_text)
        val tagDelete = tagView.findViewById<ImageView>(R.id.tag_delete)
        val tagContainer = tagView.findViewById<View>(R.id.tag_container)

        // 设置文字
        tagText.text = tag.text
        tagText.setTextColor(defaultTextColor)
        if (tagTextSize > 0) {
            tagText.setTextSize(android.util.TypedValue.COMPLEX_UNIT_PX, tagTextSize)
        }

        // 设置边距
        val paddingH = if (tagPaddingHorizontal >= 0) tagPaddingHorizontal else tagContainer.paddingLeft
        val paddingV = if (tagPaddingVertical >= 0) tagPaddingVertical else tagContainer.paddingTop
        tagContainer.setPadding(paddingH, paddingV, paddingH, paddingV)

        // 设置背景
        tagContainer.background = createRoundRectDrawable(defaultBgColor, tagRadius)

        // 删除按钮
        tagDelete.visibility = if (isEditMode) View.VISIBLE else View.GONE
        tagDelete.setOnClickListener {
            historyTagListener?.onTagDelete(tag.text, index)
        }

        // Tag 点击事件（编辑模式下禁用）
        tagView.setOnClickListener {
            if (!isEditMode) {
                historyTagListener?.onTagClick(tag.text, index)
            }
        }

        addView(tagView)
    }

    private fun createExpandCollapseButtons() {
        // 创建展开按钮 ∨
        expandButton = createControlButton(R.drawable.ic_expand_more).apply {
            setOnClickListener {
                setExpanded(true)
                requestLayout()
            }
        }

        // 创建收起按钮 ∧
        collapseButton = createControlButton(R.drawable.ic_expand_less).apply {
            setOnClickListener {
                setExpanded(false)
                requestLayout()
            }
        }

        addView(expandButton)
        addView(collapseButton)
    }

    private fun createControlButton(iconRes: Int): View {
        val button = LayoutInflater.from(context).inflate(R.layout.item_history_tag, this, false)
        val tagText = button.findViewById<TextView>(R.id.tag_text)
        val tagDelete = button.findViewById<ImageView>(R.id.tag_delete)
        val tagContainer = button.findViewById<View>(R.id.tag_container)

        // 隐藏文字，使用图标代替
        tagText.visibility = View.GONE
        tagDelete.visibility = View.GONE

        // 创建一个 ImageView 来显示图标
        val iconView = ImageView(context).apply {
            setImageResource(iconRes)
            layoutParams = LayoutParams(dpToPx(16f), dpToPx(16f))
        }

        // 替换内容
        (tagContainer as? android.view.ViewGroup)?.apply {
            removeAllViews()
            addView(iconView)
        }

        // 设置背景
        tagContainer.background = createRoundRectDrawable(
            ContextCompat.getColor(context, R.color.gray_f5),
            tagRadius
        )

        // 设置边距
        val padding = dpToPx(8f)
        tagContainer.setPadding(padding, padding, padding, padding)

        return button
    }

    private fun updateAllTagsEditMode() {
        // 更新所有 tag 的删除按钮可见性
        for (i in 0 until childCount) {
            val child = getChildAt(i)
            if (child == expandButton || child == collapseButton) continue

            val deleteBtn = child.findViewById<ImageView>(R.id.tag_delete)
            deleteBtn?.visibility = if (isEditMode) View.VISIBLE else View.GONE
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val widthSize = MeasureSpec.getSize(widthMeasureSpec)

        // 测量所有子 View
        for (i in 0 until childCount) {
            measureChild(getChildAt(i), widthMeasureSpec, heightMeasureSpec)
        }

        // 计算实际总行数（不包含展开/收起按钮）
        totalLineCount = calculateTagLineCount(widthSize)

        val effectiveMaxLines = getEffectiveMaxLines()
        val tagCount = getTagCount()

        var totalHeight = 0
        var currentLineWidth = 0
        var currentLineHeight = 0
        var lineCount = 1

        for (i in 0 until tagCount) {
            val child = getChildAt(i)
            val childWidth = child.measuredWidth
            val childHeight = child.measuredHeight

            // 检查是否需要换行
            if (currentLineWidth + childWidth > widthSize - paddingLeft - paddingRight && currentLineWidth > 0) {
                totalHeight += currentLineHeight + lineSpacing
                currentLineWidth = 0
                currentLineHeight = 0
                lineCount++

                if (lineCount > effectiveMaxLines) {
                    break
                }
            }

            currentLineWidth += childWidth + tagSpacing
            currentLineHeight = maxOf(currentLineHeight, childHeight)
        }

        // 添加最后一行高度
        totalHeight += currentLineHeight + paddingTop + paddingBottom

        setMeasuredDimension(
            MeasureSpec.getSize(widthMeasureSpec),
            resolveSize(totalHeight, heightMeasureSpec)
        )
    }

    private fun calculateTagLineCount(widthSize: Int): Int {
        var currentLineWidth = 0
        var lineCount = 1
        val tagCount = getTagCount()

        for (i in 0 until tagCount) {
            val child = getChildAt(i)
            val childWidth = child.measuredWidth

            if (currentLineWidth + childWidth > widthSize - paddingLeft - paddingRight && currentLineWidth > 0) {
                currentLineWidth = 0
                lineCount++
            }

            currentLineWidth += childWidth + tagSpacing
        }

        return lineCount
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        val width = r - l
        var currentLeft = paddingLeft
        var currentTop = paddingTop
        var currentLineHeight = 0
        var lineCount = 1

        val effectiveMaxLines = getEffectiveMaxLines()
        val tagCount = getTagCount()
        var lastVisibleTagIndex = -1

        // 先隐藏展开/收起按钮
        expandButton?.visibility = View.GONE
        collapseButton?.visibility = View.GONE

        for (i in 0 until tagCount) {
            val child = getChildAt(i)
            val childWidth = child.measuredWidth
            val childHeight = child.measuredHeight

            // 检查是否需要换行
            if (currentLeft + childWidth > width - paddingRight && currentLeft > paddingLeft) {
                currentLeft = paddingLeft
                currentTop += currentLineHeight + lineSpacing
                currentLineHeight = 0
                lineCount++

                // 收起状态下超过 maxLines，需要显示展开按钮
                if (lineCount > effectiveMaxLines) {
                    // 隐藏剩余的 tag
                    for (j in i until tagCount) {
                        getChildAt(j).visibility = View.GONE
                    }

                    // 在最后一个可见 tag 后面显示展开按钮
                    if (!isEditMode) {
                        showExpandButton(lastVisibleTagIndex, width)
                    }
                    return
                }
            }

            child.visibility = View.VISIBLE
            child.layout(currentLeft, currentTop, currentLeft + childWidth, currentTop + childHeight)

            lastVisibleTagIndex = i
            currentLeft += childWidth + tagSpacing
            currentLineHeight = maxOf(currentLineHeight, childHeight)
        }

        // 展开状态下，在最后一个 tag 后面显示收起按钮
        if (mIsExpanded && !isEditMode && isExpandable()) {
            showCollapseButton(currentLeft, currentTop, currentLineHeight, width)
        }
    }

    private fun showExpandButton(lastVisibleIndex: Int, containerWidth: Int) {
        expandButton?.let { btn ->
            btn.visibility = View.VISIBLE

            // 找到最后一个可见 tag 的位置
            if (lastVisibleIndex >= 0) {
                val lastTag = getChildAt(lastVisibleIndex)
                var left = lastTag.right + tagSpacing
                val top = lastTag.top

                // 检查是否能放下
                if (left + btn.measuredWidth > containerWidth - paddingRight) {
                    // 放不下，需要换行或替换最后一个 tag
                    // 简单处理：隐藏最后一个 tag，把按钮放在它的位置
                    lastTag.visibility = View.GONE
                    left = lastTag.left
                }

                btn.layout(left, top, left + btn.measuredWidth, top + btn.measuredHeight)
            }
        }
    }

    private fun showCollapseButton(currentLeft: Int, currentTop: Int, currentLineHeight: Int, containerWidth: Int) {
        collapseButton?.let { btn ->
            btn.visibility = View.VISIBLE

            var left = currentLeft
            var top = currentTop

            // 检查当前行是否能放下
            if (left + btn.measuredWidth > containerWidth - paddingRight) {
                // 换行
                left = paddingLeft
                top += currentLineHeight + lineSpacing
            }

            btn.layout(left, top, left + btn.measuredWidth, top + btn.measuredHeight)
        }
    }
}
