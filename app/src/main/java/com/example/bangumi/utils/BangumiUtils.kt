package com.example.bangumi.utils

import android.content.Context
import com.example.bangumi.data.model.BangumiDetail
import com.example.bangumi.widget.TagGroupView.Tag
import java.util.Calendar

/**
 * Created by gaoshiqi
 * on 2025/5/30 15:17
 * email: gaoshiqi@bilibili.com
 */
object BangumiUtils {

    /**
     * 人数单位转换
     * @param count Int
     * @return result String
     * 1000->1K, 1000000->1M, 1000000000->1B
     */
    fun convertCount(count: Int): String {
        return when {
            count >= 1_000_000_000 -> "%.2fB".format(count / 1_000_000_000.0).replace(Regex("\\.?0+\$"), "")
            count >= 1_000_000 -> "%.2fM".format(count / 1_000_000.0).replace(Regex("\\.?0+\$"), "")
            count >= 1_000 -> "%.2fK".format(count / 1_000.0).replace(Regex("\\.?0+\$"), "")
            else -> count.toString()
        }
    }

    /**
     * 获取今天是星期几
     * @return Int
     */
    fun getTodayWeekdayId(): Int {
        val calendar = Calendar.getInstance()
        // 注意：Calendar 周日=1，周一=2，需要转换
        var dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK) - 1
        if (dayOfWeek == 0) dayOfWeek = 7 // 周日
        return dayOfWeek
    }

    /**
     * 复制到剪贴板
     */
    fun copyContentToClipboard(content: String, context: Context) {
        val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
        val mClipData = android.content.ClipData.newPlainText("Label", content)
        cm.setPrimaryClip(mClipData)
    }

    fun getWeekdayName(weekdayId: Int): String {
        return when (weekdayId) {
            1 -> "周一"
            2 -> "周二"
            3 -> "周三"
            4 -> "周四"
            5 -> "周五"
            6 -> "周六"
            7 -> "周日"
            else -> ""
        }
    }

    fun getTags(data: BangumiDetail): List<Tag> {
        val tags = mutableListOf<Tag>()
        data.metaTags?.distinct()?.forEach { title ->
            tags.add(Tag(
                text = title,
                iconRes = com.example.bangumi.R.drawable.ic_vector_arrow_right
            ))
        }
        return tags
    }

}