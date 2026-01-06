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
import com.gaoshiqi.otakumap.R

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
        val noSummary = getString(R.string.detail_no_summary)
        mBinding.bangumiSummary.text = detail.summary?.ifEmpty { noSummary } ?: noSummary

        // 评分
        val score = detail.rating?.score
        if (score != null && score > 0) {
            mBinding.tvScore.text = String.format("%.1f", score)
            mBinding.progressScore.progress = (score * 10).toInt()
        } else {
            mBinding.tvScore.text = getString(R.string.detail_no_score)
            mBinding.progressScore.progress = 0
        }

        // 排名
        val rank = detail.rating?.rank
        mBinding.tvRank.text = if (rank != null && rank > 0) getString(R.string.detail_rank_format, rank) else getString(R.string.detail_no_rank)

        // 放送日期
        mBinding.tvAirDate.text = detail.date ?: getString(R.string.detail_air_date_unknown)

        // 话数
        val eps = detail.eps
        mBinding.tvEps.text = if (eps != null && eps > 0) getString(R.string.detail_eps_format, eps) else getString(R.string.detail_eps_unknown)

        // 收藏统计
        bindStatItem(mBinding.statWish, getString(R.string.collection_wish), detail.collection?.wish ?: 0)
        bindStatItem(mBinding.statDoing, getString(R.string.collection_doing), detail.collection?.doing ?: 0)
        bindStatItem(mBinding.statCollect, getString(R.string.collection_collect), detail.collection?.collect ?: 0)
        bindStatItem(mBinding.statOnHold, getString(R.string.collection_on_hold), detail.collection?.onHold ?: 0)
        bindStatItem(mBinding.statDropped, getString(R.string.collection_dropped), detail.collection?.dropped ?: 0)
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
