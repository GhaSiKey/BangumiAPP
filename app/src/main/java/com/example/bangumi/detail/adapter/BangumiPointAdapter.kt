package com.example.bangumi.detail.adapter

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
 * on 2025/6/22 21:46
 * email: gaoshiqi@bilibili.com
 */
class BangumiPointAdapter(
    private val OnItemClickListener: (LitePoint) -> Unit
): RecyclerView.Adapter<BangumiPointAdapter.BangumiPointViewHolder>() {
    private var points: List<LitePoint> = emptyList()

    fun updateList(list: List<LitePoint>) {
        points = list
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): BangumiPointViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(com.example.bangumi.R.layout.item_point, parent, false)
        return BangumiPointViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: BangumiPointViewHolder,
        position: Int
    ) {
        val point = points[position]
        holder.bind(point)
        holder.itemView.setOnClickListener {
            OnItemClickListener(point)
        }
    }

    override fun getItemCount(): Int {
        return points.size
    }


    inner class BangumiPointViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        private val pointImage: ImageView = itemView.findViewById(com.example.bangumi.R.id.pointImage)
        private val pointName: TextView = itemView.findViewById(com.example.bangumi.R.id.pointName)
        private val pointId: TextView = itemView.findViewById(com.example.bangumi.R.id.pointId)
        private val pointEp: TextView = itemView.findViewById(com.example.bangumi.R.id.pointEpisode)

        fun bind(point: LitePoint) {
            Glide.with(itemView.context)
                .load(point.image)
                .placeholder(com.example.bangumi.R.drawable.ic_cover_placeholder_36)
                .into(pointImage)
            pointName.text = point.name
            pointId.text = "ID: " + point.id
            setEpAndTimeText(pointEp, point.ep, point.s)
        }

        private fun setEpAndTimeText(textView: TextView, ep: String?, s: String?) {
            val epText = if (!ep.isNullOrEmpty()) "EP: $ep" else "EP: 未知"
            if (s != null) {
                val seconds = s.toIntOrNull()
                if (seconds != null) {
                    val minutes = seconds / 60
                    val seconds = seconds % 60
                    textView.text = epText + "  " + String.format("%02d:%02d", minutes, seconds)
                    return
                }
            }
            textView.text = epText
        }

    }

}