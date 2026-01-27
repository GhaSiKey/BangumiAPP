package com.gaoshiqi.player.omofun.data

import com.gaoshiqi.player.omofun.data.model.OmofunAnimeDetail
import com.gaoshiqi.player.omofun.data.model.OmofunEpisode
import com.gaoshiqi.player.omofun.data.model.OmofunPlaylist
import com.gaoshiqi.player.omofun.data.model.OmofunSearchResult
import org.jsoup.Jsoup
import org.jsoup.nodes.Document

/**
 * Omofun HTML 解析器
 * 使用 Jsoup 解析网页内容，提取搜索结果和番剧详情
 */
object OmofunParser {

    private const val BASE_URL = "https://omofun03.top"

    /**
     * 解析搜索结果页面
     * @param html 搜索结果页面的 HTML 内容
     * @return 搜索结果列表
     *
     * omofun 实际 HTML 结构：
     * - 结果容器：.module-card-items
     * - 单个结果：.module-card-item
     * - 链接：a.module-card-item-poster[href]
     * - 标题：.module-card-item-title strong
     * - 封面：img.lazy[data-original]
     * - 状态：.module-item-note
     * - 年份信息：.module-info-item-content
     */
    fun parseSearchResults(html: String): List<OmofunSearchResult> {
        val document = Jsoup.parse(html)
        val results = mutableListOf<OmofunSearchResult>()

        // omofun 搜索结果使用 .module-card-item 结构
        val items = document.select(".module-card-item")

        for (item in items) {
            try {
                // 提取链接
                val linkElement = item.selectFirst("a.module-card-item-poster, a[href*='/vod/detail/']")
                    ?: continue
                val detailUrl = normalizeUrl(linkElement.attr("href"))

                // 提取标题
                val title = item.selectFirst(".module-card-item-title strong")?.text()?.trim()
                    ?: item.selectFirst(".module-card-item-title a")?.text()?.trim()
                    ?: ""

                if (title.isBlank() || detailUrl.isBlank()) continue

                // 提取封面 (懒加载图片使用 data-original)
                val coverUrl = item.selectFirst("img.lazy, img.lazyload")?.let { img ->
                    val dataOriginal = img.attr("data-original")
                    if (dataOriginal.isNotBlank() && !dataOriginal.contains("load.gif")) {
                        dataOriginal
                    } else {
                        img.attr("src").takeIf { it.isNotBlank() && !it.contains("load.gif") }
                    }
                }

                // 提取状态 (如：完结、更新至01集)
                val status = item.selectFirst(".module-item-note")?.text()?.trim()

                // 提取年份 (从 .module-info-item-content 中提取)
                val infoContent = item.selectFirst(".module-info-item-content")?.text() ?: ""
                val year = Regex("""(\d{4})""").find(infoContent)?.groupValues?.get(1)

                results.add(
                    OmofunSearchResult(
                        title = title,
                        coverUrl = coverUrl,
                        detailUrl = detailUrl,
                        year = year,
                        status = status
                    )
                )
            } catch (e: Exception) {
                // 跳过解析失败的项
                continue
            }
        }

        return results
    }

    /**
     * 解析番剧详情页面
     * @param html 详情页面的 HTML 内容
     * @return 番剧详情
     */
    fun parseAnimeDetail(html: String): OmofunAnimeDetail {
        val document = Jsoup.parse(html)

        // 提取标题
        val title = extractTitle(document)

        // 提取封面
        val coverUrl = extractDetailCover(document)

        // 提取简介
        val description = extractDescription(document)

        // 提取播放列表
        val playlists = extractPlaylists(document)

        return OmofunAnimeDetail(
            title = title,
            coverUrl = coverUrl,
            description = description,
            playlists = playlists
        )
    }

