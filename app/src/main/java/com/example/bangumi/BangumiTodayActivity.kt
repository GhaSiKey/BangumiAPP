package com.example.bangumi

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowInsets
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.bangumi.data.model.CalendarResponse
import com.example.bangumi.databinding.ActivityBangumiApiTestBinding
import com.example.bangumi.ui.BangumiAdapter
import com.example.bangumi.ui.CalendarViewModel
import com.example.bangumi.utils.BangumiUtils

class BangumiTodayActivity : AppCompatActivity() {
    private lateinit var mBinding: ActivityBangumiApiTestBinding
    private lateinit var viewModel: CalendarViewModel

    private val mAdapter: BangumiAdapter = BangumiAdapter()

    @SuppressLint("NewApi")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mBinding = ActivityBangumiApiTestBinding.inflate(layoutInflater)
        setContentView(mBinding.root)
        // 设置顶部 padding 以适配状态栏/导航栏
        mBinding.root.setOnApplyWindowInsetsListener { view, insets ->
            val topInset = insets.getInsets(WindowInsets.Type.statusBars()).top
            val bottomInset = insets.getInsets(WindowInsets.Type.navigationBars()).bottom
            view.setPadding(0, topInset, 0, bottomInset)
            insets
        }
        viewModel = ViewModelProvider(this)[CalendarViewModel::class.java]

        initView()
        initObserver()
        viewModel.loadCalendarData()
    }

    private fun initView() {
        mBinding.rvBangumiList.layoutManager = LinearLayoutManager(this@BangumiTodayActivity)
        mBinding.rvBangumiList.adapter = mAdapter

    }

    private fun initObserver() {
        viewModel.isLoading.observe(this) { isLoading ->
            if (isLoading) {
                mBinding.textView.text = "正在加载..."
                mBinding.textView.visibility = View.VISIBLE
                mBinding.rvBangumiList.visibility = View.GONE
            } else {
                Toast.makeText(this, "加载完成", Toast.LENGTH_SHORT).show()
                mBinding.textView.visibility = View.GONE
                mBinding.rvBangumiList.visibility = View.VISIBLE
            }
        }

        viewModel.errorMessage.observe(this) { errorMessage ->
            errorMessage?.let {
                Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
            }
        }

        viewModel.calendarData.observe(this) { calendarData ->
            calendarData?.let {
                handleResponse(it)
            }
        }
    }

    private fun handleResponse(response: List<CalendarResponse>) {
        response.forEach { day ->
            Log.d("BangumiTodayActivity", "星期: ${day.weekday?.cn}")
            day.items?.forEach { item ->
                Log.d("BangumiTodayActivity", "动画: ${item.nameCn} (${item.name})")
            }
        }

        val today = response.firstOrNull { it.weekday?.id == BangumiUtils.getTodayWeekdayId() }
        today?.let {
            mAdapter.setData(it.items ?: emptyList())
        }
    }
}