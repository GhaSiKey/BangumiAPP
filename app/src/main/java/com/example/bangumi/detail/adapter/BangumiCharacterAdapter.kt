package com.example.bangumi.detail.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.Target
import com.example.bangumi.data.bean.BangumiCharacter
import com.example.bangumi.utils.BangumiUtils

/**
 * Created by gaoshiqi
 * on 2025/6/22 15:48
 * email: gaoshiqi@bilibili.com
 */
class BangumiCharacterAdapter(
    private val context: Context,
    private val columnCount: Int
): RecyclerView.Adapter<BangumiCharacterAdapter.BangumiCharacterViewHolder>() {

    private var characters: List<BangumiCharacter> = emptyList()
    private val columnWidth: Int by lazy {
        val displayMetrics = context.resources.displayMetrics
        val screenWidth = displayMetrics.widthPixels
        val padding = (columnCount + 1) * 8 // 8dp padding between items
        (screenWidth - padding) / columnCount
    }

    fun updateList(list: List<BangumiCharacter>) {
        characters = list
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): BangumiCharacterViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(com.example.bangumi.R.layout.item_character, parent, false)
        return BangumiCharacterViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: BangumiCharacterViewHolder,
        position: Int
    ) {
        val character = characters[position]
        holder.bind(character, columnWidth)
    }

    override fun getItemCount(): Int {
        return characters.size
    }

    inner class BangumiCharacterViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        private val characterId: TextView = itemView.findViewById(com.example.bangumi.R.id.characterId)
        private val characterImage: ImageView = itemView.findViewById(com.example.bangumi.R.id.characterImage)
        private val characterName: TextView = itemView.findViewById(com.example.bangumi.R.id.characterName)
        private val characterActor: TextView = itemView.findViewById(com.example.bangumi.R.id.characterActor)

        fun bind(character: BangumiCharacter, columnWidth: Int) {
            characterId.text = "ID: " + character.id
            characterName.text = character.name
            val actor = character.actors.firstOrNull()
            if (actor != null) {
                characterActor.text = actor.name
            }

            characterImage.layoutParams.width = columnWidth
            characterImage.requestLayout()
            val imageUrl = character.images?.medium
            if (!imageUrl.isNullOrEmpty()) {
                Glide.with(itemView)
                    .load(imageUrl)
                    .placeholder(com.example.bangumi.R.drawable.ic_cover_placeholder_36)
                    .override(columnWidth, Target.SIZE_ORIGINAL)
                    .into(characterImage)
            }

            characterId.setOnClickListener {
                BangumiUtils.copyContentToClipboard(character.id.toString(), itemView.context)
                Toast.makeText(itemView.context, "已复制到剪贴板", Toast.LENGTH_SHORT).show()
            }
        }
    }
}