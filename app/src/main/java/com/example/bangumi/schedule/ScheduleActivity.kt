package com.example.bangumi.schedule

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.view.WindowInsets
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import com.example.bangumi.R
import com.example.bangumi.databinding.ActivityScheduleBinding
import com.example.bangumi.schedule.CalendarViewModel
import com.example.bangumi.schedule.adapter.SchedulePagerAdapter
import com.example.bangumi.utils.BangumiUtils
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