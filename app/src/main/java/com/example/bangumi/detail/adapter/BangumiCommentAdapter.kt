package com.example.bangumi.detail.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.bangumi.data.bean.CommentData
import com.example.bangumi.databinding.ItemCommentBinding

/**
 * Created by gaoshiqi
 * on 2025/7/17 18:53
 * email: gaoshiqi@bilibili.com
 */
class BangumiCommentAdapter: ListAdapter<CommentData, BangumiCommentAdapter.CommentViewHolder>(
    CommentDiffCallback()) {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): CommentViewHolder {
        val binding = ItemCommentBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return CommentViewHolder(binding)
    }

    override fun onBindViewHolder(
        holder: CommentViewHolder,
        position: Int
    ) {
        holder.bind(getItem(position))
    }

    inner class CommentViewHolder(
        private val mBinding: ItemCommentBinding
    ): RecyclerView.ViewHolder(mBinding.root) {

        fun bind(item: CommentData) {
            mBinding.userName.text = item.user.username
            mBinding.comment.text = item.comment
        }
    }
}

class CommentDiffCallback: DiffUtil.ItemCallback<CommentData>() {
    override fun areItemsTheSame(
        oldItem: CommentData,
        newItem: CommentData
    ): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(
        oldItem: CommentData,
        newItem: CommentData
    ): Boolean {
        return oldItem == newItem
    }

}