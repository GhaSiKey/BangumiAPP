package com.gaoshiqi.otakumap.detail.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.gaoshiqi.otakumap.data.bean.CommentData
import com.gaoshiqi.otakumap.databinding.ItemCommentBinding
import com.gaoshiqi.otakumap.utils.BangumiUtils

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

        @SuppressLint("SetTextI18n")
        fun bind(item: CommentData) {
            mBinding.userName.text = item.user.nickname
            mBinding.subInfo.text = BangumiUtils.getCollectionStatus(item.type) + " "+ BangumiUtils.formatTimeByInterval(item.updatedAt)
            mBinding.comment.text = item.comment

            Glide.with(mBinding.userAvatar.context)
                .load(item.user.avatar.large)
                .placeholder(com.gaoshiqi.otakumap.R.drawable.ic_cover_placeholder_36)
                .into(mBinding.userAvatar)
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