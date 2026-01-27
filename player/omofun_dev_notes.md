# Omofun 视频爬取功能开发文档

## 方案选型讨论

### 视频 URL 提取方案对比

| 方案 | 描述 | 优点 | 缺点 |
|------|------|------|------|
| **A. WebView + JS 注入** ✅ | 用 WebView 加载播放页，通过 evaluateJavascript 读取 player_aaaa 变量 | 能执行页面 JS，获取动态生成的数据 | 需要处理 WebView 生命周期，首次加载较慢 |
| B. 静态 HTML 解析 | 直接用 OkHttp 获取 HTML，正则提取 player_aaaa | 速度快，无需 WebView | 如果 URL 是 JS 动态生成的则无法获取 |
| C. 抓包分析 API | 分析网络请求，找到后端 API 直接调用 | 最优雅，不依赖页面结构 | 需要逆向分析，API 可能加密或经常变化 |
| D. 无头浏览器 | 使用 Puppeteer/Playwright 等 | 最强大，能处理任何复杂场景 | Android 端难以实现，资源消耗大 |

**最终选择方案 A**，原因：
1. omofun 的视频地址存储在 `player_aaaa` 全局变量中，需要 JS 环境才能可靠获取
2. WebView 是 Android 原生组件，无需引入额外依赖
3. 相比无头浏览器，资源消耗可控

---

## 架构设计

### 数据流

```
用户搜索 ─► OmofunRepository.search() ─► OkHttp 请求 ─► OmofunParser 解析
    │
    ▼
点击结果 ─► OmofunRepository.getDetail() ─► OkHttp 请求 ─► OmofunParser 解析
    │
    ▼
点击集数 ─► VideoExtractor.extract() ─► WebView 加载 ─► JS 注入提取
    │
    ▼
播放视频 ─► PlayerTestActivity ─► ExoPlayer
```

### 模块职责

| 模块 | 职责 |
|------|------|
| `OmofunParser` | 纯函数，HTML → 数据模型转换 |
| `OmofunRepository` | 网络请求 + 解析调度 |
| `VideoExtractor` | WebView 管理 + JS 注入 |
| `OmofunViewModel` | UI 状态管理 + 业务逻辑协调 |
| `OmofunActivity` | 导航容器 + 生命周期管理 |

---

## WebView 生命周期管理

### 问题
WebView 必须在主线程创建和操作，且需要正确释放避免内存泄漏。

### 解决方案
1. `VideoExtractor` 持有 WebView 实例
2. 使用 `Handler(Looper.getMainLooper())` 确保主线程操作
3. ViewModel 的 `onCleared()` 中调用 `VideoExtractor.release()`
4. `suspendCancellableCoroutine` 支持协程取消时释放资源

```kotlin
class OmofunViewModel(application: Application) : AndroidViewModel(application) {
    private val videoExtractor = VideoExtractor(application.applicationContext)

    override fun onCleared() {
        super.onCleared()
        videoExtractor.release()  // 释放 WebView
    }
}
```

---

## HTML 解析规则

### 搜索结果页
```
选择器: .public-list-box li, .search-box li, .vodlist li
├── 链接: a.vodlist_thumb, a[href*='/detail/']
├── 标题: .vodlist_title, .title, h4
├── 封面: data-original, background-image, img[src]
├── 年份: .vodlist_year, .year
└── 状态: .vodlist_status, .status
```

### 详情页
```
选择器:
├── 标题: h1.title, .video-info-header h1
├── 封面: .detail_img img, .video-cover img
├── 简介: .detail_content, .video-info-content
└── 播放列表:
    ├── Tab: .anthology-tab a, .play-tab a
    └── 集数: .anthology-list-box a, .playlist a
```

### 播放页 JS 变量
```javascript
// 目标变量
window.player_aaaa = {
    url: "https://xxx.m3u8",  // 视频地址（可能 Base64 编码）
    ...
}

// 提取脚本
if (typeof player_aaaa !== 'undefined' && player_aaaa.url) {
    Android.onVideoUrlFound(player_aaaa.url);
}
```

---

## 已知问题和注意事项

### 1. ✅ CDN 防盗链问题（已解决）

**问题描述**：
omofun 的部分视频源使用小红书 CDN (xhscdn.com)，该 CDN 有严格的防盗链策略。

**根因（2026-01-23 定位）**：
问题不是 Referer/User-Agent 设置不对，而是 **使用了 OkHttpDataSource 替换 ExoPlayer 默认的 DataSource**。

OkHttp 的 TLS 指纹与 Android 系统的 HttpURLConnection 不同，小红书 CDN 通过 TLS 指纹检测识别出非浏览器请求并返回 403。

