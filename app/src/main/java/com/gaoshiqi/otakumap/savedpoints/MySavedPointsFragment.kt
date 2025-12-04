package com.gaoshiqi.otakumap.savedpoints

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.gaoshiqi.map.MapBottomSheetFragment
import com.gaoshiqi.map.data.LitePoint
import com.gaoshiqi.otakumap.BangumiApplication
import com.gaoshiqi.otakumap.R
import com.gaoshiqi.otakumap.databinding.FragmentMySavedPointsBinding
import com.gaoshiqi.otakumap.detail.BangumiDetailActivity
import com.gaoshiqi.room.SavedPointEntity
import com.gaoshiqi.room.SavedPointRepository
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class MySavedPointsFragment : Fragment() {

    private var _binding: FragmentMySavedPointsBinding? = null
    private val binding get() = _binding!!

    private lateinit var mRepository: SavedPointRepository
    private lateinit var mAdapter: SavedPointGroupAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMySavedPointsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRepository()
        setupRecyclerView()
        initData()
    }

    private fun setupRepository() {
        mRepository = (requireActivity().application as BangumiApplication).savedPointRepository
        mAdapter = SavedPointGroupAdapter(
            onGoDetailClick = { subjectId ->
                // 跳转到番剧详情页
                BangumiDetailActivity.start(requireContext(), subjectId)
            },
            onPointClick = { point ->
                // 弹出地图弹窗
                showMapBottomSheet(point)
            }
        )
    }

    private fun setupRecyclerView() {
        binding.recyclerViewSavedPoints.adapter = mAdapter
        binding.recyclerViewSavedPoints.layoutManager = LinearLayoutManager(requireContext())
    }

    private fun initData() {
        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                mRepository.allSavedPoints.collectLatest { points ->
                    if (points.isEmpty()) {
                        showEmpty()
                    } else {
                        hideLoadingState()
                        // 按番剧分组
                        val groups = groupBySubject(points)
                        mAdapter.submitList(groups)
                    }
                }
            }
        }
    }

    private fun groupBySubject(points: List<SavedPointEntity>): List<SavedPointGroup> {
        return points.groupBy { it.subjectId }
            .map { (subjectId, subjectPoints) ->
                val first = subjectPoints.first()
                SavedPointGroup(
                    subjectId = subjectId,
                    subjectName = first.subjectName,
                    subjectCover = first.subjectCover,
                    points = subjectPoints
                )
            }
            .sortedByDescending { group ->
                // 按最新收藏时间排序
                group.points.maxOfOrNull { it.savedTime } ?: 0L
            }
    }

    private fun showMapBottomSheet(point: SavedPointEntity) {
        // 将 SavedPointEntity 转换为 LitePoint
        val litePoint = LitePoint(
            id = point.pointId,
            cn = point.pointNameCn,
            name = point.pointName,
            image = point.pointImage,
            ep = point.episode,
            s = point.timeInEpisode,
            geo = listOf(point.lat, point.lng),
            subjectId = point.subjectId,
            subjectName = point.subjectName,
            subjectCover = point.subjectCover
        )
        MapBottomSheetFragment.show(parentFragmentManager, litePoint)
    }

    private fun showEmpty() {
        binding.loadingStateView.showEmpty(
            buttonText = getString(R.string.saved_points_empty_action)
        ) {
            navigateToRanking()
        }
        binding.recyclerViewSavedPoints.visibility = View.GONE
    }

    private fun hideLoadingState() {
        binding.loadingStateView.hide()
        binding.recyclerViewSavedPoints.visibility = View.VISIBLE
    }

    private fun navigateToRanking() {
        findNavController().navigate(R.id.nav_ranking)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
