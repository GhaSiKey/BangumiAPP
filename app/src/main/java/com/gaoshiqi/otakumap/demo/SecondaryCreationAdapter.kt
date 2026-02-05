package com.gaoshiqi.otakumap.demo

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.gaoshiqi.otakumap.databinding.ItemSecondaryCreationBinding

/**
 * Secondary Creation 列表适配器
 */
class SecondaryCreationAdapter(
    private val onItemClick: (ChildCardData) -> Unit,
    private val onMoreClick: (ChildCardData) -> Unit
) : ListAdapter<SecondaryCreationSection, SecondaryCreationViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SecondaryCreationViewHolder {
        val binding = ItemSecondaryCreationBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return SecondaryCreationViewHolder(
            binding = binding,
            onItemClick = onItemClick,
            onMoreClick = onMoreClick
        )
    }

    override fun onBindViewHolder(holder: SecondaryCreationViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class DiffCallback : DiffUtil.ItemCallback<SecondaryCreationSection>() {
        override fun areItemsTheSame(
            oldItem: SecondaryCreationSection,
            newItem: SecondaryCreationSection
        ): Boolean {
            return oldItem.sectionTitle == newItem.sectionTitle &&
                    oldItem.items.size == newItem.items.size
        }

        override fun areContentsTheSame(
            oldItem: SecondaryCreationSection,
            newItem: SecondaryCreationSection
        ): Boolean {
            return oldItem == newItem
        }
    }
}