**解决方案**：
使用 ExoPlayer 默认构建方式，不要替换 DataSource：

```kotlin
// ✅ 正确做法
val player = ExoPlayer.Builder(context).build()
player.setMediaItem(MediaItem.fromUri(url))
```

**详细分析**：见文档末尾「调试记录 - OkHttpDataSource vs DefaultHttpDataSource」

### 2. 网站结构变化
omofun 等视频网站可能随时更新页面结构，导致解析失败。
**建议**：Parser 使用多个备选选择器，并记录详细日志便于调试。

### 3. URL 编码
视频地址可能是 Base64 编码，需要尝试解码：
```kotlin
private fun tryDecodeUrl(url: String): String {
    return try {
        if (url.matches(Regex("^[A-Za-z0-9+/=]+$")) && url.length > 20) {
            val decoded = String(Base64.decode(url, Base64.DEFAULT))
            if (decoded.startsWith("http")) return decoded
        }
        url
    } catch (e: Exception) { url }
}
```

### 4. 反爬机制
**已处理**：
- User-Agent 模拟移动端 Chrome
- 设置 Referer 头（通过 CDN 映射表自动识别）
- 启用 Cookie 和 DOM Storage

**可能遇到**：
- IP 封禁 → 需要代理
- JS 加密 → 需要分析加密算法
- 验证码 → 无法自动绑过
- **CDN 防盗链** → 见上方解决方案

### 5. 视频格式兼容性
ExoPlayer 支持常见格式，但某些格式可能需要额外配置：
- m3u8 (HLS) ✅ 已添加 `media3-exoplayer-hls`
- mp4 ✅
- flv ⚠️ 需要额外扩展

---

## 使用方式

### 从其他模块启动
```kotlin
// 启动 Omofun 搜索页
OmofunActivity.start(context)

// 直接播放视频
PlayerTestActivity.start(context, videoUrl = "https://xxx.m3u8", title = "视频标题")
```

### 测试流程
1. 启动 OmofunActivity
2. 搜索 "葬送的芙莉莲"
3. 点击搜索结果
4. 点击某一集
5. 等待视频提取完成
6. 验证视频播放

---

## 调试记录

### 2026-01-23：CDN 防盗链调试

**问题**：ExoPlayer 播放小红书 CDN 视频时返回 403

**排查过程**：

1. **初始症状**
   ```
   [ERROR] 加载失败[媒体分片] 域名:fe-video-qc.xhscdn.com
   错误: Response code: 403
   ```

2. **尝试方案 1：添加 Referer 头**
   - 在 `PlayerViewModel` 中添加 `CDN_REFERER_MAP` 映射表
   - 自动识别 xhscdn.com 域名并设置 `Referer: https://omofun03.top/`
   - **结果**：仍然 403

3. **尝试方案 2：添加 Origin 头**
   - 同时设置 `Origin: https://omofun03.top`
   - **结果**：仍然 403

4. **验证测试**
   ```bash
   # 在终端用 curl 测试，带完整请求头
   curl -H "Referer: https://omofun03.top/" \
        -H "Origin: https://omofun03.top" \
        -H "User-Agent: Mozilla/5.0..." \
        "https://fe-video-qc.xhscdn.com/xxx"
   # 结果：HTTP/2 403
   ```
   说明问题不在 App 代码，而是 CDN 本身的策略

5. **测试其他线路**
   ```bash
   # 线路 9 (sid=9) 使用 dytt-hot.com CDN
   curl -I "https://vip.dytt-hot.com/20250131/24912_e10e163d/index.m3u8"
   # 结果：HTTP/2 200 ✅
   ```

**结论**：
- 小红书 CDN 的防盗链不只检查 Referer，可能还有 IP/Cookie/签名验证
- 解决方案是优先使用其他 CDN 的播放线路

**可用测试 URL**：
```
# ✅ 可用 (dytt-hot.com CDN)
https://vip.dytt-hot.com/20250131/24912_e10e163d/index.m3u8

# ✅ 可用 (bfvvs.com CDN)
https://hn.bfvvs.com/play/b688Ynle/index.m3u8

# ❌ 不可用 (xhscdn.com CDN - 403)
https://fe-video-qc.xhscdn.com/athena-creator/xxx
```

**代码修改**：
- `PlayerViewModel.kt`：添加 `CDN_REFERER_MAP` 和 `autoDetectReferer()` 方法
- 每次播放时重新创建 ExoPlayer 实例以确保请求头生效

### 2026-01-23：TLS 指纹检测推测 → ❌ 已证伪

**问题**：即使完全模拟 Chrome 请求头（包括 Sec-Fetch-* 系列），xhscdn.com 仍然返回 403

