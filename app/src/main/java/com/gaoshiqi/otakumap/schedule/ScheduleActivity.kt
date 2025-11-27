package com.gaoshiqi.otakumap.schedule

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.WindowInsets
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.gaoshiqi.otakumap.databinding.ActivityScheduleBinding
import com.gaoshiqi.otakumap.schedule.adapter.SchedulePagerAdapter
import com.gaoshiqi.otakumap.utils.BangumiUtils
import com.google.android.material.tabs.TabLayoutMediator
import java.util.Calendar

class ScheduleActivity : AppCompatActivity() {
    private lateinit var mBinding: ActivityScheduleBinding
    private val viewModel: CalendarViewModel by viewModels()


    @SuppressLint("NewApi")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = ActivityScheduleBinding.inflate(layoutInflater)
        setContentView(mBinding.root)
        // 设置顶部 padding 以适配状态栏
        mBinding.root.setOnApplyWindowInsetsListener { view, insets ->
            val topInset = insets.getInsets(WindowInsets.Type.statusBars()).top
            val bottomInset = insets.getInsets(WindowInsets.Type.navigationBars()).bottom
            view.setPadding(0, topInset, 0, bottomInset)
            insets
        }


        initView()
        initObserver()

        viewModel.loadCalendarData()
    }

    private fun initView() {
        val mPagerAdapter = SchedulePagerAdapter(this)
        mBinding.viewPager.adapter = mPagerAdapter

        TabLayoutMediator(mBinding.tabLayout, mBinding.viewPager) {tab, position ->
            tab.text = BangumiUtils.getWeekdayName(position+1)
        }.attach()

        // 设置默认显示今天
        val calendar = Calendar.getInstance()
        var dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
        // 转换 (周日=1 -> 周一=2，周日=7)
        dayOfWeek = if (dayOfWeek == Calendar.SUNDAY) 7 else dayOfWeek - 1
        mBinding.viewPager.setCurrentItem(dayOfWeek - 1, false)
    }

    private fun initObserver() {
        viewModel.errorMessage.observe(this) { errorMessage ->
            errorMessage?.let {
                Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
            }
        }
    }
}