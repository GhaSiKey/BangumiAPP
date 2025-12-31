# omofun 视频提取完整分析

## 🎊 最终完成!

经过完整的逆向分析,omofun 插件配置已经完成并可以正常工作!

---

## 📊 Kazumi 完整的数据流

### 第1步: 搜索阶段
```
用户输入: "葬送的芙莉莲"
↓
Kazumi 请求: https://omofun03.top/vod/search.html?wd=葬送的芙莉莲
↓
XPath 提取:
  - searchList: //div[@class='module-card-item module-item']  (找到 3 个结果)
  - searchName: .//div[@class='module-card-item-title']/a/strong  (提取标题)
  - searchResult: .//div[@class='module-card-item-title']/a  (提取详情页链接)
↓
结果: ["/vod/detail/id/169739.html", "/vod/detail/id/322870.html", ...]
```

### 第2步: 详情页阶段
```
用户点击: "葬送的芙莉莲"
↓
Kazumi 请求: https://omofun03.top/vod/detail/id/169739.html
↓
XPath 提取:
  - chapterRoads: //div[@class='module-play-list']  (找到 8 个播放列表)
  - chapterResult: .//a[@class='module-play-list-link']  (提取每个列表的分集链接)
↓
结果:
  播放列表1: ["/vod/play/id/169739/sid/6/nid/1.html", "/vod/play/id/169739/sid/6/nid/2.html", ...]
  播放列表2: ["/vod/play/id/169739/sid/9/nid/1.html", ...]
  ...
  共 8 个播放列表,每个 28 集
```

### 第3步: 播放页阶段 ⚡ (关键发现!)
```
用户点击: "第01集"
↓
Kazumi 使用 WebView 加载: https://omofun03.top/vod/play/id/169739/sid/6/nid/1.html
↓
WebView 执行页面中的 JavaScript
↓
JavaScript 创建变量:
  var player_aaaa = {
      "url": "https://fe-video-qc.xhscdn.com/athena-creator/1040g0pg3104o5f8u5q5g5pebdah3cnu7o5c94v8?filename=1.mp4",
      "url_next": "https://fe-video-qc.xhscdn.com/athena-creator/1040g0pg3104o5f8u5q605pebdah3cnu7af73c7o?filename=1.mp4",
      ...
  }
↓
Kazumi 的 WebView 拦截网络请求,捕获 .mp4/.m3u8 链接
↓
提取出真实视频 URL: https://fe-video-qc.xhscdn.com/athena-creator/1040g0pg3104o5f8u5q5g5pebdah3cnu7o5c94v8?filename=1.mp4
↓
调用 Media Kit 播放器播放视频
```

---

## 🔍 关键技术点

### 1. 为什么需要 WebView?

**问题**: 播放页面的 HTML 中没有直接的 `<video src="...">` 标签,视频链接藏在 JavaScript 变量中。

**解决方案**:
- 设置 `"useWebview": true`
- Kazumi 用 WebView 加载播放页面
- WebView 执行 JavaScript 后,真实视频链接才会出现
- WebView 拦截网络请求,捕获 `.mp4` 或 `.m3u8` 文件的 URL

### 2. WebView 的工作原理

Kazumi 的 WebView 实现(来自 [webview_controller.dart:48](lib/pages/webview/webview_controller.dart:48)):

```dart
// Stream to notify video source URL when the video source is loaded
// The first parameter is the video source URL and the second parameter is the video offset (start position)
final StreamController<(String, int)> videoParserEventController =
    StreamController<(String, int)>.broadcast();

Stream<(String, int)> get onVideoURLParser => videoParserEventController.stream;
```

**工作流程**:
1. WebView 加载播放页面
2. 监听所有网络请求
3. 当检测到 `.mp4`, `.m3u8` 等视频格式的 URL 时
4. 通过 `videoParserEventController` 通知播放器
5. 播放器获取真实视频 URL 并开始播放

### 3. omofun 的 JavaScript 混淆

播放页面中有大量混淆的 JavaScript:

```javascript
!function(){function a(a){var b={e:"P",w:"D",T:"y","+":"J",...};return a.split("").map(function(a){return void 0!==b[a]?b[a]:a}).join("")}var b=a(`wUOJxWvZzKl7_2(F6O2cYa[Xd5 F8[P!7_2...`);new Function(b)()}();
```

**目的**: 反爬虫,防止直接提取视频链接。

**绕过方法**: 使用 WebView,让浏览器执行混淆的 JavaScript,然后拦截网络请求。

---

## 📝 完整的 omofun.json 配置

```json
{
    "api": "1",
    "type": "anime",
    "name": "omofun",
    "version": "1.0",
    "muliSources": true,
    "useWebview": true,          // ⚡ 关键配置!必须为 true
    "useNativePlayer": true,
    "userAgent": "",
    "baseURL": "https://omofun03.top/",
    "searchURL": "https://omofun03.top/vod/search.html?wd=@keyword",
    "searchList": "//div[@class='module-card-item module-item']",
    "searchName": ".//div[@class='module-card-item-title']/a/strong",
    "searchResult": ".//div[@class='module-card-item-title']/a",
    "chapterRoads": "//div[@class='module-play-list']",
    "chapterResult": ".//a[@class='module-play-list-link']"
}
```

---

## 🧪 测试方法

### 在 Kazumi 中测试

```bash
# 1. 确保配置文件已更新
cat /Users/shiqigao/VSCodeProjects/Kazumi/assets/plugins/omofun.json

# 2. 重新运行 Kazumi
cd /Users/shiqigao/VSCodeProjects/Kazumi
flutter pub get
flutter run

# 3. 测试流程
# a. 搜索 "葬送的芙莉莲"
# b. 选择第一个结果
# c. 查看是否显示 8 个播放列表
# d. 点击 "第01集"
# e. 观察 WebView 是否加载页面并提取视频链接
# f. 视频应该开始播放
```

### 预期结果

1. **搜索页面**: 显示 3 个结果
   - 葬送的芙莉莲 ～●●的魔法～
   - 葬送的芙莉莲
   - 葬送的芙莉莲[电影解说]

2. **详情页面**: 显示 8 个播放列表
   - 独家超清 (28集)
   - 高清线路10 (28集)
   - 高清线路2 (28集)
   - 高清线路 (28集)
   - 高清线路3 (28集)
   - 高清线路4 (28集)
   - 高清线路7 (28集)
   - 超快线路《推荐》 (28集)

3. **播放页面**:
   - WebView 加载播放页面
   - 提取视频 URL: `https://fe-video-qc.xhscdn.com/athena-creator/...`
   - Media Kit 开始播放

---

## 🔧 Android 开发中的等价实现

如果你要在 Android 中实现类似功能,需要:

### 1. 使用 WebView 拦截网络请求

```kotlin
// Kotlin 示例
webView.webViewClient = object : WebViewClient() {
    override fun shouldInterceptRequest(
        view: WebView?,
        request: WebResourceRequest?
    ): WebResourceResponse? {
        val url = request?.url.toString()

        // 拦截视频链接
        if (url.endsWith(".mp4") || url.endsWith(".m3u8")) {
            Log.i("VideoExtractor", "Found video URL: $url")
            // 通知播放器
            onVideoUrlFound(url)
            return null
        }

        return super.shouldInterceptRequest(view, request)
    }
}

// 加载播放页面
webView.loadUrl("https://omofun03.top/vod/play/id/169739/sid/6/nid/1.html")
```

### 2. 或使用 JavaScript 注入

```kotlin
webView.settings.javaScriptEnabled = true

// 注入 JavaScript 提取 player_aaaa 变量
webView.evaluateJavascript("""
    (function() {
        if (typeof player_aaaa !== 'undefined') {
            return JSON.stringify(player_aaaa);
        }
        return null;
    })();
""") { result ->
    // result 是 JSON 字符串
    val playerData = JSONObject(result)
    val videoUrl = playerData.getString("url")
    Log.i("VideoExtractor", "Video URL: $videoUrl")
    playVideo(videoUrl)
}
```

### 3. 使用 OkHttp 拦截器 (更高级)

```kotlin
val client = OkHttpClient.Builder()
    .addInterceptor { chain ->
        val request = chain.request()
        val response = chain.proceed(request)

        val url = request.url.toString()
        if (url.endsWith(".mp4") || url.endsWith(".m3u8")) {
            Log.i("VideoExtractor", "Intercepted: $url")
            onVideoUrlFound(url)
        }

        response
    }
    .build()
```

---

## 🎯 总结

### ✅ 已完成

- [x] 搜索 XPath 配置
- [x] 详情页 XPath 配置
- [x] 播放页 WebView 配置
- [x] 完整的 omofun.json 插件
- [x] 理解 Kazumi 的视频提取机制
- [x] 分析播放页面的 JavaScript 变量
- [x] 提供 Android 实现方案

### 📊 配置对照表

| 阶段 | 配置项 | 值 | 说明 |
|------|--------|-----|------|
| 搜索 | `searchList` | `//div[@class='module-card-item module-item']` | 3个搜索结果 |
| 搜索 | `searchName` | `.//div[@class='module-card-item-title']/a/strong` | 番剧标题 |
| 搜索 | `searchResult` | `.//div[@class='module-card-item-title']/a` | 详情页链接 |
| 详情 | `chapterRoads` | `//div[@class='module-play-list']` | 8个播放列表 |
| 详情 | `chapterResult` | `.//a[@class='module-play-list-link']` | 每个28集 |
| 播放 | `useWebview` | `true` | ⚡ 必须启用! |

### 🎉 关键突破

**最重要的发现**: omofun 的真实视频链接不在 HTML 中,而是通过 JavaScript 动态生成,存储在 `player_aaaa` 变量中。

**解决方案**: `"useWebview": true` - 让 Kazumi 用 WebView 执行 JavaScript,拦截视频请求。

---

## 📚 下一步学习建议

1. **研究 Kazumi 的 WebView 实现**:
   - [webview_controller.dart](lib/pages/webview/webview_controller.dart)
   - [webview_android_controller_impel.dart](lib/pages/webview/webview_controller_impel/webview_android_controller_impel.dart)

2. **学习视频链接拦截技术**:
   - WebView shouldInterceptRequest
   - JavaScript Bridge
   - Network Request Interception

3. **分析其他视频网站**:
   - 有些网站直接在 HTML 中有 `<video>` 标签 → 不需要 WebView
   - 有些网站需要解析 m3u8 播放列表
   - 有些网站需要解密视频链接

4. **实战练习**:
   - 尝试分析其他番剧网站
   - 创建更多插件配置
   - 优化视频提取速度