**最初推测**：小红书 CDN 使用 TLS 指纹检测（JA3/JA4）

**实际根因**：见下方「OkHttpDataSource vs DefaultHttpDataSource」

---

### 2026-01-23：OkHttpDataSource vs DefaultHttpDataSource（根因定位）⭐

**问题复现**：
- ❌ 使用 `OkHttpDataSource` 播放 xhscdn.com → 403 Forbidden
- ✅ 使用默认 `ExoPlayer.Builder(context).build()` → 正常播放

**关键发现**：
之前为了统一添加请求头，将 ExoPlayer 的 DataSource 从默认实现改为了 OkHttpDataSource：

```kotlin
// ❌ 有问题的实现（使用 OkHttpDataSource）
val okHttpClient = OkHttpClient.Builder()
    .addNetworkInterceptor { chain ->
        chain.proceed(chain.request().newBuilder()
            .header("User-Agent", "...")
            .header("Referer", "...")
            .build())
    }
    .build()

val dataSourceFactory = OkHttpDataSource.Factory(okHttpClient)
val mediaSource = ProgressiveMediaSource.Factory(dataSourceFactory)
    .createMediaSource(MediaItem.fromUri(url))
player.setMediaSource(mediaSource)
```

```kotlin
// ✅ 正确的实现（使用默认 DataSource）
val player = ExoPlayer.Builder(context).build()
player.setMediaItem(MediaItem.fromUri(url))
```

**技术原因**：

| 组件 | 底层实现 | TLS 栈 |
|------|----------|--------|
| DefaultHttpDataSource | Android HttpURLConnection | 系统 SSL |
| OkHttpDataSource | OkHttp | OkHttp 内置 SSL |

两者的 TLS 握手参数（密码套件顺序、扩展列表等）不同，产生不同的 TLS 指纹。
小红书 CDN 检测到 OkHttp 的指纹，判定为非浏览器请求并返回 403。

**为什么之前没发现**：
- 其他 CDN（如 dytt-hot.com）不做 TLS 指纹检测，两种方式都能播放
- 只有 xhscdn.com 严格检测，才暴露出这个问题

**解决方案**：
回退 `PlayerViewModel.kt`，不使用 OkHttpDataSource，直接用默认的 ExoPlayer 构建方式：

```kotlin
// PlayerViewModel.kt
private fun createPlayer(): ExoPlayer {
    return ExoPlayer.Builder(context).build()
}
```

**教训总结**：
1. **不要过度封装**：ExoPlayer 默认的 DataSource 已经足够用，除非有明确需求不要替换
2. **TLS 指纹是隐形的差异**：看起来完全相同的 HTTP 请求，可能因为底层 TLS 实现不同而被区别对待
3. **优先使用系统组件**：Android 系统的 HttpURLConnection 比第三方库更容易被 CDN 信任

---

### 线路优先级功能（已移除）

**原设计**：
根据 CDN 可用性对播放线路进行优先级排序，把已知可用的线路排在前面。

**移除原因**：
定位到真正问题是 OkHttpDataSource 后，回退代码即可解决，不需要复杂的线路优先级逻辑。

**移除的代码**：
- `OmofunParser.kt`：删除 `SOURCE_PRIORITY_MAP`、`PROBLEMATIC_KEYWORDS`、`calculateSourcePriority()`
- `OmofunAnimeDetail.kt`：删除 `OmofunPlaylist.priority` 和 `OmofunPlaylist.isRecommended` 字段
- `OmofunDetailScreen.kt`：删除 PlaylistTabs 中的「推荐」标记 UI

**当前行为**：
所有播放线路按原始顺序展示，用户自行选择。

---

## WebView 播放兜底方案（备用）

### 2026-01-23：实现 WebView 自动切换

> ⚠️ **注意**：在定位到 OkHttpDataSource 是根因后，此方案作为备用，当前未启用。

**原设计**：
自动检测 xhscdn URL，切换到 WebView 播放器。

**实现文件**：
- `WebViewPlayer.kt` - WebView 视频播放组件（已创建，未使用）

**核心代码**：

```kotlin
// WebViewPlayer.kt
fun shouldUseWebViewPlayer(url: String): Boolean {
    val host = android.net.Uri.parse(url).host ?: ""
    val webViewRequiredDomains = listOf(
        "xhscdn.com",
        "fe-video-qc.xhscdn.com",
    )
    return webViewRequiredDomains.any { host.endsWith(it) }
}
```

**保留此方案的原因**：
- 如果未来遇到其他 CDN 的防盗链问题，可以快速启用
- WebView 播放是最后的兜底手段
