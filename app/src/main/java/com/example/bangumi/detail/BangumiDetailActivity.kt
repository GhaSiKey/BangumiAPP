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
import com.bumptech.glide.Glide
import com.example.bangumi.R
import com.example.bangumi.data.model.BangumiDetail
import com.example.bangumi.databinding.ActivityBangumiDetailBinding
import com.example.bangumi.utils.BangumiUtils

class BangumiDetailActivity : AppCompatActivity() {
    private lateinit var mBinding: ActivityBangumiDetailBinding
    private val mViewModel: BangumiDetailViewModel by viewModels()
    private var mSubjectId: Int = -1

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
        // 设置顶部 padding 以适配状态栏/导航栏
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
        // todo
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
                }
                is BangumiDetailState.ERROR -> {
                    showError(state.msg)
                }
                else -> {}
            }
        }
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