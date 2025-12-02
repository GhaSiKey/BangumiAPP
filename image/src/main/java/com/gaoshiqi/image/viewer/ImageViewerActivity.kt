package com.gaoshiqi.image.viewer

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityOptionsCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import android.widget.FrameLayout
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.gaoshiqi.image.R
import com.gaoshiqi.image.databinding.ActivityImageViewerBinding

/**
 * 图片查看器 Activity
 * 支持全屏查看、双指缩放、双击缩放、共享元素转场动画
 */
class ImageViewerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityImageViewerBinding

    companion object {
        private const val EXTRA_IMAGE_URL = "extra_image_url"
        const val SHARED_ELEMENT_NAME = "shared_image"

        /**
         * 启动图片查看器（无共享元素）
         * @param context Context
         * @param imageUrl 图片 URL
         */
        fun start(context: Context, imageUrl: String) {
            val intent = Intent(context, ImageViewerActivity::class.java).apply {
                putExtra(EXTRA_IMAGE_URL, imageUrl)
            }
            context.startActivity(intent)
        }

        /**
         * 启动图片查看器（带共享元素转场动画）
         * @param activity Activity
         * @param imageUrl 图片 URL
         * @param sharedElement 共享元素 View
         */
        fun startWithSharedElement(activity: Activity, imageUrl: String, sharedElement: ImageView) {
            val intent = Intent(activity, ImageViewerActivity::class.java).apply {
                putExtra(EXTRA_IMAGE_URL, imageUrl)
            }
            val options = ActivityOptionsCompat.makeSceneTransitionAnimation(
                activity,
                sharedElement,
                SHARED_ELEMENT_NAME
            )
            activity.startActivity(intent, options.toBundle())
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityImageViewerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupSharedElement()
        setupFullscreen()
        setupViews()
        loadImage()
    }

    private fun setupSharedElement() {
        // 设置共享元素的 transitionName
        ViewCompat.setTransitionName(binding.zoomableImageView, SHARED_ELEMENT_NAME)
        // 延迟转场动画直到图片加载完成
        supportPostponeEnterTransition()
    }

    private fun setupFullscreen() {
        // 沉浸式布局，状态栏保持可见
        WindowCompat.setDecorFitsSystemWindows(window, false)

        // 让关闭按钮避开状态栏
        ViewCompat.setOnApplyWindowInsetsListener(binding.btnClose) { view, insets ->
            val statusBarHeight = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top
            view.updateLayoutParams<FrameLayout.LayoutParams> {
                topMargin = statusBarHeight + 8
            }
            insets
        }
    }

    private fun setupViews() {
        binding.btnClose.setOnClickListener {
            finishWithTransition()
        }

        binding.zoomableImageView.setOnClickListener {
            finishWithTransition()
        }
    }

    private fun finishWithTransition() {
        supportFinishAfterTransition()
    }

    private fun loadImage() {
        val imageUrl = intent.getStringExtra(EXTRA_IMAGE_URL)
        if (imageUrl.isNullOrEmpty()) {
            showError()
            supportStartPostponedEnterTransition()
            return
        }

        binding.progressBar.visibility = View.VISIBLE

        Glide.with(this)
            .load(imageUrl)
            .listener(object : RequestListener<Drawable> {
                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any?,
                    target: Target<Drawable>,
                    isFirstResource: Boolean
                ): Boolean {
                    binding.progressBar.visibility = View.GONE
                    showError()
                    // 加载失败也要启动转场，否则界面会卡住
                    supportStartPostponedEnterTransition()
                    return false
                }

                override fun onResourceReady(
                    resource: Drawable,
                    model: Any,
                    target: Target<Drawable>?,
                    dataSource: DataSource,
                    isFirstResource: Boolean
                ): Boolean {
                    binding.progressBar.visibility = View.GONE
                    // 图片加载完成，启动转场动画
                    supportStartPostponedEnterTransition()
                    return false
                }
            })
            .into(binding.zoomableImageView)
    }

    private fun showError() {
        Toast.makeText(this, R.string.image_load_failed, Toast.LENGTH_SHORT).show()
    }
}
