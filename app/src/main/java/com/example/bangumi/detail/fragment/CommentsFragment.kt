package com.example.bangumi.detail.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bangumi.databinding.FragmentCommentsBinding
import com.example.bangumi.detail.adapter.BangumiCommentAdapter
import com.example.bangumi.detail.viewmodel.CommentsIntent
import com.example.bangumi.detail.viewmodel.CommentsState
import com.example.bangumi.detail.viewmodel.CommentsViewModel

/**
 * Created by gaoshiqi
 * on 2025/7/17 18:13
 * email: gaoshiqi@bilibili.com
 */
class CommentsFragment: Fragment() {

    companion object {
        private const val ARG_SUBJECT_ID = "subject_id"

        fun newInstance(tabType: Int): CommentsFragment {
            return CommentsFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_SUBJECT_ID, tabType)
                }
            }
        }
    }

    private val mSubjectId: Int by lazy {
        arguments?.getInt(ARG_SUBJECT_ID, 0)?: 0
    }
    private lateinit var mBinding: FragmentCommentsBinding
    private val mViewModel: CommentsViewModel by lazy {
        ViewModelProvider(this, CommentsViewModelFactory(mSubjectId))[CommentsViewModel::class.java]
    }
    private val mAdapter = BangumiCommentAdapter()
    // 是否正在加载更多
    private var isLoadingMore = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mBinding = FragmentCommentsBinding.inflate(inflater, container, false)
        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        mBinding.swipeRefreshLayout.setOnRefreshListener {
            mViewModel.handleIntent(CommentsIntent.Refresh)
        }

        mViewModel.state.observe(viewLifecycleOwner) { state ->
            when (state) {
                is CommentsState.Idle -> {
                }
                is CommentsState.Loading -> {
                    // 显示加载指示器
                    mBinding.swipeRefreshLayout.isRefreshing = true
                }
                is CommentsState.Error -> {
                    // 显示错误
                    mBinding.swipeRefreshLayout.isRefreshing = false
                    Toast.makeText(context, state.message, Toast.LENGTH_SHORT).show()
                    isLoadingMore = false
                }
                is CommentsState.Success -> {
                    // 更新数据
                    mBinding.swipeRefreshLayout.isRefreshing = false
                    mAdapter.submitList(state.data)
                    isLoadingMore = false
                }

                is CommentsState.LoadingMore -> {
                    // 显示加载更多指示器
                    isLoadingMore = true
                }

                is CommentsState.LoadMoreError -> {
                    // 加载更多错误
                    isLoadingMore = false
                    Toast.makeText(context, state.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun setupRecyclerView() {
        mBinding.recyclerView.adapter = mAdapter
        mBinding.recyclerView.layoutManager = LinearLayoutManager(context)

        // 添加滚动监听实现上拉加载
        mBinding.recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                val lastVisibleItem = layoutManager.findLastVisibleItemPosition()
                val totalItemCount = layoutManager.itemCount

                // 检查是否需要加载更多
                if (!isLoadingMore && lastVisibleItem >= totalItemCount - 5) {
                    mViewModel.handleIntent(CommentsIntent.LoadMore)
                }
            }
        })
    }
}

@Suppress("UNCHECKED_CAST")
class CommentsViewModelFactory(private val mSubjectId: Int): ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CommentsViewModel::class.java)) {
            return CommentsViewModel(mSubjectId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}