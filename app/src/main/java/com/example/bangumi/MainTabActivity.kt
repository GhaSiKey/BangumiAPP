package com.example.bangumi

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import com.example.bangumi.databinding.ActivityMainTabBinding
import com.example.bangumi.homepage.CollectionFragment
import com.example.bangumi.homepage.RankingFragment
import com.example.bangumi.homepage.ScheduleFragment
import com.example.bangumi.homepage.SettingsFragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainTabActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityMainTabBinding
    
    private var collectionFragment: CollectionFragment? = null
    private var rankingFragment: RankingFragment? = null
    private var scheduleFragment: ScheduleFragment? = null
    private var settingsFragment: SettingsFragment? = null
    
    private var currentFragment: Fragment? = null
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainTabBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupBottomNavigation()
        
        if (savedInstanceState == null) {
            showFragment(0)
        }
    }
    
    private fun setupBottomNavigation() {
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_collection -> {
                    showFragment(0)
                    true
                }
                R.id.nav_ranking -> {
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
                if (collectionFragment == null) {
                    collectionFragment = CollectionFragment()
                    transaction.add(R.id.fragment_container, collectionFragment!!)
                }
                collectionFragment
            }
            1 -> {
                if (rankingFragment == null) {
                    rankingFragment = RankingFragment()
                    transaction.add(R.id.fragment_container, rankingFragment!!)
                }
                rankingFragment
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