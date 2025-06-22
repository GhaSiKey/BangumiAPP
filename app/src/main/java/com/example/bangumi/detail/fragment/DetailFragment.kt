package com.example.bangumi.detail.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.bangumi.data.model.BangumiDetail
import com.example.bangumi.databinding.FragmentDetailBinding
import com.example.bangumi.detail.model.BangumiDetailViewModel
import com.example.bangumi.detail.model.BangumiDetailState

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
        mBinding.bangumiSummary.text = detail.summary

        val more = buildString {
            append("放送日期: ${detail.date ?: "未定"}\n")
            append("话数: ${detail.eps ?: "未知"}\n\n")
            append("评分: ${detail.rating?.score ?: "暂无"}\n")
            append("排名: ${detail.rating?.rank ?: "暂无"}\n\n")
            append("想看: ${detail.collection?.wish ?: 0}\n")
            append("看过: ${detail.collection?.collect ?: 0}\n")
            append("在看: ${detail.collection?.doing ?: 0}\n")
            append("搁置: ${detail.collection?.onHold ?: 0}\n")
            append("抛弃: ${detail.collection?.dropped ?: 0}\n\n")
        }
        mBinding.bangumiMore.text = more
    }
}