# omofun 完整插件配置

## 🎉 成功!播放列表结构已找到!

### 关键 HTML 结构分析

```html
<!-- 播放列表容器 -->
<div class="module-list sort-list tab-list his-tab-list" id="panel1">

    <!-- 单个播放列表 -->
    <div class="module-play-list">

        <!-- 分集链接容器 -->
        <div class="module-play-list-content module-play-list-base">

            <!-- 每一集的链接 -->
            <a class="module-play-list-link" href="/vod/play/id/169739/sid/6/nid/1.html" title="播放葬送的芙莉莲第01集">
                <span>第01集</span>
            </a>

            <a class="module-play-list-link" href="/vod/play/id/169739/sid/6/nid/2.html" title="播放葬送的芙莉莲第02集">
                <span>第02集</span>
            </a>

            <a class="module-play-list-link" href="/vod/play/id/169739/sid/6/nid/3.html" title="播放葬送的芙莉莲第03集">
                <span>第03集</span>
            </a>

            <!-- 更多集数... -->
        </div>
    </div>

</div>
```

**重要发现**:
- 有 **8 个播放列表** (独家超清、高清线路10、高清线路2等)
- 每个播放列表都有 **28 集**
- 所有播放列表的 HTML 结构完全相同
- 分集链接格式: `/vod/play/id/169739/sid/6/nid/1.html`

---

## ✅ 完整的 omofun.json 插件配置

```json
{
    "api": "1",
    "type": "anime",
    "name": "omofun",
    "version": "1.0",
    "muliSources": true,
    "useWebview": false,
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

## 🧪 XPath 详细说明

### 1. chapterRoads (播放列表容器)

```xpath
//div[@class='module-play-list']
```

**匹配到**: 8 个播放列表容器

**验证** (浏览器 Console):
```javascript
$x("//div[@class='module-play-list']").length
// 应该返回: 8
```

### 2. chapterResult (分集链接)

```xpath
.//a[@class='module-play-list-link']
```

**说明**:
- `.//` - 从当前播放列表节点开始查找
- `a[@class='module-play-list-link']` - 精确匹配 class

**验证** (浏览器 Console):
```javascript
// 获取第一个播放列表
var firstPlaylist = $x("//div[@class='module-play-list']")[0];

// 提取该列表的所有分集链接
$x(".//a[@class='module-play-list-link']", firstPlaylist).length
// 应该返回: 28

// 提取所有链接的 href
$x(".//a[@class='module-play-list-link']/@href", firstPlaylist).map(a => a.value)
// 应该返回:
// ["/vod/play/id/169739/sid/6/nid/1.html", "/vod/play/id/169739/sid/6/nid/2.html", ...]

// 提取所有分集名称
$x(".//a[@class='module-play-list-link']/span/text()", firstPlaylist).map(t => t.textContent)
// 应该返回:
// ["第01集", "第02集", "第03集", ...]
```

---

## 📊 数据流完整演示

### 第1步: 搜索 "葬送"

```
URL: https://omofun03.top/vod/search.html?wd=葬送

XPath: //div[@class='module-card-item module-item']
结果: 3 个搜索结果

XPath: .//div[@class='module-card-item-title']/a/strong/text()
结果: ["葬送的芙莉莲 ～●●的魔法～", "葬送的芙莉莲", "葬送的芙莉莲[电影解说]"]

XPath: .//div[@class='module-card-item-title']/a/@href
结果: ["/vod/detail/id/322870.html", "/vod/detail/id/169739.html", "/vod/detail/id/214968.html"]
```

### 第2步: 访问详情页

```
URL: https://omofun03.top/vod/detail/id/169739.html

XPath: //div[@class='module-play-list']
结果: 8 个播放列表

播放列表1 (sid=6, 独家超清):
  XPath: .//a[@class='module-play-list-link']/@href
  结果: [
    "/vod/play/id/169739/sid/6/nid/1.html",
    "/vod/play/id/169739/sid/6/nid/2.html",
    ...
    "/vod/play/id/169739/sid/6/nid/28.html"
  ]

播放列表2 (sid=9, 高清线路10):
  结果: [
    "/vod/play/id/169739/sid/9/nid/1.html",
    "/vod/play/id/169739/sid/9/nid/2.html",
    ...
  ]

... (共8个播放列表)
```

### 第3步: Kazumi 解析后的数据结构

