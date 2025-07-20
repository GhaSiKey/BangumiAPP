package com.example.bangumi.trending

import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bangumi.R
import com.example.bangumi.databinding.ActivityAnimeTrendingBinding

class AnimeTrendingActivity : AppCompatActivity() {
    private lateinit var mBinding: ActivityAnimeTrendingBinding
    private val mViewModel: AnimeTrendingViewModel by viewModels()
    private val mAdapter = AnimeTrendingAdapter()
    private var isLoadingMore = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        mBinding = ActivityAnimeTrendingBinding.inflate(layoutInflater)
        setContentView(mBinding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        initView()
        initObserve()
    }

    private fun initView() {
        mBinding.recyclerView.adapter = mAdapter
        mBinding.recyclerView.layoutManager = LinearLayoutManager(this)
    }

    private fun initObserve() {
        // 添加滚动监听实现上拉加载
        mBinding.recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                val lastVisibleItem = layoutManager.findLastVisibleItemPosition()
                val totalItemCount = layoutManager.itemCount

                // 检查是否需要加载更多
                if (!isLoadingMore && lastVisibleItem >= totalItemCount - 5) {
                    mViewModel.handleIntent(TrendingIntent.LoadMore)
                }
            }
        })

        mBinding.swipeRefreshLayout.setOnRefreshListener {
            mViewModel.handleIntent(TrendingIntent.Refresh)
        }

        mViewModel.state.observe(this) { state ->
            when (state) {
                is TrendingState.Idle -> {
                }
                is TrendingState.Error -> {
                    // 显示错误
                    mBinding.swipeRefreshLayout.isRefreshing = false
                    Toast.makeText(this, state.message, Toast.LENGTH_SHORT).show()
                    isLoadingMore = false
                }
                is TrendingState.LoadMore -> {
                    // 显示加载更多指示器
                    isLoadingMore = true
                }
                is TrendingState.LoadMoreError -> {
                    // 加载更多错误
                    isLoadingMore = false
                    Toast.makeText(this, state.message, Toast.LENGTH_SHORT).show()
                }
                is TrendingState.Loading -> {
                    // 显示加载指示器
                    mBinding.swipeRefreshLayout.isRefreshing = true
                }
                is TrendingState.Success -> {
                    // 更新数据
                    mBinding.swipeRefreshLayout.isRefreshing = false
                    mAdapter.submitList(state.data)
                    isLoadingMore = false
                }
            }
        }
    }


}