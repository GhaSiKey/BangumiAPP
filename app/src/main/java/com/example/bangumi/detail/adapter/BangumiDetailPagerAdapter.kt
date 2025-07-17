package com.example.bangumi.detail.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.bangumi.detail.fragment.CharactersFragment
import com.example.bangumi.detail.fragment.CommentsFragment
import com.example.bangumi.detail.fragment.DetailFragment
import com.example.bangumi.detail.fragment.PointsFragment

/**
 * Created by gaoshiqi
 * on 2025/6/2 14:53
 * email: gaoshiqi@bilibili.com
 */
class BangumiDetailPagerAdapter(
    fragmentActivity: FragmentActivity,
    private val mSubjectId: Int
): FragmentStateAdapter(fragmentActivity) {
    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> DetailFragment.newInstance() // 简介页面
            1 -> CharactersFragment.newInstance(mSubjectId) // 角色页面
            2 -> CommentsFragment.newInstance(mSubjectId)   // 吐槽箱
            3 -> PointsFragment.newInstance(mSubjectId) // 圣地巡礼页面
            else -> throw IllegalArgumentException("Invalid position")
        }
    }

    override fun getItemCount(): Int = 4

    fun getPageTitle(position: Int): String {
        return when (position) {
            0 -> "简介"
            1 -> "角色"
            2 -> "吐槽箱"
            3 -> "圣地巡礼"
            else -> ""
        }
    }
}