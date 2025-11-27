package com.gaoshiqi.otakumap.schedule

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.gaoshiqi.otakumap.databinding.FragmentDailyBinding
import com.gaoshiqi.otakumap.schedule.adapter.BangumiAdapter

/**
 * Created by gaoshiqi
 * on 2025/5/30 17:32
 * email: gaoshiqi@bilibili.com
 */
class DailyFragment: Fragment() {

    companion object {
        private const val ARG_WEEKDAY_ID = "weekday_id"

        fun newInstance(weekdayId: Int): DailyFragment {
            return DailyFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_WEEKDAY_ID, weekdayId)
                }
            }
        }
    }

    private lateinit var mBinding: FragmentDailyBinding
    private val viewModel: CalendarViewModel by lazy {
        ViewModelProvider(requireActivity())[CalendarViewModel::class.java]
    }
    private var mWeekdayId: Int = 0
    private val mAdapter: BangumiAdapter = BangumiAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            mWeekdayId = it.getInt(ARG_WEEKDAY_ID)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mBinding = FragmentDailyBinding.inflate(inflater, container, false)
        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initView()
        initObserver()
    }

    private fun initView() {
        mBinding.recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = mAdapter
        }
    }

    private fun initObserver() {
        viewModel.calendarData.observe(viewLifecycleOwner) { response ->
            val today = response.firstOrNull { it.weekday?.id == mWeekdayId }
            today?.let {
                val items = it.items ?: emptyList()
                if (items.isEmpty()) {
                    showEmpty()
                } else {
                    hideLoadingState()
                    mAdapter.setData(items)
                }
            }
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            if (isLoading) {
                showLoading()
            }
        }
    }

    private fun showLoading() {
        mBinding.loadingStateView.showLoading()
        mBinding.recyclerView.visibility = View.GONE
    }

    private fun showEmpty() {
        mBinding.loadingStateView.showEmpty()
        mBinding.recyclerView.visibility = View.GONE
    }

    private fun hideLoadingState() {
        mBinding.loadingStateView.hide()
        mBinding.recyclerView.visibility = View.VISIBLE
    }
}