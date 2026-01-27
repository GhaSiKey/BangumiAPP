package com.gaoshiqi.player.omofun.data

import com.gaoshiqi.player.omofun.data.model.OmofunAnimeDetail
import com.gaoshiqi.player.omofun.data.model.OmofunSearchResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.net.URLEncoder
import java.util.concurrent.TimeUnit

/**
 * Omofun 数据仓库
 * 封装网络请求和数据解析逻辑
 */
class OmofunRepository {

    companion object {
        private const val BASE_URL = "https://omofun03.top"
        private const val SEARCH_PATH = "/vod/search.html"
        private const val TIMEOUT_SECONDS = 30L
    }

    private val client: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .readTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .followRedirects(true)
            .addInterceptor { chain ->
                val request = chain.request().newBuilder()
                    .header("User-Agent", USER_AGENT)
                    .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
                    .header("Accept-Language", "zh-CN,zh;q=0.9,en;q=0.8")
                    .header("Referer", BASE_URL)
                    .build()
                chain.proceed(request)
            }
            .build()
    }

    /**
     * 搜索番剧
     * @param keyword 搜索关键词
     * @return 搜索结果列表
     */
    suspend fun search(keyword: String): Result<List<OmofunSearchResult>> = withContext(Dispatchers.IO) {
        runCatching {
            val encodedKeyword = URLEncoder.encode(keyword, "UTF-8")
            val url = "$BASE_URL$SEARCH_PATH?wd=$encodedKeyword"

            val request = Request.Builder()
                .url(url)
                .get()
                .build()

            val response = client.newCall(request).execute()
            if (!response.isSuccessful) {
                throw RuntimeException("搜索请求失败: ${response.code}")
            }

            val html = response.body?.string() ?: throw RuntimeException("响应体为空")
            OmofunParser.parseSearchResults(html)
        }
    }

    /**
     * 获取番剧详情
     * @param detailUrl 详情页 URL (可以是完整 URL 或相对路径)
     * @return 番剧详情
     */
    suspend fun getDetail(detailUrl: String): Result<OmofunAnimeDetail> = withContext(Dispatchers.IO) {
        runCatching {
            val fullUrl = normalizeUrl(detailUrl)

            val request = Request.Builder()
                .url(fullUrl)
                .get()
                .build()

            val response = client.newCall(request).execute()
            if (!response.isSuccessful) {
                throw RuntimeException("获取详情失败: ${response.code}")
            }

            val html = response.body?.string() ?: throw RuntimeException("响应体为空")
            OmofunParser.parseAnimeDetail(html)
        }
    }

    private fun normalizeUrl(url: String): String {
        return when {
            url.startsWith("http") -> url
            url.startsWith("//") -> "https:$url"
            url.startsWith("/") -> "$BASE_URL$url"
            else -> "$BASE_URL/$url"
        }
    }
}

private const val USER_AGENT =
    "Mozilla/5.0 (Linux; Android 13; Pixel 7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Mobile Safari/537.36"
