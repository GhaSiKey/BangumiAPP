package com.gaoshiqi.map.widget

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
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

    // 集数到颜色的映射（由外部设置）
    private var episodeColorMap: Map<String, Int> = emptyMap()

    inner class LitePointViewHolder(
        private val binding: ItemLitePointBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(litePoint: LitePoint, position: Int) {
            binding.itemCover.loadCover(litePoint.image, R.drawable.placeholder_landscape)
            binding.itemTitle.text = litePoint.displayName()

            // 设置集数角标
            setupEpisodeBadge(litePoint)

            // 副标题只显示番剧名和时间
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

        private fun setupEpisodeBadge(point: LitePoint) {
            val ep = point.ep
            if (!ep.isNullOrBlank() && ep != "null") {
                binding.itemEpisodeBadge.visibility = android.view.View.VISIBLE
                binding.itemEpisodeBadge.text = formatEpisode(ep)

                // 设置角标背景颜色，与地图标记颜色一致
                val normalizedEp = normalizeEpisode(ep)
                val colorIndex = episodeColorMap[normalizedEp] ?: 0
                val hue = getMarkerHue(colorIndex)
                val color = hsvToRgb(hue)

                // 创建带圆角和透明度的背景
                val drawable = GradientDrawable().apply {
                    shape = GradientDrawable.RECTANGLE
                    cornerRadius = 12f * binding.root.context.resources.displayMetrics.density
                    setColor(addAlpha(color, 0.9f))
                }
                binding.itemEpisodeBadge.background = drawable
            } else {
                binding.itemEpisodeBadge.visibility = android.view.View.GONE
            }
        }

        private fun formatEpisode(ep: String): String {
            return when {
                ep.matches("\\d+".toRegex()) -> "EP$ep"
                else -> ep
            }
        }

        private fun normalizeEpisode(ep: String?): String {
            return when {
                ep.isNullOrBlank() || ep == "null" -> "其他"
                ep.matches("\\d+".toRegex()) -> ep
                else -> ep
            }
        }

        private fun getMarkerHue(index: Int): Float {
            val hues = floatArrayOf(
                0f,    // RED
                210f,  // AZURE
                120f,  // GREEN
                30f,   // ORANGE
                270f,  // VIOLET
                180f,  // CYAN
                300f,  // MAGENTA
                60f,   // YELLOW
                330f,  // ROSE
                240f,  // BLUE
            )
            return hues[index % hues.size]
        }

        private fun hsvToRgb(hue: Float): Int {
            val hsv = floatArrayOf(hue, 0.8f, 0.9f)
            return Color.HSVToColor(hsv)
        }

        private fun addAlpha(color: Int, alpha: Float): Int {
            val alphaInt = (alpha * 255).toInt().coerceIn(0, 255)
            return Color.argb(alphaInt, Color.red(color), Color.green(color), Color.blue(color))
        }

        private fun buildSubtitle(point: LitePoint): String {
            val parts = mutableListOf<String>()
            if (point.subjectName.isNotEmpty()) {
                parts.add(point.subjectName)
            }
            // 只显示时间，不显示集数（集数已在角标中）
            val timeStr = point.s?.toIntOrNull()?.let { seconds ->
                val min = seconds / 60
                val sec = seconds % 60
                String.format("%02d:%02d", min, sec)
            }
            if (!timeStr.isNullOrEmpty()) {
                parts.add(timeStr)
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

    fun setEpisodeColorMap(colorMap: Map<String, Int>) {
        episodeColorMap = colorMap
        notifyDataSetChanged()
    }

    private fun updateBookmarkIcon(button: ImageButton, isSaved: Boolean) {
        button.setImageResource(
            if (isSaved) R.drawable.ic_bookmark_filled
            else R.drawable.ic_bookmark_border
        )
    }
}
