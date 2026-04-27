package com.gaoshiqi.map.widget

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.recyclerview.widget.RecyclerView
import com.gaoshiqi.image.loadCover
import com.gaoshiqi.map.R
import com.gaoshiqi.map.data.LitePoint
import com.gaoshiqi.map.databinding.ItemLitePointBinding
import com.gaoshiqi.map.utils.GoogleMapUtils

class LitePointAdapter(
    private val points: List<LitePoint>
) : RecyclerView.Adapter<LitePointAdapter.LitePointViewHolder>() {

    var onBookmarkClick: ((LitePoint, Int) -> Unit)? = null

    private val bookmarkStates = mutableMapOf<Int, Boolean>()

    inner class LitePointViewHolder(
        private val binding: ItemLitePointBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(litePoint: LitePoint, position: Int) {
            binding.itemCover.loadCover(litePoint.image, R.drawable.placeholder_landscape)
            binding.itemTitle.text = litePoint.displayName()

            val subtitle = buildSubtitle(litePoint)
            binding.itemSubtitle.text = subtitle

            updateBookmarkIcon(binding.btnBookmark, bookmarkStates[position] == true)

            binding.btnOpenMaps.setOnClickListener {
                if (litePoint.geo.size >= 2) {
                    GoogleMapUtils.openInGoogleMaps(
                        itemView.context,
                        litePoint.geo[0],
                        litePoint.geo[1],
                        litePoint.displayName()
                    )
                }
            }

            binding.btnBookmark.setOnClickListener {
                onBookmarkClick?.invoke(litePoint, position)
            }
        }

        private fun buildSubtitle(point: LitePoint): String {
            val parts = mutableListOf<String>()
            if (point.subjectName.isNotEmpty()) {
                parts.add(point.subjectName)
            }
            val episodeTime = point.formatEpisodeTime()
            if (episodeTime.isNotEmpty()) {
                parts.add(episodeTime)
            }
            return parts.joinToString(" · ")
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LitePointViewHolder {
        val binding = ItemLitePointBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return LitePointViewHolder(binding)
    }

    override fun onBindViewHolder(holder: LitePointViewHolder, position: Int) {
        holder.bind(points[position], position)
    }

    override fun getItemCount() = points.size

    fun updateBookmarkState(position: Int, isSaved: Boolean) {
        bookmarkStates[position] = isSaved
        notifyItemChanged(position)
    }

    fun setBookmarkStates(states: Map<Int, Boolean>) {
        bookmarkStates.clear()
        bookmarkStates.putAll(states)
        notifyDataSetChanged()
    }

    private fun updateBookmarkIcon(button: ImageButton, isSaved: Boolean) {
        button.setImageResource(
            if (isSaved) R.drawable.ic_bookmark_filled
            else R.drawable.ic_bookmark_border
        )
    }
}
