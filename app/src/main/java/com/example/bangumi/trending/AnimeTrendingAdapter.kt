package com.example.bangumi.trending

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.bangumi.data.bean.TrendingSubjectItem
import com.example.bangumi.databinding.ItemTrendingCardBinding
import com.example.bangumi.detail.BangumiDetailActivity
import com.example.bangumi.utils.BangumiUtils

class AnimeTrendingAdapter: ListAdapter<TrendingSubjectItem, TrendingViewHolder>(
    TrendingDiffCallback()) {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): TrendingViewHolder {
        val mBinding = ItemTrendingCardBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return TrendingViewHolder(mBinding)
    }

    override fun onBindViewHolder(
        holder: TrendingViewHolder,
        position: Int
    ) {
        holder.bind(position, getItem(position))
    }
}

class TrendingViewHolder(
    private val mBinding: ItemTrendingCardBinding
): RecyclerView.ViewHolder(mBinding.root) {
    @SuppressLint("SetTextI18n")
    fun bind(position: Int, item: TrendingSubjectItem) {
        Glide.with(mBinding.ivCover.context)
            .load(item.subject.images.large)
            .centerCrop()
            .placeholder(com.example.bangumi.R.drawable.ic_cover_placeholder_36)
            .into(mBinding.ivCover)
        if ((position + 1) < 100) {
            mBinding.tvRank.visibility = android.view.View.VISIBLE
            mBinding.tvRank.text = (position + 1).toString()
        }
        mBinding.tvTitle.text = item.subject.name
        mBinding.tvTitleCn.text = item.subject.nameCN
        mBinding.tvSid.text = "ID: " + item.subject.id
        mBinding.tvInfo.text = item.subject.info
        mBinding.tvScore.text = item.subject.rating.score.toString()
        mBinding.tvScoreCount.text = BangumiUtils.convertCount(item.subject.rating.total) + "人打分"

        mBinding.tvSid.setOnClickListener {
            BangumiUtils.copyContentToClipboard(item.subject.id.toString(), mBinding.tvSid.context)
            Toast.makeText(mBinding.tvSid.context, "已复制到剪贴板", Toast.LENGTH_SHORT).show()
        }

        mBinding.root.setOnClickListener {
            BangumiDetailActivity.start(mBinding.root.context, item.subject.id)
        }
    }
}

class TrendingDiffCallback: DiffUtil.ItemCallback<TrendingSubjectItem>() {
    override fun areItemsTheSame(
        oldItem: TrendingSubjectItem,
        newItem: TrendingSubjectItem
    ): Boolean {
        return oldItem.subject.id == newItem.subject.id
    }

    override fun areContentsTheSame(
        oldItem: TrendingSubjectItem,
        newItem: TrendingSubjectItem
    ): Boolean {
        return oldItem == newItem
    }
}