```dart
List<Road> roadList = [
  Road(
    name: "播放列表1",
    data: [
      "/vod/play/id/169739/sid/6/nid/1.html",
      "/vod/play/id/169739/sid/6/nid/2.html",
      "/vod/play/id/169739/sid/6/nid/3.html",
      // ... 共28个
    ],
    identifier: [
      "第01集", "第02集", "第03集", ... // 共28个
    ]
  ),
  Road(
    name: "播放列表2",
    data: [...], // sid=9 的28个链接
    identifier: [...]
  ),
  // ... 共8个 Road
];
```

### 第4步: 用户选择播放

```
用户点击: "播放列表1" 的 "第01集"
访问: https://omofun03.top/vod/play/id/169739/sid/6/nid/1.html

这个页面会包含真实的视频播放器或视频链接
```

---

## 🎯 浏览器完整验证步骤

### 验证搜索 (第1步)

```javascript
// 打开: https://omofun03.top/vod/search.html?wd=葬送

// 1. 验证搜索结果数量
$x("//div[@class='module-card-item module-item']").length
// 应该: 3

// 2. 验证提取标题
$x("//div[@class='module-card-item module-item']//div[@class='module-card-item-title']/a/strong/text()").map(t => t.textContent)
// 应该: ["葬送的芙莉莲 ～●●的魔法～", "葬送的芙莉莲", "葬送的芙莉莲[电影解说]"]

// 3. 验证提取链接
$x("//div[@class='module-card-item module-item']//div[@class='module-card-item-title']/a/@href").map(a => a.value)
// 应该: ["/vod/detail/id/322870.html", "/vod/detail/id/169739.html", "/vod/detail/id/214968.html"]
```

### 验证详情页 (第2步)

```javascript
// 打开: https://omofun03.top/vod/detail/id/169739.html

// 1. 验证播放列表数量
$x("//div[@class='module-play-list']").length
// 应该: 8

// 2. 验证第一个播放列表的分集数量
var firstPlaylist = $x("//div[@class='module-play-list']")[0];
$x(".//a[@class='module-play-list-link']", firstPlaylist).length
// 应该: 28

// 3. 验证提取分集链接
$x(".//a[@class='module-play-list-link']/@href", firstPlaylist).map(a => a.value)
// 应该: ["/vod/play/id/169739/sid/6/nid/1.html", "/vod/play/id/169739/sid/6/nid/2.html", ...]

// 4. 验证提取分集名称
$x(".//a[@class='module-play-list-link']/span/text()", firstPlaylist).map(t => t.textContent)
// 应该: ["第01集", "第02集", "第03集", ...]

// 5. 验证所有播放列表
$x("//div[@class='module-play-list']").forEach((playlist, index) => {
    var episodes = $x(".//a[@class='module-play-list-link']", playlist);
    console.log(`播放列表${index + 1}: ${episodes.length} 集`);
});
// 应该输出:
// 播放列表1: 28 集
// 播放列表2: 28 集
// ...
// 播放列表8: 28 集
```

---

## 🚀 如何在 Kazumi 中使用

### 1. 创建插件文件

文件名: `omofun.json`

位置: `/Users/shiqigao/VSCodeProjects/Kazumi/assets/plugins/omofun.json`

内容: (上面的完整 JSON 配置)

### 2. 重新编译 Kazumi

```bash
cd /Users/shiqigao/VSCodeProjects/Kazumi
flutter pub get
flutter run
```

### 3. 测试流程

1. 打开 Kazumi
2. 搜索 "葬送的芙莉莲"
3. 查看是否有来自 "omofun" 源的结果
4. 点击进入详情页
5. 查看是否解析出 8 个播放列表
6. 点击 "第01集" 测试播放

---

## 🎊 总结

### ✅ 已完成

- [x] 搜索 XPath 配置
- [x] 详情页 XPath 配置
- [x] 完整的 omofun.json 插件
- [x] 浏览器验证方法

### 📝 XPath 配置对照表

| 配置项 | XPath | 说明 |
|-------|-------|-----|
| `searchList` | `//div[@class='module-card-item module-item']` | 搜索结果列表 |
| `searchName` | `.//div[@class='module-card-item-title']/a/strong` | 番剧标题 |
| `searchResult` | `.//div[@class='module-card-item-title']/a` | 详情页链接 |
| `chapterRoads` | `//div[@class='module-play-list']` | 播放列表容器 (8个) |
| `chapterResult` | `.//a[@class='module-play-list-link']` | 分集链接 (每个28集) |

### 🎯 下一步

配置已经完成!现在可以:
1. 保存 `omofun.json` 到 Kazumi 的 assets/plugins/ 目录
2. 重新运行 Kazumi 测试
3. 如果需要,我可以继续帮你分析播放页面提取真实视频链接

