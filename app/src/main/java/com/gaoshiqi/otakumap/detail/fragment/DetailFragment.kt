package com.gaoshiqi.otakumap.detail.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.gaoshiqi.otakumap.data.bean.BangumiDetail
import com.gaoshiqi.otakumap.databinding.FragmentDetailBinding
import com.gaoshiqi.otakumap.databinding.ItemCollectionStatBinding
import com.gaoshiqi.otakumap.detail.viewmodel.BangumiDetailViewModel
import com.gaoshiqi.otakumap.detail.viewmodel.BangumiDetailState

/**
 * Created by gaoshiqi
 * on 2025/6/2 14:38
 * email: gaoshiqi@bilibili.com
 */
class DetailFragment: Fragment() {

    companion object {

        fun newInstance(): DetailFragment {
            return DetailFragment()
        }
    }

    private lateinit var mBinding: FragmentDetailBinding
    private val mViewModel: BangumiDetailViewModel by lazy {
        ViewModelProvider(requireActivity())[BangumiDetailViewModel::class.java]
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mBinding = FragmentDetailBinding.inflate(inflater, container, false)
        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initObserver()
    }

    private fun initObserver() {
        mViewModel.state.observe(viewLifecycleOwner) { state ->
            when (state) {
                is BangumiDetailState.LOADING -> {
                }
                is BangumiDetailState.SUCCESS -> {
                    showDetails(state.data)
                }
                is BangumiDetailState.ERROR -> {
                }
                else -> {}
            }
        }
    }

    private fun showDetails(detail: BangumiDetail) {
        // 简介
        mBinding.bangumiSummary.text = detail.summary?.ifEmpty { "暂无简介" } ?: "暂无简介"

        // 评分
        val score = detail.rating?.score
        if (score != null && score > 0) {
            mBinding.tvScore.text = String.format("%.1f", score)
            mBinding.progressScore.progress = (score * 10).toInt()
        } else {
            mBinding.tvScore.text = "暂无"
            mBinding.progressScore.progress = 0
        }

        // 排名
        val rank = detail.rating?.rank
        mBinding.tvRank.text = if (rank != null && rank > 0) "排名 #$rank" else "暂无排名"

        // 放送日期
        mBinding.tvAirDate.text = detail.date ?: "未定"

        // 话数
        val eps = detail.eps
        mBinding.tvEps.text = if (eps != null && eps > 0) "${eps}话" else "未知"

        // 收藏统计
        bindStatItem(mBinding.statWish, "想看", detail.collection?.wish ?: 0)
        bindStatItem(mBinding.statDoing, "在看", detail.collection?.doing ?: 0)
        bindStatItem(mBinding.statCollect, "看过", detail.collection?.collect ?: 0)
        bindStatItem(mBinding.statOnHold, "搁置", detail.collection?.onHold ?: 0)
        bindStatItem(mBinding.statDropped, "抛弃", detail.collection?.dropped ?: 0)
    }

    private fun bindStatItem(binding: ItemCollectionStatBinding, label: String, count: Int) {
        binding.tvLabel.text = label
        binding.tvCount.text = formatCount(count)
    }

    private fun formatCount(count: Int): String {
        return when {
            count >= 10000 -> String.format("%.1fw", count / 10000.0)
            count >= 1000 -> String.format("%.1fk", count / 1000.0)
            else -> count.toString()
        }
    }
}
