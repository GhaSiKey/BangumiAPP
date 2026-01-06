package com.gaoshiqi.otakumap.detail.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.gaoshiqi.otakumap.detail.fragment.CharactersFragment
import com.gaoshiqi.otakumap.detail.fragment.CommentsFragment
import com.gaoshiqi.otakumap.detail.fragment.DetailFragment
import com.gaoshiqi.otakumap.detail.fragment.PointsFragment
import com.gaoshiqi.otakumap.R

/**
 * Created by gaoshiqi
 * on 2025/6/2 14:53
 * email: gaoshiqi@bilibili.com
 */
class BangumiDetailPagerAdapter(
    private val fragmentActivity: FragmentActivity,
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
            0 -> fragmentActivity.getString(R.string.tab_detail)
            1 -> fragmentActivity.getString(R.string.tab_characters)
            2 -> fragmentActivity.getString(R.string.tab_comments)
            3 -> fragmentActivity.getString(R.string.tab_points)
            else -> ""
        }
    }
}