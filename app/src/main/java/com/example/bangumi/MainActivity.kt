package com.example.bangumi

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.bangumi.databinding.ActivityMainBinding
import com.example.bangumi.schedule.BangumiTodayActivity
import com.example.bangumi.schedule.ScheduleActivity

class MainActivity : AppCompatActivity() {

    private val mBinding: ActivityMainBinding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(mBinding.root)

        initView()
        initEvent()
    }

    private fun initView() {
    }

    private fun initEvent() {
        mBinding.button.setOnClickListener {
            val intent = Intent(this, BangumiTodayActivity::class.java)
            startActivity(intent)
        }

        mBinding.button1.setOnClickListener {
            val intent = Intent(this, ScheduleActivity::class.java)
            startActivity(intent)
        }
    }
}