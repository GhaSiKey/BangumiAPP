package com.gaoshiqi.otakumap.homepage

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.gaoshiqi.otakumap.databinding.FragmentRankingBinding
import com.gaoshiqi.otakumap.search.SearchActivity
import com.gaoshiqi.otakumap.trending.AnimeTrendingAdapter
import com.gaoshiqi.otakumap.trending.AnimeTrendingViewModel
import com.gaoshiqi.otakumap.trending.TrendingIntent
import com.gaoshiqi.otakumap.trending.TrendingState

class RankingFragment : Fragment() {

    private var _binding: FragmentRankingBinding? = null
    private val binding get() = _binding!!

    private val mViewModel: AnimeTrendingViewModel by lazy {
        ViewModelProvider(requireActivity())[AnimeTrendingViewModel::class.java]
    }
    private val mAdapter = AnimeTrendingAdapter()
    private var isLoadingMore = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentRankingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initView()
        initObserve()
    }

    private fun initView() {
        binding.recyclerViewRanking.adapter = mAdapter
        binding.recyclerViewRanking.layoutManager = LinearLayoutManager(requireContext())

        binding.btnSearch.setOnClickListener {
            startActivity(Intent(requireContext(), SearchActivity::class.java))
        }
    }

    private fun initObserve() {
        binding.recyclerViewRanking.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                val lastVisibleItem = layoutManager.findLastVisibleItemPosition()
                val totalItemCount = layoutManager.itemCount

                if (!isLoadingMore && lastVisibleItem >= totalItemCount - 5) {
                    mViewModel.handleIntent(TrendingIntent.LoadMore)
                }
            }
        })

        binding.swipeRefreshLayout.setOnRefreshListener {
            mViewModel.handleIntent(TrendingIntent.Refresh)
        }

        mViewModel.state.observe(viewLifecycleOwner) { state ->
            when (state) {
                is TrendingState.Idle -> {
                }
                is TrendingState.Error -> {
                    binding.swipeRefreshLayout.isRefreshing = false
                    Toast.makeText(requireContext(), state.message, Toast.LENGTH_SHORT).show()
                    isLoadingMore = false
                }
                is TrendingState.LoadMore -> {
                    isLoadingMore = true
                }
                is TrendingState.LoadMoreError -> {
                    isLoadingMore = false
                    Toast.makeText(requireContext(), state.message, Toast.LENGTH_SHORT).show()
                }
                is TrendingState.Loading -> {
                    binding.swipeRefreshLayout.isRefreshing = true
                }
                is TrendingState.Success -> {
                    binding.swipeRefreshLayout.isRefreshing = false
                    mAdapter.submitList(state.data)
                    isLoadingMore = false
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}