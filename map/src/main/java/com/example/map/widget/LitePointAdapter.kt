package com.example.map.widget

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.map.data.LitePoint

/**
 * Created by gaoshiqi
 * on 2025/7/11 17:57
 * email: gaoshiqi@bilibili.com
 */
class LitePointAdapter(
    private val points: List<LitePoint>
): RecyclerView.Adapter<LitePointAdapter.LitePointViewHolder>() {

    inner class LitePointViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val cover: ImageView = itemView.findViewById(com.example.map.R.id.item_cover)
        private val title: TextView = itemView.findViewById(com.example.map.R.id.item_title)
        private val description: TextView = itemView.findViewById(com.example.map.R.id.item_description)

        fun bind(litePoint: LitePoint) {
            Glide.with(itemView.context)
                .load(litePoint.image)
                .placeholder(com.example.map.R.drawable.ic_cover_placeholder_36)
                .into(cover)

            title.text = litePoint.name
            description.text = "坐标: ${litePoint.geo[0]}, ${litePoint.geo[1]}"
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LitePointViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(com.example.map.R.layout.item_lite_point, parent, false)
        return LitePointViewHolder(view)
    }

    override fun onBindViewHolder(holder: LitePointViewHolder, position: Int) {
        holder.bind(points[position])
    }

    override fun getItemCount() = points.size
}