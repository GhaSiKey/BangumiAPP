package com.example.bangumi.utils

import android.content.Context
import com.example.bangumi.data.bean.BangumiDetail
import com.example.bangumi.widget.TagGroupView.Tag
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

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

    /**
     * 照章节的`type`条目收藏状态
     * 1 = 想看
     * 2 = 看过
     * 3 = 在看
     * 4 = 搁置
     * 5 = 抛弃
     */
    fun getCollectionStatus(type: Int): String {
        return when (type) {
            1 -> "想看"
            2 -> "看过"
            3 -> "在看"
            4 -> "搁置"
            5 -> "抛弃"
            else -> ""
        }
    }

    /**
     * 根据时间戳与当前时间的间隔返回不同的展示内容
     * @param timestamp 时间戳（单位：秒）
     * @return 格式化后的时间字符串
     */
    fun formatTimeByInterval(timestamp: Long): String {
        val now = Calendar.getInstance()
        val target = Calendar.getInstance()
        target.timeInMillis = timestamp * 1000 // 将秒转换为毫秒

        val diffInMillis = now.timeInMillis - target.timeInMillis
        val diffInMinutes = diffInMillis / (1000 * 60)
        val diffInHours = diffInMinutes / 60

        return when {
            diffInMinutes < 60 -> "${diffInMinutes}分钟前"
            diffInHours < 6 -> "${diffInHours}小时前"
            isSameDay(now, target) -> "今天"
            isPreviousDay(now, target) -> "昨天"
            else -> SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(timestamp * 1000))
        }
    }

    /**
     * 判断两个 Calendar 对象是否为同一天
     * @param cal1 第一个 Calendar 对象
     * @param cal2 第二个 Calendar 对象
     * @return 是否为同一天
     */
    private fun isSameDay(cal1: Calendar, cal2: Calendar): Boolean {
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
    }

    /**
     * 判断第二个 Calendar 对象是否为第一个 Calendar 对象的前一天
     * @param cal1 第一个 Calendar 对象
     * @param cal2 第二个 Calendar 对象
     * @return 是否为前一天
     */
    private fun isPreviousDay(cal1: Calendar, cal2: Calendar): Boolean {
        val temp = Calendar.getInstance()
        temp.timeInMillis = cal1.timeInMillis
        temp.add(Calendar.DAY_OF_YEAR, -1)
        return isSameDay(temp, cal2)
    }

}