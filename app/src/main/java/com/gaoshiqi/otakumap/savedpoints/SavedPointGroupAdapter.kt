package com.gaoshiqi.otakumap.savedpoints

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.gaoshiqi.otakumap.R
import com.gaoshiqi.otakumap.databinding.ItemSavedPointGroupBinding
import com.gaoshiqi.room.SavedPointEntity

/**
 * 番剧分组数据
 */
data class SavedPointGroup(
    val subjectId: Int,
    val subjectName: String,
    val subjectCover: String,
    val points: List<SavedPointEntity>
)

/**
 * 番剧分组列表适配器
 */
class SavedPointGroupAdapter(
    private val onGoDetailClick: (Int) -> Unit,
    private val onPointClick: (SavedPointEntity) -> Unit
) : ListAdapter<SavedPointGroup, SavedPointGroupAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemSavedPointGroupBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(
        private val binding: ItemSavedPointGroupBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        private val pointAdapter = SavedPointAdapter { point ->
            onPointClick(point)
        }

        init {
            binding.rvPoints.adapter = pointAdapter
        }

        fun bind(group: SavedPointGroup) {
            // 番剧名称
            binding.tvSubjectName.text = group.subjectName

            // 地点数量
            binding.tvPointCount.text = binding.root.context.getString(
                R.string.point_count_format, group.points.size
            )

            // 跳转详情按钮
            binding.btnGoDetail.setOnClickListener {
                onGoDetailClick(group.subjectId)
            }

            // 地点列表
            pointAdapter.submitList(group.points)
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<SavedPointGroup>() {
        override fun areItemsTheSame(oldItem: SavedPointGroup, newItem: SavedPointGroup): Boolean {
            return oldItem.subjectId == newItem.subjectId
        }

        override fun areContentsTheSame(oldItem: SavedPointGroup, newItem: SavedPointGroup): Boolean {
            return oldItem == newItem
        }
    }
}
