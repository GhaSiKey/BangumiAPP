package com.example.bangumi.ui.schedule

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.bangumi.databinding.FragmentScheduleBinding
import com.example.bangumi.schedule.CalendarViewModel
import com.example.bangumi.schedule.adapter.SchedulePagerAdapter
import com.example.bangumi.utils.BangumiUtils
import com.google.android.material.tabs.TabLayoutMediator
import java.util.Calendar

class ScheduleFragment : Fragment() {
    
    private var _binding: FragmentScheduleBinding? = null
    private val binding get() = _binding!!
    
    private val viewModel: CalendarViewModel by lazy {
        ViewModelProvider(requireActivity())[CalendarViewModel::class.java]
    }
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentScheduleBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    @SuppressLint("NewApi")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        initView()
        initObserver()
        
        viewModel.loadCalendarData()
    }
    
    private fun initView() {
        val mPagerAdapter = SchedulePagerAdapter(requireActivity())
        binding.viewPager.adapter = mPagerAdapter
        
        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = BangumiUtils.getWeekdayName(position + 1)
        }.attach()
        
        val calendar = Calendar.getInstance()
        var dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
        dayOfWeek = if (dayOfWeek == Calendar.SUNDAY) 7 else dayOfWeek - 1
        binding.viewPager.setCurrentItem(dayOfWeek - 1, false)
    }
    
    private fun initObserver() {
        viewModel.errorMessage.observe(viewLifecycleOwner) { errorMessage ->
            errorMessage?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}