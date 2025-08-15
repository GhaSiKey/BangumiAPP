package com.example.bangumi.search

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.TextWatcher
import android.text.Editable
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bangumi.R
import com.example.bangumi.databinding.ActivitySearchBinding
import com.example.bangumi.schedule.adapter.BangumiAdapter
import com.example.bangumi.utils.KeyboardUtils

class SearchActivity : AppCompatActivity() {
    private lateinit var mBinding: ActivitySearchBinding
    private val mViewModel: SearchViewModel by viewModels()
    private val mAdapter = BangumiAdapter()
    private var isLoadingMore = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        mBinding = ActivitySearchBinding.inflate(layoutInflater)
        setContentView(mBinding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        initView()
        initObserver()
    }

    private fun initView() {
        mBinding.searchBar.requestFocus()
        mBinding.rvSearchResult.adapter = mAdapter
        mBinding.rvSearchResult.layoutManager = LinearLayoutManager(this)

        setupSearchBar()

        Handler(Looper.getMainLooper()).postDelayed({
            KeyboardUtils.showSoftKeyboard(this, mBinding.searchBar)
        }, 300)
    }

    private fun setupSearchBar() {
        // 设置键盘回车监听
        mBinding.searchBar.setOnEditorActionListener { _, actionId, event ->
            when (actionId) {
                EditorInfo.IME_ACTION_SEARCH -> {
                    performSearch()
                    true
                }
                else -> {
                    // 处理硬件键盘回车
                    if (event?.action == KeyEvent.ACTION_DOWN && event.keyCode == KeyEvent.KEYCODE_ENTER) {
                        performSearch()
                        true
                    } else {
                        false
                    }
                }
            }
        }

        // 添加文本变化监听（可选：实时搜索或验证）
        mBinding.searchBar.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            
            override fun afterTextChanged(s: Editable?) {
                // 可以在这里添加实时搜索或者清空结果的逻辑
                val query = s?.toString()?.trim()
                if (query.isNullOrEmpty()) {
                    // 当搜索框为空时显示空状态
                    showEmpty()
                }
            }
        })
    }

    private fun performSearch() {
        val query = mBinding.searchBar.text?.toString()?.trim()
        if (!query.isNullOrBlank()) {
            mViewModel.handleIntent(SearchIntent.Search(query))
            mBinding.searchBar.clearFocus()
            KeyboardUtils.hideSoftKeyboard(this, mBinding.searchBar)
        } else {
            Toast.makeText(this, "请输入搜索内容", Toast.LENGTH_SHORT).show()
        }
    }

    private fun initObserver() {
        mBinding.rvSearchResult.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(
                recyclerView: RecyclerView,
                dx: Int,
                dy: Int
            ) {
                super.onScrolled(recyclerView, dx, dy)

                val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                val lastVisibleItem = layoutManager.findLastVisibleItemPosition()
                val totalItemCount = layoutManager.itemCount
                if (!isLoadingMore && lastVisibleItem >= totalItemCount - 2) {
                    mViewModel.handleIntent(SearchIntent.LoadMore)
                }
            }
        })

        mBinding.tvSearch.setOnClickListener {
            performSearch()
        }

        mBinding.action.setOnClickListener {
            onBackPressed()
        }

        mViewModel.state.observe(this) { state ->
            when (state) {
                is SearchState.Error -> {
                    Toast.makeText(this, state.message, Toast.LENGTH_LONG).show()
                    isLoadingMore = false
                    showEmpty()
                }
                SearchState.Idle -> {
                    showEmpty()
                }
                SearchState.LoadMore -> {
                    isLoadingMore = true
                }
                is SearchState.LoadMoreError -> {
                    Toast.makeText(this, state.message, Toast.LENGTH_LONG).show()
                    isLoadingMore = false
                }
                SearchState.Loading -> {
                    mAdapter.setData(emptyList())
                    showLoading()
                }
                is SearchState.Success -> {
                    mAdapter.setData(state.data)
                    isLoadingMore = false
                    hideLoading()
                }
            }
        }
    }

    private fun showLoading() {
        mBinding.loadingView.visibility = View.VISIBLE
        mBinding.rvSearchResult.visibility = View.GONE
        mBinding.loadingView.text = "正在加载..."
    }

    private fun showEmpty() {
        mBinding.loadingView.visibility = View.VISIBLE
        mBinding.rvSearchResult.visibility = View.GONE
        mBinding.loadingView.text = "暂无数据"
    }

    private fun hideLoading() {
        mBinding.loadingView.visibility = View.GONE
        mBinding.rvSearchResult.visibility = View.VISIBLE
    }


}