package com.example.bangumi.collection

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.GridLayoutManager
import com.example.bangumi.BangumiApplication
import com.example.bangumi.R
import com.example.bangumi.collection.adapter.CollectionAdapter
import com.example.bangumi.databinding.ActivityMyCollectionBinding
import com.gaoshiqi.room.AnimeMarkRepository
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class MyCollectionActivity : AppCompatActivity() {
    private lateinit var mBinding: ActivityMyCollectionBinding
    private lateinit var mRepository: AnimeMarkRepository
    private lateinit var mAdapter: CollectionAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        mBinding = ActivityMyCollectionBinding.inflate(layoutInflater)
        setContentView(mBinding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        mRepository = (application as BangumiApplication).animeMarkRepository
        mAdapter = CollectionAdapter(this)

        mBinding.recyclerView.adapter = mAdapter
        mBinding.recyclerView.layoutManager = GridLayoutManager(this, 3)

        initData()
    }

    private fun initData() {
        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(androidx.lifecycle.Lifecycle.State.CREATED) {
                mRepository.allAnimeMarks.collectLatest { animeList ->
                    mAdapter.submitList(animeList)
                }
            }
        }
    }


}