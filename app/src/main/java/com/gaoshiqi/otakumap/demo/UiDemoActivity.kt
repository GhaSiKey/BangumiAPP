package com.gaoshiqi.otakumap.demo

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.gaoshiqi.otakumap.databinding.ActivityUiDemoBinding

/**
 * UI Demo Activity - 用于展示和测试 Secondary Creation 卡片组件
 *
 * 测试 9 种布局情况：
 * - 1-3 张子卡片：单列垂直排列
 * - 4-5 张子卡片：两行横滑排列
 * - 6-9 张子卡片：三行横滑排列
 */
class UiDemoActivity : AppCompatActivity() {

    private lateinit var binding: ActivityUiDemoBinding
    private lateinit var adapter: SecondaryCreationAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUiDemoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupViews()
        loadTestData()
    }

    private fun setupViews() {
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }

        // 设置 RecyclerView
        adapter = SecondaryCreationAdapter(
            onItemClick = { item ->
                Toast.makeText(this, "点击: ${item.title}", Toast.LENGTH_SHORT).show()
            },
            onMoreClick = { item ->
                Toast.makeText(this, "更多: ${item.title}", Toast.LENGTH_SHORT).show()
            }
        )

        binding.rvDemo.apply {
            layoutManager = LinearLayoutManager(this@UiDemoActivity)
            adapter = this@UiDemoActivity.adapter
        }
    }

    private fun loadTestData() {
        // 生成 9 种测试数据（1-9 张子卡片）
        val testSections = TestDataGenerator.generateTestSections()
        adapter.submitList(testSections)

        binding.tvDataInfo.text = "展示 ${testSections.size} 种布局情况 (1-9 张子卡片)"
    }

    companion object {
        fun start(context: Context) {
            context.startActivity(Intent(context, UiDemoActivity::class.java))
        }
    }
}
