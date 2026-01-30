package com.gaoshiqi.otakumap

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.gaoshiqi.otakumap.collection.MyCollectionActivity
import com.gaoshiqi.otakumap.databinding.ActivityTestBinding
import com.gaoshiqi.otakumap.schedule.BangumiTodayActivity
import com.gaoshiqi.otakumap.schedule.ScheduleActivity
import com.gaoshiqi.otakumap.search.SearchActivity
import com.gaoshiqi.otakumap.search.SearchOldTestActivity
import com.gaoshiqi.otakumap.search.SearchTestActivity
import com.gaoshiqi.otakumap.trending.AnimeTrendingActivity
import com.gaoshiqi.map.MapsDemoActivity
import com.gaoshiqi.otakumap.collection.v2.CollectionV2Activity
import com.gaoshiqi.player.PlayerTestActivity
import com.gaoshiqi.camera.CameraActivity

class TestActivity : AppCompatActivity() {

    private val mBinding: ActivityTestBinding by lazy {
        ActivityTestBinding.inflate(layoutInflater)
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

        mBinding.button2.setOnClickListener {
            val intent = Intent(this, MapsDemoActivity::class.java)
            startActivity(intent)
        }

        mBinding.button3.setOnClickListener {
            val intent = Intent(this, MyCollectionActivity::class.java)
            startActivity(intent)
        }

        mBinding.button4.setOnClickListener {
            val intent = Intent(this, AnimeTrendingActivity::class.java)
            startActivity(intent)
        }

        mBinding.button5.setOnClickListener {
            val intent = Intent(this, SearchActivity::class.java)
            startActivity(intent)
        }

        mBinding.button6.setOnClickListener {
            SearchTestActivity.start(this)
        }

        mBinding.button7.setOnClickListener {
            SearchOldTestActivity.start(this)
        }

        mBinding.buttonPlayer.setOnClickListener {
            PlayerTestActivity.start(this)
        }

        mBinding.buttonCollectionV2.setOnClickListener {
            CollectionV2Activity.start(this)
        }

        mBinding.buttonCamera.setOnClickListener {
            CameraActivity.start(this)
        }
    }
}