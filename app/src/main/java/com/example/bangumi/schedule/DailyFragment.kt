package com.example.bangumi.schedule

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.bangumi.databinding.FragmentDailyBinding
import com.example.bangumi.ui.BangumiAdapter
import com.example.bangumi.ui.CalendarViewModel

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
                mAdapter.setData(it.items ?: emptyList())
            }
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            if (isLoading) {
                mBinding.tv.text = "正在加载..."
                mBinding.tv.visibility = View.VISIBLE
                mBinding.recyclerView.visibility = View.GONE
            } else {
                mBinding.tv.visibility = View.GONE
                mBinding.recyclerView.visibility = View.VISIBLE
            }
        }
    }
}