    /**
     * omofun 详情页结构：
     * - 标题：.module-info-heading h1
     * - 封面：.module-info-poster img[data-original]
     * - 简介：.module-info-introduction-content
     * - 线路 Tab：.module-tab-item[data-dropdown-value]
     * - 播放列表：.module-play-list-content
     * - 单集链接：a.module-play-list-link[href]
     */
    private fun extractTitle(document: Document): String {
        return document.selectFirst(".module-info-heading h1")?.text()?.trim()
            ?: document.selectFirst("h1")?.text()?.trim()
            ?: "未知标题"
    }

    private fun extractDetailCover(document: Document): String? {
        val coverElement = document.selectFirst(".module-info-poster img, .module-item-pic img")
        return coverElement?.let { extractImageUrl(it) }
    }

    private fun extractDescription(document: Document): String? {
        val descElement = document.selectFirst(".module-info-introduction-content")
        return descElement?.text()?.trim()?.takeIf { it.isNotBlank() }
    }

    private fun extractPlaylists(document: Document): List<OmofunPlaylist> {
        val playlists = mutableListOf<OmofunPlaylist>()

        // omofun 结构: 多个 .module-tab-item 对应多个 .module-play-list
        val tabs = document.select(".module-tab-item.tab-item")
        val playlistContents = document.select(".module-play-list")

        if (tabs.isNotEmpty() && playlistContents.isNotEmpty()) {
            tabs.forEachIndexed { index, tab ->
                val name = tab.attr("data-dropdown-value").ifBlank {
                    tab.text().trim().ifBlank { "线路${index + 1}" }
                }
                val contentBox = playlistContents.getOrNull(index)
                if (contentBox != null) {
                    val episodes = parseEpisodeList(contentBox)
                    if (episodes.isNotEmpty()) {
                        playlists.add(OmofunPlaylist(name = name, episodes = episodes))
                    }
                }
            }
        }

        // 备用：如果没有 Tab，直接解析所有播放列表
        if (playlists.isEmpty()) {
            playlistContents.forEachIndexed { index, contentBox ->
                val episodes = parseEpisodeList(contentBox)
                if (episodes.isNotEmpty()) {
                    playlists.add(OmofunPlaylist(name = "线路${index + 1}", episodes = episodes))
                }
            }
        }

        // 最后备用：查找任何播放链接
        if (playlists.isEmpty()) {
            val directLinks = document.select("a.module-play-list-link, a[href*='/vod/play/']")
            val episodes = directLinks.mapNotNull { link ->
                val url = normalizeUrl(link.attr("href"))
                val name = link.selectFirst("span")?.text()?.trim()
                    ?: link.text().trim()
                if (url.isNotBlank() && name.isNotBlank()) {
                    OmofunEpisode(name = name, playUrl = url)
                } else null
            }
            if (episodes.isNotEmpty()) {
                playlists.add(OmofunPlaylist(name = "默认线路", episodes = episodes))
            }
        }

        return playlists
    }

    private fun parseEpisodeList(container: org.jsoup.nodes.Element): List<OmofunEpisode> {
        val links = container.select("a.module-play-list-link, a[href*='/vod/play/']")
        return links.mapNotNull { link ->
            val url = normalizeUrl(link.attr("href"))
            val name = link.selectFirst("span")?.text()?.trim()
                ?: link.text().trim()

            if (url.isBlank() || name.isBlank()) return@mapNotNull null

            OmofunEpisode(name = name, playUrl = url)
        }
    }

    private fun extractImageUrl(img: org.jsoup.nodes.Element): String?  {
        // 按优先级尝试不同属性
        val attrs = listOf("data-original", "data-src", "data-lazy-src", "src")
        for (attr in attrs) {
            val url = img.attr(attr)
            if (url.isNotBlank() && !url.contains("loading") && !url.contains("placeholder")) {
                return normalizeUrl(url)
            }
        }
        return null
    }

    private fun normalizeUrl(url: String): String {
        return when {
            url.isBlank() -> ""
            url.startsWith("//") -> "https:$url"
            url.startsWith("/") -> "$BASE_URL$url"
            url.startsWith("http") -> url
            else -> "$BASE_URL/$url"
        }
    }
}
