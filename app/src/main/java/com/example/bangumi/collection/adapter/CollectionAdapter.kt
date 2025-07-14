package com.example.bangumi.collection.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.ViewTreeObserver
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.bangumi.databinding.ItemMyCollectionBinding
import com.example.bangumi.detail.BangumiDetailActivity
import com.example.room.AnimeEntity


/**
 * Created by gaoshiqi
 * on 2025/7/14 18:09
 * email: gaoshiqi@bilibili.com
 */
class CollectionAdapter(
    private val context: Context
): ListAdapter<AnimeEntity, CollectionAdapter.VH>(CollectionDiffCallback()) {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): VH {
        val binding = ItemMyCollectionBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VH(binding)
    }

    override fun onBindViewHolder(
        holder: VH,
        position: Int
    ) {
        val anime = getItem(position)
        holder.bind(anime)
    }


    inner class VH(private val mBinding: ItemMyCollectionBinding) : RecyclerView.ViewHolder(mBinding.root) {
        fun bind(item: AnimeEntity) {
            mBinding.title.text = item.name

            mBinding.cover.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
                override fun onGlobalLayout() {
                    mBinding.cover.viewTreeObserver.removeOnGlobalLayoutListener(this)
                    val width = mBinding.cover.measuredWidth
                    val height = width * 7 / 5
                    val layoutParams = mBinding.cover.layoutParams
                    layoutParams.height = height
                    mBinding.cover.layoutParams = layoutParams
                }
            })

            Glide.with(mBinding.cover.context)
                .load(item.imageUrl)
                .placeholder(com.example.bangumi.R.drawable.ic_cover_placeholder_36)
                .into(mBinding.cover)

            mBinding.root.setOnClickListener {
                BangumiDetailActivity.start(context, item.id)
            }
        }
    }

    private class CollectionDiffCallback : DiffUtil.ItemCallback<AnimeEntity>() {
        override fun areItemsTheSame(oldItem: AnimeEntity, newItem: AnimeEntity): Boolean {
            return oldItem.id == newItem.id
        }
        override fun areContentsTheSame(oldItem: AnimeEntity, newItem: AnimeEntity): Boolean {
            return oldItem == newItem
        }
    }
}