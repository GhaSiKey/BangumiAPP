package com.gaoshiqi.otakumap.homepage

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.GridLayoutManager
import androidx.navigation.fragment.findNavController
import com.gaoshiqi.otakumap.BangumiApplication
import com.gaoshiqi.otakumap.R
import com.gaoshiqi.otakumap.collection.adapter.CollectionAdapter
import com.gaoshiqi.otakumap.databinding.FragmentCollectionBinding
import com.gaoshiqi.room.AnimeMarkRepository
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class CollectionFragment : Fragment() {

    private var _binding: FragmentCollectionBinding? = null
    private val binding get() = _binding!!

    private lateinit var mRepository: AnimeMarkRepository
    private lateinit var mAdapter: CollectionAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentCollectionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRepository()
        setupRecyclerView()
        initData()
    }

    private fun setupRepository() {
        mRepository = (requireActivity().application as BangumiApplication).animeMarkRepository
        mAdapter = CollectionAdapter(requireContext())
    }

    private fun setupRecyclerView() {
        binding.recyclerViewCollection.adapter = mAdapter
        binding.recyclerViewCollection.layoutManager = GridLayoutManager(requireContext(), 3)
    }

    private fun initData() {
        lifecycleScope.launch {
            // 使用 STARTED 状态：在前台时收集数据，切到后台时暂停
            // 这样返回页面时能自动恢复数据监听，及时更新收藏列表
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                mRepository.allAnimeMarks.collectLatest { animeList ->
                    if (animeList.isEmpty()) {
                        showEmpty()
                    } else {
                        hideLoadingState()
                        mAdapter.submitList(animeList)
                    }
                }
            }
        }
    }

    private fun showEmpty() {
        binding.loadingStateView.showEmpty(
            buttonText = getString(R.string.collection_empty_action)
        ) {
            navigateToRanking()
        }
        binding.recyclerViewCollection.visibility = View.GONE
    }

    private fun hideLoadingState() {
        binding.loadingStateView.hide()
        binding.recyclerViewCollection.visibility = View.VISIBLE
    }

    private fun navigateToRanking() {
        findNavController().navigate(R.id.nav_ranking)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}