package com.example.bangumi.detail

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.bangumi.detail.fragment.DetailFragment

/**
 * Created by gaoshiqi
 * on 2025/6/2 14:53
 * email: gaoshiqi@bilibili.com
 */
class BangumiDetailPagerAdapter(
    fragmentActivity: FragmentActivity
): FragmentStateAdapter(fragmentActivity) {
    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> DetailFragment.newInstance(0) // 简介页面
            1 -> DetailFragment.newInstance(1) // 详情页面
            else -> throw IllegalArgumentException("Invalid position")
        }
    }

    override fun getItemCount(): Int = 2

    fun getPageTitle(position: Int): String {
        return when (position) {
            0 -> "简介"
            1 -> "详情"
            else -> ""
        }
    }
}