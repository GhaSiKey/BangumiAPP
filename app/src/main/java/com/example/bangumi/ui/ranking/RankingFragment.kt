package com.example.bangumi.ui.ranking

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bangumi.databinding.FragmentRankingBinding
import com.example.bangumi.trending.AnimeTrendingAdapter
import com.example.bangumi.trending.AnimeTrendingViewModel
import com.example.bangumi.trending.TrendingIntent
import com.example.bangumi.trending.TrendingState

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