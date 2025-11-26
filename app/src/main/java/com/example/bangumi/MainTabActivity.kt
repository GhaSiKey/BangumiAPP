package com.example.bangumi

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.lifecycleScope
import com.example.bangumi.databinding.ActivityMainTabBinding
import com.example.bangumi.homepage.CollectionFragment
import com.example.bangumi.homepage.RankingFragment
import com.example.bangumi.homepage.ScheduleFragment
import com.example.bangumi.homepage.SettingsFragment
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainTabActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityMainTabBinding
    
    private var collectionFragment: CollectionFragment? = null
    private var rankingFragment: RankingFragment? = null
    private var scheduleFragment: ScheduleFragment? = null
    private var settingsFragment: SettingsFragment? = null
    
    private var currentFragment: Fragment? = null

    private var isAppReady = false // 控制条件
    
    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        // 2. （可选）设置保持条件，等待数据加载
        splashScreen.setKeepOnScreenCondition { !isAppReady }

        super.onCreate(savedInstanceState)
        binding = ActivityMainTabBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        
        setupBottomNavigation()
        
        if (savedInstanceState == null) {
            showFragment(0)
        }

        // 3. （可选）在数据加载完成后，移除启动屏
        loadData()
    }

    private fun loadData() {
        // 例如进行网络请求、初始化SDK等
        lifecycleScope.launch {
            delay(1000) // 模拟耗时操作
            isAppReady = true // 数据加载完成，允许进入主界面
        }
    }
    
    private fun setupBottomNavigation() {
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_ranking -> {
                    showFragment(0)
                    true
                }
                R.id.nav_collection -> {
                    showFragment(1)
                    true
                }
                R.id.nav_schedule -> {
                    showFragment(2)
                    true
                }
                R.id.nav_settings -> {
                    showFragment(3)
                    true
                }
                else -> false
            }
        }
    }
    
    private fun showFragment(index: Int) {
        val transaction: FragmentTransaction = supportFragmentManager.beginTransaction()
        
        currentFragment?.let { fragment ->
            transaction.hide(fragment)
        }
        
        val targetFragment = when (index) {
            0 -> {
                if (rankingFragment == null) {
                    rankingFragment = RankingFragment()
                    transaction.add(R.id.fragment_container, rankingFragment!!)
                }
                rankingFragment
            }
            1 -> {
                if (collectionFragment == null) {
                    collectionFragment = CollectionFragment()
                    transaction.add(R.id.fragment_container, collectionFragment!!)
                }
                collectionFragment
            }
            2 -> {
                if (scheduleFragment == null) {
                    scheduleFragment = ScheduleFragment()
                    transaction.add(R.id.fragment_container, scheduleFragment!!)
                }
                scheduleFragment
            }
            3 -> {
                if (settingsFragment == null) {
                    settingsFragment = SettingsFragment()
                    transaction.add(R.id.fragment_container, settingsFragment!!)
                }
                settingsFragment
            }
            else -> null
        }
        
        targetFragment?.let { fragment ->
            transaction.show(fragment)
            currentFragment = fragment
        }
        
        transaction.commitAllowingStateLoss()
    }
}