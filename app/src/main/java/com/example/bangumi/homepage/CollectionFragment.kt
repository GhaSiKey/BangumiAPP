package com.example.bangumi.homepage

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.GridLayoutManager
import com.example.bangumi.BangumiApplication
import com.example.bangumi.collection.adapter.CollectionAdapter
import com.example.bangumi.databinding.FragmentCollectionBinding
import com.example.room.AnimeMarkRepository
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
            lifecycle.repeatOnLifecycle(Lifecycle.State.CREATED) {
                mRepository.allAnimeMarks.collectLatest { animeList ->
                    mAdapter.submitList(animeList)
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}