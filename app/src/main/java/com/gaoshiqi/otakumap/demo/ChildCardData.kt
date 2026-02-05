package com.gaoshiqi.otakumap.demo

/**
 * 子卡片数据模型
 */
data class ChildCardData(
    val id: String,
    val title: String,
    val coverUrl: String,
    val authorName: String,
    val authorAvatar: String,
    val duration: String,
    val playCount: String  // 播放量 (如 "22.4K")
)

/**
 * Secondary Creation 区块数据
 */
data class SecondaryCreationSection(
    val sectionTitle: String,
    val items: List<ChildCardData>  // 1-9 个子卡片
)

/**
 * 测试数据生成器
 */
object TestDataGenerator {

    private val sampleTitles = listOf(
        "Demon Slayer: Kimetsu no Yaiba - All Out Attack to Destroy All Demons",
        "Record of Ragnarok and the Epic Battle",
        "Virus Invasion of the city, our Harley Davidson",
        "Attack on Titan Final Season",
        "Jujutsu Kaisen: Hidden Inventory",
        "Chainsaw Man Opening Analysis",
        "Spy x Family Episode Review",
        "Bocchi the Rock Live Performance",
        "Frieren Beyond Journey's End"
    )

    private val sampleAuthors = listOf(
        "AnimeFanatics", "OtakuReviews", "MangaLover",
        "AnimeInsights", "WeebCentral", "SakuraSubs",
        "TokyoAnime", "AnimeExplained", "MangaHub"
    )

    private val samplePlayCounts = listOf(
        "22.4K", "15.8K", "8.2K", "45.1K", "12.3K",
        "6.7K", "33.9K", "19.5K", "28.0K"
    )

    /**
     * 生成 9 种测试数据（1-9 张子卡片）
     */
    fun generateTestSections(): List<SecondaryCreationSection> {
        return (1..9).map { count ->
            SecondaryCreationSection(
                sectionTitle = "Secondary Creation",
                items = generateChildCards(count)
            )
        }
    }

    /**
     * 生成指定数量的子卡片
     */
    private fun generateChildCards(count: Int): List<ChildCardData> {
        return (1..count).map { index ->
            ChildCardData(
                id = "test_$index",
                title = sampleTitles.getOrElse(index - 1) { "Sample Title $index" },
                coverUrl = "", // 使用占位图
                authorName = sampleAuthors.getOrElse(index - 1) { "Author $index" },
                authorAvatar = "",
                duration = formatDuration(index),
                playCount = samplePlayCounts.getOrElse(index - 1) { "${index * 5}.${index}K" }
            )
        }
    }

    private fun formatDuration(index: Int): String {
        val minutes = (index * 3) % 60
        val seconds = (index * 17) % 60
        return String.format("%02d:%02d", minutes, seconds)
    }
}
