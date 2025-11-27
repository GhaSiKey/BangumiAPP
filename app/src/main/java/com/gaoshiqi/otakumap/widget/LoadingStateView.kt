package com.gaoshiqi.otakumap.widget

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import com.gaoshiqi.otakumap.R
import com.gaoshiqi.otakumap.databinding.ViewLoadingStateBinding

/**
 * 通用加载状态视图组件
 *
 * 支持三种状态：
 * - Loading: 加载中，显示进度条
 * - Empty: 空数据，显示空状态图标和文案
 * - Error: 加载失败，显示错误图标、文案和重试按钮
 *
 * 使用方式：
 * ```xml
 * <com.gaoshiqi.otakumap.widget.LoadingStateView
 *     android:id="@+id/loading_state_view"
 *     android:layout_width="match_parent"
 *     android:layout_height="match_parent"
 *     app:emptyText="暂无数据"
 *     app:errorText="加载失败"
 *     app:showRetryOnError="true" />
 * ```
 *
 * ```kotlin
 * loadingStateView.showLoading()
 * loadingStateView.showEmpty()
 * loadingStateView.showError { // 重试回调 }
 * loadingStateView.hide()
 * ```
 */
class LoadingStateView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    /**
     * 加载状态枚举
     */
    enum class State {
        LOADING,
        EMPTY,
        ERROR,
        HIDDEN
    }

    private val binding: ViewLoadingStateBinding

    private var emptyIconRes: Int = R.mipmap.ic_loading_empty
    private var errorIconRes: Int = R.mipmap.ic_loading_error
    private var emptyText: String
    private var errorText: String
    private var loadingText: String
    private var actionButtonText: String
    private var emptyActionButtonText: String
    private var errorActionButtonText: String

    private var currentState: State = State.HIDDEN
    private var onActionClickListener: (() -> Unit)? = null

    init {
        binding = ViewLoadingStateBinding.inflate(LayoutInflater.from(context), this, true)

        // 默认文案
        emptyText = context.getString(R.string.loading_state_empty)
        errorText = context.getString(R.string.loading_state_error)
        loadingText = context.getString(R.string.loading_state_loading)
        actionButtonText = context.getString(R.string.loading_state_retry)
        emptyActionButtonText = actionButtonText
        errorActionButtonText = actionButtonText

        // 读取自定义属性
        context.obtainStyledAttributes(attrs, R.styleable.LoadingStateView).apply {
            emptyIconRes = getResourceId(R.styleable.LoadingStateView_emptyIcon, emptyIconRes)
            errorIconRes = getResourceId(R.styleable.LoadingStateView_errorIcon, errorIconRes)
            emptyText = getString(R.styleable.LoadingStateView_emptyText) ?: emptyText
            errorText = getString(R.styleable.LoadingStateView_errorText) ?: errorText
            loadingText = getString(R.styleable.LoadingStateView_loadingText) ?: loadingText

            val textColor = getColor(
                R.styleable.LoadingStateView_messageTextColor,
                ContextCompat.getColor(context, R.color.black_40)
            )
            binding.tvStateMessage.setTextColor(textColor)

            val textSize = getDimension(R.styleable.LoadingStateView_messageTextSize, 0f)
            if (textSize > 0) {
                binding.tvStateMessage.textSize = textSize / resources.displayMetrics.scaledDensity
            }

            // 按钮文案
            val buttonText = getString(R.styleable.LoadingStateView_actionButtonText)
            if (!buttonText.isNullOrEmpty()) {
                actionButtonText = buttonText
                emptyActionButtonText = buttonText
                errorActionButtonText = buttonText
            }
            getString(R.styleable.LoadingStateView_emptyActionButtonText)?.let {
                emptyActionButtonText = it
            }
            getString(R.styleable.LoadingStateView_errorActionButtonText)?.let {
                errorActionButtonText = it
            }

            recycle()
        }

        binding.btnAction.setOnClickListener {
            onActionClickListener?.invoke()
        }

        // 默认隐藏
        visibility = View.GONE
    }

    /**
     * 显示加载中状态
     */
    fun showLoading(message: String? = null) {
        currentState = State.LOADING
        visibility = View.VISIBLE

        binding.ivStateIcon.visibility = View.GONE
        binding.progressBar.visibility = View.VISIBLE
        binding.tvStateMessage.visibility = View.VISIBLE
        binding.tvStateMessage.text = message ?: loadingText
        binding.btnAction.visibility = View.GONE
    }

    /**
     * 显示空数据状态
     *
     * @param message 空状态提示信息
     * @param iconRes 自定义空状态图标
     * @param buttonText 自定义按钮文案，为 null 时使用默认文案
     * @param onAction 按钮点击回调，为 null 时隐藏按钮
     */
    fun showEmpty(
        message: String? = null,
        @DrawableRes iconRes: Int? = null,
        buttonText: String? = null,
        onAction: (() -> Unit)? = null
    ) {
        currentState = State.EMPTY
        visibility = View.VISIBLE

        binding.ivStateIcon.visibility = View.VISIBLE
        binding.ivStateIcon.setImageResource(iconRes ?: emptyIconRes)
        binding.progressBar.visibility = View.GONE
        binding.tvStateMessage.visibility = View.VISIBLE
        binding.tvStateMessage.text = message ?: emptyText

        onActionClickListener = onAction
        if (onAction != null) {
            binding.btnAction.text = buttonText ?: emptyActionButtonText
            binding.btnAction.visibility = View.VISIBLE
        } else {
            binding.btnAction.visibility = View.GONE
        }
    }

    /**
     * 显示加载失败状态
     *
     * @param message 错误信息
     * @param iconRes 自定义错误图标
     * @param buttonText 自定义按钮文案，为 null 时使用默认文案
     * @param onRetry 重试回调，为 null 时隐藏按钮
     */
    fun showError(
        message: String? = null,
        @DrawableRes iconRes: Int? = null,
        buttonText: String? = null,
        onRetry: (() -> Unit)? = null
    ) {
        currentState = State.ERROR
        visibility = View.VISIBLE

        binding.ivStateIcon.visibility = View.VISIBLE
        binding.ivStateIcon.setImageResource(iconRes ?: errorIconRes)
        binding.progressBar.visibility = View.GONE
        binding.tvStateMessage.visibility = View.VISIBLE
        binding.tvStateMessage.text = message ?: errorText

        onActionClickListener = onRetry
        if (onRetry != null) {
            binding.btnAction.text = buttonText ?: errorActionButtonText
            binding.btnAction.visibility = View.VISIBLE
        } else {
            binding.btnAction.visibility = View.GONE
        }
    }

    /**
     * 隐藏状态视图
     */
    fun hide() {
        currentState = State.HIDDEN
        visibility = View.GONE
    }

    /**
     * 获取当前状态
     */
    fun getCurrentState(): State = currentState

    /**
     * 是否正在显示
     */
    fun isShowing(): Boolean = currentState != State.HIDDEN

    /**
     * 设置空状态图标
     */
    fun setEmptyIcon(@DrawableRes iconRes: Int) {
        emptyIconRes = iconRes
        if (currentState == State.EMPTY) {
            binding.ivStateIcon.setImageResource(iconRes)
        }
    }

    /**
     * 设置错误状态图标
     */
    fun setErrorIcon(@DrawableRes iconRes: Int) {
        errorIconRes = iconRes
        if (currentState == State.ERROR) {
            binding.ivStateIcon.setImageResource(iconRes)
        }
    }

    /**
     * 设置空状态文案
     */
    fun setEmptyText(text: String) {
        emptyText = text
        if (currentState == State.EMPTY) {
            binding.tvStateMessage.text = text
        }
    }

    /**
     * 设置空状态文案（资源ID）
     */
    fun setEmptyText(@StringRes textRes: Int) {
        setEmptyText(context.getString(textRes))
    }

    /**
     * 设置错误状态文案
     */
    fun setErrorText(text: String) {
        errorText = text
        if (currentState == State.ERROR) {
            binding.tvStateMessage.text = text
        }
    }

    /**
     * 设置错误状态文案（资源ID）
     */
    fun setErrorText(@StringRes textRes: Int) {
        setErrorText(context.getString(textRes))
    }

    /**
     * 设置加载中文案
     */
    fun setLoadingText(text: String) {
        loadingText = text
        if (currentState == State.LOADING) {
            binding.tvStateMessage.text = text
        }
    }

    /**
     * 设置按钮文案（同时设置空状态和错误状态）
     */
    fun setActionButtonText(text: String) {
        actionButtonText = text
        emptyActionButtonText = text
        errorActionButtonText = text
        if (currentState == State.EMPTY || currentState == State.ERROR) {
            binding.btnAction.text = text
        }
    }

    /**
     * 设置空状态按钮文案
     */
    fun setEmptyActionButtonText(text: String) {
        emptyActionButtonText = text
        if (currentState == State.EMPTY) {
            binding.btnAction.text = text
        }
    }

    /**
     * 设置错误状态按钮文案
     */
    fun setErrorActionButtonText(text: String) {
        errorActionButtonText = text
        if (currentState == State.ERROR) {
            binding.btnAction.text = text
        }
    }

    /**
     * 设置按钮点击监听
     */
    fun setOnActionClickListener(listener: () -> Unit) {
        onActionClickListener = listener
    }
}
