package com.example.bangumi.detail.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.bangumi.databinding.FragmentPointsBinding
import com.example.bangumi.detail.adapter.BangumiPointAdapter
import com.example.bangumi.detail.viewmodel.BangumiPointsState
import com.example.bangumi.detail.viewmodel.BangumiPointsViewModel
import com.example.map.MapActivity
import com.google.android.material.chip.Chip
import com.example.map.MapBottomSheetFragment
import com.example.map.data.LitePoint
import com.example.map.utils.PointListSingleton

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
    private val mAdapter: BangumiPointAdapter = BangumiPointAdapter() { point ->
        val geo = point.geo
        if (geo.size != 2) return@BangumiPointAdapter
        val fragment = MapBottomSheetFragment.newInstance(geo[0], geo[1], point.name)
        fragment.show(parentFragmentManager, "MapBottomSheetFragment")
    }
    private val mViewModel: BangumiPointsViewModel by lazy {
        ViewModelProvider(requireActivity())[BangumiPointsViewModel::class.java]
    }
    private var pointList: List<LitePoint> ?= null

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
        // 初始时隐藏集数选择器
        mBinding.episodeScrollView.visibility = View.GONE

        initObserver()
        mViewModel.loadPoints(mSubjectId)
    }

    private fun initObserver() {
        mViewModel.state.observe(viewLifecycleOwner) {state ->
            when (state) {
                is BangumiPointsState.LOADING -> showLoading()
                is BangumiPointsState.SUCCESS -> {
                    if (state.data.isEmpty()) {
                        showEmpty()
                        mBinding.watchAll.visibility = View.GONE
                        mBinding.episodeScrollView.visibility = View.GONE
                        return@observe
                    }
                    hideLoading()
                    mAdapter.updateList(state.data)
                    mBinding.watchAll.visibility = View.VISIBLE
                    mBinding.episodeScrollView.visibility = View.VISIBLE
                    pointList = mViewModel.getRawPoints()
                    updateEpisodeChips()
                }
                is BangumiPointsState.ERROR -> {
                    Toast.makeText(requireContext(), state.msg, Toast.LENGTH_SHORT).show()
                    mBinding.episodeScrollView.visibility = View.GONE
                    showEmpty()
                }
            }
        }

        mBinding.watchAll.setOnClickListener {
            if (pointList.isNullOrEmpty()) return@setOnClickListener
            PointListSingleton.setPointList(pointList!!)

            val intent = Intent(requireContext(), MapActivity::class.java)
            startActivity(intent)
        }
    }

    private fun showLoading() {
        mBinding.loadingStateView.showLoading()
        mBinding.rvPoints.visibility = View.GONE
    }

    private fun showEmpty() {
        mBinding.loadingStateView.showEmpty()
        mBinding.rvPoints.visibility = View.GONE
    }

    private fun hideLoading() {
        mBinding.loadingStateView.hide()
        mBinding.rvPoints.visibility = View.VISIBLE
    }

    private fun updateEpisodeChips() {
        val episodes = mViewModel.getEpisodeList()
        if (episodes.isEmpty()) return

        mBinding.chipGroupEpisodes.removeAllViews()

        episodes.forEachIndexed { index, episode ->
            val chip = Chip(requireContext()).apply {
                text = formatEpisodeDisplayName(episode)
                isCheckable = true
                isClickable = true
                
                // 为每个chip设置tag来标识对应的episode
                tag = episode
            }
            mBinding.chipGroupEpisodes.addView(chip)
        }
        
        // 设置ChipGroup的选中监听器
        mBinding.chipGroupEpisodes.setOnCheckedStateChangeListener { group, checkedIds ->
            if (checkedIds.isNotEmpty()) {
                val checkedChip = group.findViewById<Chip>(checkedIds[0])
                val episode = checkedChip.tag as String
                scrollToEpisode(episode)
            }
        }
        
        // 设置第一个Chip为默认选中
        if (episodes.isNotEmpty()) {
            mBinding.chipGroupEpisodes.check(mBinding.chipGroupEpisodes.getChildAt(0).id)
        }
    }

    private fun formatEpisodeDisplayName(episode: String): String {
        return when {
            episode == "其他" -> "其他"
            episode.matches("\\d+".toRegex()) -> "第${episode}集"
            else -> episode
        }
    }

    private fun scrollToEpisode(episode: String) {
        val position = mViewModel.getPositionForEpisode(episode)
        if (position != -1) {
            // 使用LinearLayoutManager将标题精确滑动到顶部
            (mBinding.rvPoints.layoutManager as? LinearLayoutManager)?.let { layoutManager ->
                layoutManager.scrollToPositionWithOffset(position, 0)
            }
        }
    }
    
}