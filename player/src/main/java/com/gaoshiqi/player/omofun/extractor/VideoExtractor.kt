package com.gaoshiqi.player.omofun.extractor

import android.annotation.SuppressLint
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Base64
import android.util.Log
import android.webkit.JavascriptInterface
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeout
import java.net.URLDecoder
import kotlin.coroutines.resume

/**
 * 视频 URL 提取器
 * 使用 WebView + JS 注入方式提取 player_aaaa 变量中的视频地址
 *
 * 原理：
 * 1. 用 WebView 加载播放页面
 * 2. 等待页面 JS 执行完毕后，通过 evaluateJavascript 读取 window.player_aaaa
 * 3. 解析 player_aaaa.url 获取真实视频地址（可能需要 Base64 解码）
 */
class VideoExtractor(private val context: Context) {

    companion object {
        private const val TAG = "VideoExtractor"
        private const val EXTRACTION_TIMEOUT_MS = 30_000L
        private const val PAGE_LOAD_DELAY_MS = 2000L
    }

    private var webView: WebView? = null
    private val mainHandler = Handler(Looper.getMainLooper())

    /**
     * 提取视频 URL
     * @param playUrl 播放页面 URL
     * @return 提取到的视频直链
     */
    @SuppressLint("SetJavaScriptEnabled")
    suspend fun extract(playUrl: String): Result<String> = withTimeout(EXTRACTION_TIMEOUT_MS) {
        suspendCancellableCoroutine { continuation ->
            mainHandler.post {
                try {
                    // 创建 WebView
                    val wv = createWebView()
                    webView = wv

                    var isResumed = false

                    // 设置 JS 回调接口
                    val jsInterface = object {
                        @JavascriptInterface
                        fun onVideoUrlFound(url: String) {
                            Log.d(TAG, "JS 回调获取到 URL: $url")
                            if (!isResumed && url.isNotBlank()) {
                                isResumed = true
                                val decodedUrl = tryDecodeUrl(url)
                                // 提取成功后立即释放 WebView，防止后台继续播放
                                mainHandler.post { releaseWebView() }
                                continuation.resume(Result.success(decodedUrl))
                            }
                        }

                        @JavascriptInterface
                        fun onError(message: String) {
                            Log.e(TAG, "JS 提取失败: $message")
                            if (!isResumed) {
                                isResumed = true
                                // 提取失败也要释放 WebView
                                mainHandler.post { releaseWebView() }
                                continuation.resume(Result.failure(RuntimeException(message)))
                            }
                        }
                    }
                    wv.addJavascriptInterface(jsInterface, "Android")

                    wv.webViewClient = object : WebViewClient() {
                        override fun onPageFinished(view: WebView?, url: String?) {
                            super.onPageFinished(view, url)
                            Log.d(TAG, "页面加载完成: $url")

                            // 延迟执行 JS 提取，等待页面 JS 初始化完成
                            mainHandler.postDelayed({
                                if (!isResumed) {
                                    injectExtractionScript(wv)
                                }
                            }, PAGE_LOAD_DELAY_MS)
                        }

                        override fun shouldOverrideUrlLoading(
                            view: WebView?,
                            request: WebResourceRequest?
                        ): Boolean {
                            // 拦截视频直链跳转
                            val redirectUrl = request?.url?.toString() ?: return false
                            if (isVideoUrl(redirectUrl) && !isResumed) {
                                Log.d(TAG, "拦截到视频直链: $redirectUrl")
                                isResumed = true
                                // 拦截到视频后立即释放 WebView
                                mainHandler.post { releaseWebView() }
                                continuation.resume(Result.success(redirectUrl))
                                return true
                            }
                            return false
                        }
                    }

                    // 加载播放页
                    Log.d(TAG, "开始加载播放页: $playUrl")
                    wv.loadUrl(playUrl)

                    // 取消时释放资源
                    continuation.invokeOnCancellation {
                        mainHandler.post { releaseWebView() }
                    }

                } catch (e: Exception) {
                    Log.e(TAG, "提取异常", e)
                    continuation.resume(Result.failure(e))
                }
            }
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun createWebView(): WebView {
        return WebView(context).apply {
            settings.apply {
                javaScriptEnabled = true
                domStorageEnabled = true
                // 要求用户手势才能播放媒体，防止 WebView 自动播放视频
                mediaPlaybackRequiresUserGesture = true
                userAgentString = USER_AGENT
                // 允许混合内容（HTTP/HTTPS）
                mixedContentMode = android.webkit.WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
            }
        }
    }

    private fun injectExtractionScript(webView: WebView) {
        // 注入 JS 脚本提取 player_aaaa 变量
        val script = """
            (function() {
                try {
                    // 方式1: 直接读取 window.player_aaaa
                    if (typeof player_aaaa !== 'undefined' && player_aaaa.url) {
                        Android.onVideoUrlFound(player_aaaa.url);
                        return;
                    }

                    // 方式2: 查找 script 标签中的 player_aaaa 定义
                    var scripts = document.getElementsByTagName('script');
                    for (var i = 0; i < scripts.length; i++) {
                        var content = scripts[i].innerHTML;
                        if (content.indexOf('player_aaaa') !== -1) {
                            var match = content.match(/player_aaaa\s*=\s*(\{[^}]+\})/);
                            if (match) {
                                var data = eval('(' + match[1] + ')');
                                if (data && data.url) {
                                    Android.onVideoUrlFound(data.url);
                                    return;
                                }
                            }
                        }
                    }

                    // 方式3: 查找 iframe 中的视频
                    var iframes = document.querySelectorAll('iframe');
                    for (var j = 0; j < iframes.length; j++) {
                        var src = iframes[j].src;
                        if (src && (src.indexOf('.m3u8') !== -1 || src.indexOf('.mp4') !== -1)) {
                            Android.onVideoUrlFound(src);
                            return;
                        }
                    }

                    // 方式4: 查找 video 标签
                    var videos = document.querySelectorAll('video source, video');
                    for (var k = 0; k < videos.length; k++) {
                        var videoSrc = videos[k].src || videos[k].getAttribute('src');
                        if (videoSrc && videoSrc.length > 0) {
                            Android.onVideoUrlFound(videoSrc);
                            return;
                        }
                    }

                    Android.onError('未找到视频地址');
                } catch (e) {
                    Android.onError('提取异常: ' + e.message);
                }
            })();
        """.trimIndent()

        webView.evaluateJavascript(script, null)
    }

    /**
     * 尝试解码 URL（可能是 Base64 编码）
     */
    private fun tryDecodeUrl(url: String): String {
        return try {
            // 尝试 Base64 解码
            if (url.matches(Regex("^[A-Za-z0-9+/=]+$")) && url.length > 20) {
                val decoded = String(Base64.decode(url, Base64.DEFAULT))
                if (decoded.startsWith("http")) {
                    Log.d(TAG, "Base64 解码成功: $decoded")
                    return decoded
                }
            }
            // 尝试 URL 解码
            URLDecoder.decode(url, "UTF-8")
        } catch (e: Exception) {
            url
        }
    }

    private fun isVideoUrl(url: String): Boolean {
        val lowerUrl = url.lowercase()
        return lowerUrl.contains(".m3u8") ||
                lowerUrl.contains(".mp4") ||
                lowerUrl.contains(".flv") ||
                lowerUrl.contains("/hls/") ||
                lowerUrl.contains("video")
    }

    /**
     * 释放 WebView 资源
     * 应在 ViewModel 的 onCleared 中调用
     */
    fun release() {
        mainHandler.post {
            releaseWebView()
        }
    }

    private fun releaseWebView() {
        webView?.apply {
            stopLoading()
            clearHistory()
            removeAllViews()
            destroy()
        }
        webView = null
        Log.d(TAG, "WebView 资源已释放")
    }
}

private const val USER_AGENT =
    "Mozilla/5.0 (Linux; Android 13; Pixel 7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Mobile Safari/537.36"
