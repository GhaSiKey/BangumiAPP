package com.example.bangumi.detail.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.bangumi.databinding.FragmentPointsBinding
import com.example.bangumi.detail.adapter.BangumiPointAdapter
import com.example.bangumi.detail.viewmodel.BangumiPointsState
import com.example.bangumi.detail.viewmodel.BangumiPointsViewModel

/**
 * Created by gaoshiqi
 * on 2025/6/22 21:04
 * email: gaoshiqi@bilibili.com
 */
class PointsFragment: Fragment() {

    companion object {
        private const val ARG_SUBJECT_ID = "subject_id"

        fun newInstance(tabType: Int): PointsFragment {
            return PointsFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_SUBJECT_ID, tabType)
                }
            }
        }
    }

    private val mSubjectId: Int by lazy {
        arguments?.getInt(ARG_SUBJECT_ID, 0)?: 0
    }
    private lateinit var mBinding: FragmentPointsBinding
    private val mAdapter: BangumiPointAdapter = BangumiPointAdapter()
    private val mViewModel: BangumiPointsViewModel by lazy {
        ViewModelProvider(requireActivity())[BangumiPointsViewModel::class.java]
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mBinding = FragmentPointsBinding.inflate(inflater, container, false)
        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mBinding.rvPoints.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = mAdapter
        }
        initObserver()

        mViewModel.loadPoints(mSubjectId)
    }

    private fun initObserver() {
        mViewModel.state.observe(viewLifecycleOwner) {state ->
            when (state) {
                is BangumiPointsState.LOADING -> showLoading()
                is BangumiPointsState.SUCCESS -> {
                    hideLoading()
                    if (state.data.isEmpty()) {
                        showEmpty()
                        return@observe
                    }
                    mAdapter.updateList(state.data)
                }
                is BangumiPointsState.ERROR -> showEmpty()
            }
        }
    }

    private fun showLoading() {
        mBinding.loadingView.visibility = View.VISIBLE
        mBinding.rvPoints.visibility = View.GONE
        mBinding.loadingView.text = "正在加载..."
    }

    private fun showEmpty() {
        mBinding.loadingView.visibility = View.VISIBLE
        mBinding.rvPoints.visibility = View.GONE
        mBinding.loadingView.text = "暂无数据"
    }

    private fun hideLoading() {
        mBinding.loadingView.visibility = View.GONE
        mBinding.rvPoints.visibility = View.VISIBLE
    }
}