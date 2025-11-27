package com.gaoshiqi.otakumap.schedule.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.gaoshiqi.otakumap.schedule.DailyFragment

/**
 * Created by gaoshiqi
 * on 2025/5/30 17:31
 * email: gaoshiqi@bilibili.com
 */
class SchedulePagerAdapter(
    fragmentActivity: FragmentActivity
): FragmentStateAdapter(fragmentActivity) {
    override fun createFragment(position: Int): Fragment {
        val weekdayId = position + 1
        return DailyFragment.Companion.newInstance(weekdayId)
    }

    override fun getItemCount(): Int = 7
}