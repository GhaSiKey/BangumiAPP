package com.example.bangumi.detail.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.example.bangumi.databinding.FragmentCharactersBinding
import com.example.bangumi.detail.adapter.BangumiCharacterAdapter
import com.example.bangumi.detail.model.BangumiCharacterState
import com.example.bangumi.detail.model.BangumiCharactersViewModel

/**
 * Created by gaoshiqi
 * on 2025/6/9 17:59
 * email: gaoshiqi@bilibili.com
 */
class CharactersFragment: Fragment() {

    companion object {

        private const val ARG_SUBJECT_ID = "subject_id"
        private const val COLUMN_COUNT = 3

        fun newInstance(tabType: Int): CharactersFragment {
            return CharactersFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_SUBJECT_ID, tabType)
                }
            }
        }
    }

    private val mSubjectId: Int by lazy {
        arguments?.getInt(ARG_SUBJECT_ID, 0) ?: 0
    }
    private lateinit var mBinding: FragmentCharactersBinding
    private val mAdapter: BangumiCharacterAdapter by lazy {
        BangumiCharacterAdapter(requireContext(), COLUMN_COUNT)
    }
    private val mViewModel: BangumiCharactersViewModel by lazy {
        ViewModelProvider(requireActivity())[BangumiCharactersViewModel::class.java]
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mBinding = FragmentCharactersBinding.inflate(inflater, container, false)
        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mBinding.recyclerView.apply {
            layoutManager = StaggeredGridLayoutManager(COLUMN_COUNT, StaggeredGridLayoutManager.VERTICAL)
            adapter = mAdapter
        }
        mViewModel.loadCharacters(mSubjectId)
        initObserver()
    }

    private fun initObserver() {
        mViewModel.state.observe(viewLifecycleOwner) { state ->
            when (state) {
                is BangumiCharacterState.LOADING -> {
                    showLoading()
                }
                is BangumiCharacterState.SUCCESS -> {
                    hideLoading()
                    if (state.data.isEmpty()) {
                        showEmpty()
                        return@observe
                    }
                    mAdapter.updateList(state.data)
                }
                is BangumiCharacterState.ERROR -> {
                    showEmpty()
                }
                else -> {}
            }
        }
    }

    private fun showLoading() {
        mBinding.recyclerView.visibility = View.GONE
        mBinding.loadingView.visibility = View.VISIBLE
        mBinding.loadingView.text = "正在加载..."
    }

    private fun showEmpty() {
        mBinding.recyclerView.visibility = View.GONE
        mBinding.loadingView.visibility = View.VISIBLE
        mBinding.loadingView.text = "暂无数据"
    }

    private fun hideLoading() {
        mBinding.recyclerView.visibility = View.VISIBLE
        mBinding.loadingView.visibility = View.GONE
    }
}