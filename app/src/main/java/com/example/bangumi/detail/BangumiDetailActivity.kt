package com.example.bangumi.detail

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.WindowInsets
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.bangumi.BangumiApplication
import com.example.bangumi.R
import com.example.bangumi.data.bean.BangumiDetail
import com.example.bangumi.databinding.ActivityBangumiDetailBinding
import com.example.bangumi.detail.adapter.BangumiDetailPagerAdapter
import com.example.bangumi.detail.viewmodel.BangumiDetailIntent
import com.example.bangumi.detail.viewmodel.BangumiDetailState
import com.example.bangumi.detail.viewmodel.BangumiDetailViewModel
import com.example.bangumi.utils.BangumiUtils
import com.example.room.AnimeEntity
import com.example.room.AnimeMarkRepository
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.coroutines.launch

class BangumiDetailActivity : AppCompatActivity() {
    private lateinit var mBinding: ActivityBangumiDetailBinding
    private val mViewModel: BangumiDetailViewModel by viewModels()
    private var mSubjectId: Int = -1
    private lateinit var repository: AnimeMarkRepository

    companion object {
        const val ARG_SUBJECT_ID = "subject_id"

        fun start(context: Context, subjectId: Int) {
            val intent = Intent(context, BangumiDetailActivity::class.java).apply {
                putExtra(ARG_SUBJECT_ID, subjectId)
            }
            context.startActivity(intent)
        }
    }

    @SuppressLint("NewApi")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = ActivityBangumiDetailBinding.inflate(layoutInflater)
        setContentView(mBinding.root)
        // 设置顶部 padding 以适配状态栏
        mBinding.root.setOnApplyWindowInsetsListener { view, insets ->
            val topInset = insets.getInsets(WindowInsets.Type.statusBars()).top
            val bottomInset = insets.getInsets(WindowInsets.Type.navigationBars()).bottom
            view.setPadding(0, topInset, 0, bottomInset)
            insets
        }

        // 获取传入的subjectID
        mSubjectId = intent.getIntExtra(ARG_SUBJECT_ID, 0)
        if (mSubjectId == 0) {
            Toast.makeText(application, "无效的番剧ID", Toast.LENGTH_SHORT).show()
            finish()
        }

        initView()
        initObserver()
        mViewModel.setSubjectId(mSubjectId)
    }

    private fun initView() {
        val adapter = BangumiDetailPagerAdapter(this, mSubjectId)
        mBinding.viewPager.adapter = adapter
        TabLayoutMediator(mBinding.tabLayout, mBinding.viewPager) { tab, position ->
            tab.text = adapter.getPageTitle(position)
        }.attach()
    }

    private fun initObserver() {
        mViewModel.state.observe(this) { state ->
            when (state) {
                is BangumiDetailState.LOADING -> {
                    showLoading()
                }
                is BangumiDetailState.SUCCESS -> {
                    hideLoading()
                    showBangumiDetail(state.data)

                    repository = (application as BangumiApplication).animeMarkRepository
                    setupMarkButton(state.data)
                }
                is BangumiDetailState.ERROR -> {
                    showError(state.msg)
                }
                else -> {}
            }
        }
    }

    private fun setupMarkButton(data: BangumiDetail) {
        lifecycleScope.launch {
            val isMarked = repository.isBookmarked(mSubjectId)
            updateMarkButton(isMarked)

            mBinding.btnMark.setOnClickListener {
                lifecycleScope.launch {
                    if (isMarked) {
                        // 取消关注
                        repository.removeAnimeMark(AnimeEntity(
                            id = mSubjectId,
                            name = data.name,
                            nameCn = data.nameCn,
                            imageUrl = data.images.large
                        ))
                        updateMarkButton(false)
                        Toast.makeText(this@BangumiDetailActivity, "已取消收藏", Toast.LENGTH_SHORT).show()
                    } else {
                        repository.addAnimeMark(AnimeEntity(
                            id = mSubjectId,
                            name = data.name,
                            nameCn = data.nameCn,
                            imageUrl = data.images.large
                        ))
                        updateMarkButton(true)
                        Toast.makeText(this@BangumiDetailActivity, "已收藏", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun updateMarkButton(isMarked: Boolean) {
        mBinding.btnMark.text = if (isMarked) "已收藏" else "收藏"
    }


    private fun showBangumiDetail(data: BangumiDetail) {
        // 标题
        mBinding.bangumiTitle.text = data.displayTitle()
        // 封面
        Glide.with(this)
            .load(data.images.large)
            .centerCrop()
            .placeholder(com.example.bangumi.R.drawable.ic_cover_placeholder_36)
            .into(mBinding.ivCover)
        // 信息
        mBinding.bangumiAirDate.text = data.date
        mBinding.bangumiScoreCount.text = BangumiUtils.convertCount(data.rating?.total ?: 0) + " 人打分："
        mBinding.bangumiScore.text = data.rating?.score?.toString()?: "0.0"
        // 设置标签
        val tagList = BangumiUtils.getTags(data)
        mBinding.bangumiTags.setTags(tagList)

    }


    private fun showLoading() {
        mBinding.loadingView.visibility = View.VISIBLE
        mBinding.detailContainer.visibility = View.GONE

        mBinding.loadingView.text = getString(R.string.loading)
    }

    private fun hideLoading() {
        mBinding.loadingView.visibility = View.GONE
        mBinding.detailContainer.visibility = View.VISIBLE
    }

    private fun showError(msg: String) {
        mBinding.loadingView.visibility = View.VISIBLE
        mBinding.detailContainer.visibility = View.GONE

        mBinding.loadingView.text = msg + "\n点击重试"
        mBinding.loadingView.setOnClickListener {
            mViewModel.processIntent(BangumiDetailIntent.Retry)
        }
    }
}