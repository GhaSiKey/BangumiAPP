# omofun 网站视频爬取完整技术指南

> 本文档详细介绍如何从 omofun 网站 (https://omofun03.top) 爬取视频播放链接的完整流程

---

## 📋 目录

1. [网站结构概览](#网站结构概览)
2. [第1步：搜索番剧](#第1步搜索番剧)
3. [第2步：获取详情页](#第2步获取详情页)
4. [第3步：获取播放页](#第3步获取播放页)
5. [第4步：提取真实视频链接](#第4步提取真实视频链接)
6. [完整代码示例](#完整代码示例)
7. [反爬虫机制分析](#反爬虫机制分析)
8. [技术栈选择](#技术栈选择)

---

## 网站结构概览

omofun 网站采用**三层结构**：

```
搜索页面
    ↓
详情页面 (番剧信息 + 播放列表)
    ↓
播放页面 (JavaScript 动态生成视频链接)
    ↓
真实视频 URL (MP4 文件)
```

### URL 结构分析

| 阶段 | URL 模板 | 示例 |
|------|----------|------|
| **搜索页** | `https://omofun03.top/vod/search.html?wd={关键词}` | `https://omofun03.top/vod/search.html?wd=葬送` |
| **详情页** | `https://omofun03.top/vod/detail/id/{番剧ID}.html` | `https://omofun03.top/vod/detail/id/169739.html` |
| **播放页** | `https://omofun03.top/vod/play/id/{番剧ID}/sid/{线路ID}/nid/{集数}.html` | `https://omofun03.top/vod/play/id/169739/sid/6/nid/1.html` |
| **真实视频** | `https://fe-video-qc.xhscdn.com/...` | `https://fe-video-qc.xhscdn.com/athena-creator/1040g0pg3104o5f8u5q5g5pebdah3cnu7o5c94v8?filename=1.mp4` |

---

## 第1步：搜索番剧

### 1.1 请求搜索页面

**HTTP 请求**:
```http
GET /vod/search.html?wd=葬送 HTTP/1.1
Host: omofun03.top
User-Agent: Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36
Accept: text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8
Accept-Language: zh-CN,zh;q=0.9,en;q=0.8
Connection: keep-alive
```

### 1.2 解析搜索结果

**HTML 结构**:
```html
<div class="module-items module-card-items">

    <!-- 搜索结果项 1 -->
    <div class="module-card-item module-item">
        <div class="module-card-item-class">动漫</div>

        <a href="/vod/detail/id/322870.html" class="module-card-item-poster">
            <div class="module-item-cover">
                <div class="module-item-note">已完结</div>
                <div class="module-item-pic">
                    <img data-original="https://vip.dytt-img.com/upload/vod/20250922-1/93b5ef17ba35e595de6f1109d3eb5ac6.jpg"
                         alt="葬送的芙莉莲 ～●●的魔法～">
                </div>
            </div>
        </a>

        <div class="module-card-item-info">
            <div class="module-card-item-title">
                <a href="/vod/detail/id/322870.html">
                    <strong>葬送的芙莉莲 ～●●的魔法～</strong>
                </a>
            </div>
            <div class="module-info-item">
                <div class="module-info-item-content">2023/日本/日韩动漫</div>
            </div>
        </div>
    </div>

    <!-- 搜索结果项 2 -->
    <div class="module-card-item module-item">
        <div class="module-card-item-title">
            <a href="/vod/detail/id/169739.html">
                <strong>葬送的芙莉莲</strong>
            </a>
        </div>
    </div>

    <!-- 搜索结果项 3 -->
    <div class="module-card-item module-item">
        <div class="module-card-item-title">
            <a href="/vod/detail/id/214968.html">
                <strong>葬送的芙莉莲[电影解说]</strong>
            </a>
        </div>
    </div>

</div>
```

### 1.3 XPath 提取规则

| 提取目标 | XPath | 结果 |
|----------|-------|------|
| **所有搜索结果** | `//div[@class='module-card-item module-item']` | 3 个 `<div>` 节点 |
| **番剧标题** | `.//div[@class='module-card-item-title']/a/strong/text()` | `"葬送的芙莉莲 ～●●的魔法～"` |
| **详情页链接** | `.//div[@class='module-card-item-title']/a/@href` | `"/vod/detail/id/322870.html"` |

### 1.4 提取结果

```json
{
  "results": [
    {
      "title": "葬送的芙莉莲 ～●●的魔法～",
      "detailUrl": "/vod/detail/id/322870.html",
      "fullUrl": "https://omofun03.top/vod/detail/id/322870.html"
    },
    {
      "title": "葬送的芙莉莲",
      "detailUrl": "/vod/detail/id/169739.html",
      "fullUrl": "https://omofun03.top/vod/detail/id/169739.html"
    },
    {
      "title": "葬送的芙莉莲[电影解说]",
      "detailUrl": "/vod/detail/id/214968.html",
      "fullUrl": "https://omofun03.top/vod/detail/id/214968.html"
    }
  ]
}
```

---

## 第2步：获取详情页

### 2.1 请求详情页面

**HTTP 请求**:
```http
GET /vod/detail/id/169739.html HTTP/1.1
Host: omofun03.top
User-Agent: Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36
Referer: https://omofun03.top/vod/search.html?wd=葬送
Accept: text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8
Accept-Language: zh-CN,zh;q=0.9,en;q=0.8
Connection: keep-alive
```

### 2.2 解析播放列表

**HTML 结构**:
```html
<div class="module-list sort-list tab-list his-tab-list" id="panel1">

    <!-- 播放列表 1：独家超清 (sid=6) -->
    <div class="module-play-list">
        <div class="module-play-list-content module-play-list-base">

            <a class="module-play-list-link"
               href="/vod/play/id/169739/sid/6/nid/1.html"
               title="播放葬送的芙莉莲第01集">
                <span>第01集</span>
            </a>

            <a class="module-play-list-link"
               href="/vod/play/id/169739/sid/6/nid/2.html"
               title="播放葬送的芙莉莲第02集">
                <span>第02集</span>
            </a>

            <a class="module-play-list-link"
               href="/vod/play/id/169739/sid/6/nid/3.html"
               title="播放葬送的芙莉莲第03集">
                <span>第03集</span>
            </a>

            <!-- ... 共 28 集 ... -->

            <a class="module-play-list-link"
               href="/vod/play/id/169739/sid/6/nid/28.html"
               title="播放葬送的芙莉莲第28集">
                <span>第28集</span>
            </a>

        </div>
    </div>

    <!-- 播放列表 2：高清线路10 (sid=9) -->
    <div class="module-play-list">
        <div class="module-play-list-content module-play-list-base">
            <a class="module-play-list-link" href="/vod/play/id/169739/sid/9/nid/1.html">
                <span>第01集</span>
            </a>
            <!-- ... 共 28 集 ... -->
        </div>
    </div>

    <!-- 播放列表 3-8：其他线路 -->
    <!-- ... 共 8 个播放列表 ... -->

</div>
```

### 2.3 XPath 提取规则

| 提取目标 | XPath | 结果 |
|----------|-------|------|
| **所有播放列表** | `//div[@class='module-play-list']` | 8 个播放列表容器 |
| **列表内所有分集** | `.//a[@class='module-play-list-link']` | 每个列表 28 个链接 |
| **分集链接** | `.//a[@class='module-play-list-link']/@href` | `"/vod/play/id/169739/sid/6/nid/1.html"` |
| **分集名称** | `.//a[@class='module-play-list-link']/span/text()` | `"第01集"` |

### 2.4 提取结果

```json
{
  "title": "葬送的芙莉莲",
  "bangumiId": "169739",
  "playlists": [
    {
      "playlistId": 1,
      "name": "独家超清",
      "sourceId": "6",
      "episodeCount": 28,
      "episodes": [
        {
          "episodeNumber": 1,
          "name": "第01集",
          "playUrl": "/vod/play/id/169739/sid/6/nid/1.html",
          "fullUrl": "https://omofun03.top/vod/play/id/169739/sid/6/nid/1.html"
        },
        {
          "episodeNumber": 2,
          "name": "第02集",
          "playUrl": "/vod/play/id/169739/sid/6/nid/2.html",
          "fullUrl": "https://omofun03.top/vod/play/id/169739/sid/6/nid/2.html"
        }
        // ... 共 28 集
      ]
    },
    {
      "playlistId": 2,
      "name": "高清线路10",
      "sourceId": "9",
      "episodeCount": 28,
      "episodes": [
        // ...
      ]
    }
    // ... 共 8 个播放列表
  ]
}
```

---

## 第3步：获取播放页

### 3.1 请求播放页面

**HTTP 请求**:
```http
GET /vod/play/id/169739/sid/6/nid/1.html HTTP/1.1
Host: omofun03.top
User-Agent: Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36
Referer: https://omofun03.top/vod/detail/id/169739.html
Accept: text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8
Accept-Language: zh-CN,zh;q=0.9,en;q=0.8
Connection: keep-alive
```

### 3.2 播放页面关键代码

**⚠️ 重要**: 播放页面的 HTML 中**没有直接的视频链接**,需要执行 JavaScript 才能获取!

**关键 JavaScript 代码**:
```html
<script type="text/javascript">
var player_aaaa = {
    "flag": "play",
    "encrypt": 0,
    "trysee": 0,
    "points": 0,
    "link": "\/vod\/play\/id\/169739\/sid\/1\/nid\/1.html",
    "link_next": "\/vod\/play\/id\/169739\/sid\/6\/nid\/2.html",
    "link_pre": "",
    "vod_data": {
        "vod_name": "葬送的芙莉莲",
        "vod_actor": "种崎敦美,冈本信彦,东地宏树,上田耀司,市之濑加那,小林千晃",
        "vod_director": "斋藤圭一郎",
        "vod_class": "剧情,动画,奇幻,冒险"
    },
    "url": "https:\/\/fe-video-qc.xhscdn.com\/athena-creator\/1040g0pg3104o5f8u5q5g5pebdah3cnu7o5c94v8?filename=1.mp4",
    "url_next": "https:\/\/fe-video-qc.xhscdn.com\/athena-creator\/1040g0pg3104o5f8u5q605pebdah3cnu7af73c7o?filename=1.mp4",
    "from": "videojs",
    "server": "no",
    "note": "",
    "id": "169739",
    "sid": 6,
    "nid": 1
}
</script>
```

### 3.3 混淆的 JavaScript 代码

播放页面还包含**大量混淆的 JavaScript** 代码用于反爬虫:

```javascript
<script>
!function(){
    function a(a){
        var b={e:"P",w:"D",T:"y","+":"J",l:"!",t:"L",E:"E","@":"2",d:"a",b:"%",q:"l",X:"v","~":"R",5:"r","&":"X",C:"j","]":"F",a:")","^":"m",",":"~","}":"1",x:"C",c:"(",G:"@",h:"h",".":"*",L:"s","=":",",p:"g",I:"Q",1:"7",_:"u",K:"6",F:"t",2:"n",8:"=",k:"G",Z:"]",")":"b",P:"}",B:"U",S:"k",6:"i",g:":",N:"N",i:"S","%":"+","-":"Y","?":"|",4:"z","*":"-",3:"^","[":"{","(":"c",u:"B",y:"M",U:"Z",H:"[",z:"K",9:"H",7:"f",R:"x",v:"&","!":";",M:"_",Q:"9",Y:"e",o:"4",r:"A",m:".",O:"o",V:"W",J:"p",f:"d",":":"q","{":"8",W:"I",j:"?",n:"5",s:"3","|":"T",A:"V",D:"w",";":"O"};
        return a.split("").map(function(a){
            return void 0!==b[a]?b[a]:a
        }).join("")
    }
    var b=a(`wUOJxWvZzKl7_2(F6O2cYa[Xd5 F8[P!7_2(F6O2 5c2a[67cFH2Za5YF_52 FH2ZmYRJO5FL...`);
    new Function(b)()
}();
</script>
```

**混淆目的**: 防止直接用正则表达式或简单的 HTML 解析器提取视频链接。

---

## 第4步：提取真实视频链接

### 方案对比

| 方案 | 难度 | 成功率 | 性能 | 推荐度 |
|------|------|--------|------|--------|
| **方案1: 使用无头浏览器** | ⭐⭐⭐ | ✅ 100% | 🐢 慢 | ⭐⭐⭐⭐⭐ |
| **方案2: 正则提取 player_aaaa** | ⭐⭐ | ⚠️ 80% | 🚀 快 | ⭐⭐⭐ |
| **方案3: 解密 JavaScript** | ⭐⭐⭐⭐⭐ | ⚠️ 不稳定 | 🚀 快 | ⭐ |
| **方案4: 拦截网络请求** | ⭐⭐⭐⭐ | ✅ 95% | 🐢 慢 | ⭐⭐⭐⭐ |

---

### 方案1: 使用无头浏览器 (推荐)

**优点**:
- ✅ 100% 成功率
- ✅ 自动执行所有 JavaScript
- ✅ 不需要理解混淆代码
- ✅ 适应性强,网站更新后仍可工作

**缺点**:
- ❌ 性能较慢 (每个页面加载需要 2-5 秒)
- ❌ 资源占用较大

#### Python 实现 (Selenium)

```python
from selenium import webdriver
from selenium.webdriver.common.by import By
from selenium.webdriver.support.ui import WebDriverWait
from selenium.webdriver.support import expected_conditions as EC
from selenium.webdriver.chrome.options import Options
import json

def get_video_url_with_selenium(play_url):
    """
    使用 Selenium 提取视频链接

    Args:
        play_url: 播放页面 URL
        例: https://omofun03.top/vod/play/id/169739/sid/6/nid/1.html

    Returns:
        视频 URL 字符串
    """
    # 配置 Chrome 无头模式
    chrome_options = Options()
    chrome_options.add_argument('--headless')
    chrome_options.add_argument('--disable-gpu')
    chrome_options.add_argument('--no-sandbox')
    chrome_options.add_argument('--user-agent=Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36')

    driver = webdriver.Chrome(options=chrome_options)

    try:
        # 加载播放页面
        driver.get(play_url)

        # 等待 JavaScript 执行完成
        WebDriverWait(driver, 10).until(
            lambda d: d.execute_script('return typeof player_aaaa !== "undefined"')
        )

        # 提取 player_aaaa 变量
        player_data = driver.execute_script('return player_aaaa;')

        # 获取视频 URL
        video_url = player_data.get('url')

        print(f"提取成功: {video_url}")
        return video_url

    except Exception as e:
        print(f"提取失败: {e}")
        return None

    finally:
        driver.quit()

# 使用示例
play_url = "https://omofun03.top/vod/play/id/169739/sid/6/nid/1.html"
video_url = get_video_url_with_selenium(play_url)
print(f"真实视频链接: {video_url}")
```

#### Node.js 实现 (Puppeteer)

```javascript
const puppeteer = require('puppeteer');

async function getVideoUrlWithPuppeteer(playUrl) {
    /**
     * 使用 Puppeteer 提取视频链接
     *
     * @param {string} playUrl - 播放页面 URL
     * @returns {Promise<string>} 视频 URL
     */

    // 启动浏览器
    const browser = await puppeteer.launch({
        headless: true,
        args: ['--no-sandbox', '--disable-setuid-sandbox']
    });

    const page = await browser.newPage();

    try {
        // 设置 User-Agent
        await page.setUserAgent('Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36');

        // 加载播放页面
        await page.goto(playUrl, { waitUntil: 'networkidle2' });

        // 等待并提取 player_aaaa 变量
        const videoUrl = await page.evaluate(() => {
            if (typeof player_aaaa !== 'undefined') {
                return player_aaaa.url;
            }
            return null;
        });

        console.log(`提取成功: ${videoUrl}`);
        return videoUrl;

    } catch (error) {
        console.error(`提取失败: ${error}`);
        return null;

    } finally {
        await browser.close();
    }
}

// 使用示例
(async () => {
    const playUrl = 'https://omofun03.top/vod/play/id/169739/sid/6/nid/1.html';
    const videoUrl = await getVideoUrlWithPuppeteer(playUrl);
    console.log(`真实视频链接: ${videoUrl}`);
})();
```

---

### 方案2: 正则表达式提取 player_aaaa

**优点**:
- ✅ 性能快 (< 1 秒)
- ✅ 资源占用少
- ✅ 实现简单

**缺点**:
- ⚠️ 如果网站修改变量名或格式会失效
- ⚠️ 无法处理复杂的 JavaScript 混淆

#### Python 实现

```python
import requests
import re
import json

def get_video_url_with_regex(play_url):
    """
    使用正则表达式提取视频链接

    Args:
        play_url: 播放页面 URL

    Returns:
        视频 URL 字符串
    """
    headers = {
        'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36',
        'Referer': 'https://omofun03.top/',
        'Accept': 'text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8',
        'Accept-Language': 'zh-CN,zh;q=0.9,en;q=0.8',
    }

    try:
        # 请求播放页面
        response = requests.get(play_url, headers=headers, timeout=10)
        response.raise_for_status()
        html = response.text

        # 正则提取 player_aaaa 变量
        pattern = r'var\s+player_aaaa\s*=\s*({.*?});'
        match = re.search(pattern, html, re.DOTALL)

        if match:
            # 解析 JSON
            player_json = match.group(1)
            player_data = json.loads(player_json)

            # 获取视频 URL
            video_url = player_data.get('url')

            # 处理转义字符
            video_url = video_url.replace('\\/', '/')

            print(f"提取成功: {video_url}")
            return video_url
        else:
            print("未找到 player_aaaa 变量")
            return None

    except Exception as e:
        print(f"提取失败: {e}")
        return None

# 使用示例
play_url = "https://omofun03.top/vod/play/id/169739/sid/6/nid/1.html"
video_url = get_video_url_with_regex(play_url)
print(f"真实视频链接: {video_url}")
```

#### 正则表达式说明

```regex
var\s+player_aaaa\s*=\s*({.*?});

解释:
  var\s+           匹配 "var" 后面的空白字符
  player_aaaa      匹配变量名
  \s*=\s*          匹配等号及其周围的空白
  ({.*?})          非贪婪匹配 JSON 对象 (用括号捕获)
  ;                匹配分号结尾
```

---

### 方案3: 拦截网络请求

**优点**:
- ✅ 可以捕获所有视频请求
- ✅ 适用于多种视频格式 (MP4, M3U8, FLV)

**缺点**:
- ⚠️ 实现复杂
- ⚠️ 需要深入理解浏览器机制

#### Python 实现 (Selenium + BrowserMob Proxy)

```python
from selenium import webdriver
from selenium.webdriver.chrome.options import Options
from browsermobproxy import Server
import json

def get_video_url_with_network_intercept(play_url):
    """
    通过拦截网络请求获取视频链接

    Args:
        play_url: 播放页面 URL

    Returns:
        视频 URL 字符串
    """
    # 启动 BrowserMob Proxy
    server = Server("/path/to/browsermob-proxy")
    server.start()
    proxy = server.create_proxy()

    # 配置 Chrome
    chrome_options = Options()
    chrome_options.add_argument(f'--proxy-server={proxy.proxy}')
    chrome_options.add_argument('--user-agent=Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36')

    driver = webdriver.Chrome(options=chrome_options)

    try:
        # 开始捕获网络请求
        proxy.new_har("omofun", options={'captureHeaders': True, 'captureContent': True})

        # 加载播放页面
        driver.get(play_url)

        # 等待页面加载
        import time
        time.sleep(5)

        # 分析网络请求
        har = proxy.har
        video_url = None

        for entry in har['log']['entries']:
            url = entry['request']['url']

            # 检测视频文件
            if url.endswith('.mp4') or url.endswith('.m3u8'):
                video_url = url
                break

        if video_url:
            print(f"拦截到视频链接: {video_url}")
            return video_url
        else:
            print("未拦截到视频链接")
            return None

    except Exception as e:
        print(f"提取失败: {e}")
        return None

    finally:
        driver.quit()
        server.stop()

# 使用示例
play_url = "https://omofun03.top/vod/play/id/169739/sid/6/nid/1.html"
video_url = get_video_url_with_network_intercept(play_url)
print(f"真实视频链接: {video_url}")
```

---

## 完整代码示例

### Python 完整爬虫 (推荐)

```python
#!/usr/bin/env python3
# -*- coding: utf-8 -*-

"""
omofun 完整视频爬虫
支持搜索、播放列表提取、视频链接获取
"""

import requests
from lxml import etree
from selenium import webdriver
from selenium.webdriver.chrome.options import Options
from selenium.webdriver.support.ui import WebDriverWait
import json
from typing import List, Dict, Optional
import time

class OmofunCrawler:
    """omofun 视频爬虫"""

    BASE_URL = "https://omofun03.top"

    def __init__(self, use_selenium=True):
        """
        初始化爬虫

        Args:
            use_selenium: 是否使用 Selenium (推荐开启以提取视频链接)
        """
        self.use_selenium = use_selenium
        self.session = requests.Session()
        self.session.headers.update({
            'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36',
            'Accept': 'text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8',
            'Accept-Language': 'zh-CN,zh;q=0.9,en;q=0.8',
        })

        if self.use_selenium:
            self._init_selenium()

    def _init_selenium(self):
        """初始化 Selenium WebDriver"""
        chrome_options = Options()
        chrome_options.add_argument('--headless')
        chrome_options.add_argument('--disable-gpu')
        chrome_options.add_argument('--no-sandbox')
        chrome_options.add_argument('--user-agent=Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36')

        self.driver = webdriver.Chrome(options=chrome_options)

    def search(self, keyword: str) -> List[Dict]:
        """
        搜索番剧

        Args:
            keyword: 搜索关键词

        Returns:
            搜索结果列表
        """
        search_url = f"{self.BASE_URL}/vod/search.html?wd={keyword}"

        try:
            response = self.session.get(search_url, timeout=10)
            response.raise_for_status()

            # 解析 HTML
            html = etree.HTML(response.text)

            # XPath 提取
            result_nodes = html.xpath("//div[@class='module-card-item module-item']")

            results = []
            for node in result_nodes:
                title_nodes = node.xpath(".//div[@class='module-card-item-title']/a/strong/text()")
                url_nodes = node.xpath(".//div[@class='module-card-item-title']/a/@href")

                if title_nodes and url_nodes:
                    results.append({
                        'title': title_nodes[0].strip(),
                        'detailUrl': url_nodes[0],
                        'fullUrl': self.BASE_URL + url_nodes[0]
                    })

            print(f"搜索 '{keyword}' 找到 {len(results)} 个结果")
            return results

        except Exception as e:
            print(f"搜索失败: {e}")
            return []

    def get_playlists(self, detail_url: str) -> List[Dict]:
        """
        获取播放列表

        Args:
            detail_url: 详情页 URL

        Returns:
            播放列表数据
        """
        if not detail_url.startswith('http'):
            detail_url = self.BASE_URL + detail_url

        try:
            response = self.session.get(detail_url, timeout=10)
            response.raise_for_status()

            # 解析 HTML
            html = etree.HTML(response.text)

            # XPath 提取播放列表
            playlist_nodes = html.xpath("//div[@class='module-play-list']")

            playlists = []
            for idx, playlist_node in enumerate(playlist_nodes, start=1):
                # 提取所有分集
                episode_nodes = playlist_node.xpath(".//a[@class='module-play-list-link']")

                episodes = []
                for ep_node in episode_nodes:
                    ep_url = ep_node.xpath("./@href")[0]
                    ep_name = ep_node.xpath("./span/text()")[0]

                    episodes.append({
                        'name': ep_name.strip(),
                        'playUrl': ep_url,
                        'fullUrl': self.BASE_URL + ep_url if not ep_url.startswith('http') else ep_url
                    })

                playlists.append({
                    'playlistId': idx,
                    'name': f"播放列表{idx}",
                    'episodeCount': len(episodes),
                    'episodes': episodes
                })

            print(f"找到 {len(playlists)} 个播放列表,共 {sum(p['episodeCount'] for p in playlists)} 集")
            return playlists

        except Exception as e:
            print(f"获取播放列表失败: {e}")
            return []

    def get_video_url(self, play_url: str) -> Optional[str]:
        """
        提取真实视频链接

        Args:
            play_url: 播放页 URL

        Returns:
            视频 URL 或 None
        """
        if not play_url.startswith('http'):
            play_url = self.BASE_URL + play_url

        if self.use_selenium:
            return self._get_video_url_with_selenium(play_url)
        else:
            return self._get_video_url_with_regex(play_url)

    def _get_video_url_with_selenium(self, play_url: str) -> Optional[str]:
        """使用 Selenium 提取视频链接"""
        try:
            self.driver.get(play_url)

            # 等待 JavaScript 执行
            WebDriverWait(self.driver, 10).until(
                lambda d: d.execute_script('return typeof player_aaaa !== "undefined"')
            )

            # 提取 player_aaaa
            player_data = self.driver.execute_script('return player_aaaa;')
            video_url = player_data.get('url', '').replace('\\/', '/')

            print(f"提取视频链接成功: {video_url[:80]}...")
            return video_url

        except Exception as e:
            print(f"提取视频链接失败: {e}")
            return None

    def _get_video_url_with_regex(self, play_url: str) -> Optional[str]:
        """使用正则表达式提取视频链接"""
        import re

        try:
            response = self.session.get(play_url, timeout=10)
            response.raise_for_status()

            pattern = r'var\s+player_aaaa\s*=\s*({.*?});'
            match = re.search(pattern, response.text, re.DOTALL)

            if match:
                player_data = json.loads(match.group(1))
                video_url = player_data.get('url', '').replace('\\/', '/')

                print(f"提取视频链接成功: {video_url[:80]}...")
                return video_url
            else:
                print("未找到 player_aaaa 变量")
                return None

        except Exception as e:
            print(f"提取视频链接失败: {e}")
            return None

    def close(self):
        """关闭爬虫"""
        if self.use_selenium and hasattr(self, 'driver'):
            self.driver.quit()


# ========== 使用示例 ==========

if __name__ == "__main__":
    # 初始化爬虫
    crawler = OmofunCrawler(use_selenium=True)

    try:
        # 第1步: 搜索番剧
        print("\n=== 第1步: 搜索番剧 ===")
        search_results = crawler.search("葬送的芙莉莲")

        if not search_results:
            print("搜索结果为空")
            exit(1)

        # 显示搜索结果
        for i, result in enumerate(search_results, start=1):
            print(f"{i}. {result['title']}")
            print(f"   详情页: {result['fullUrl']}")

        # 第2步: 获取播放列表
        print("\n=== 第2步: 获取播放列表 ===")
        first_result = search_results[0]
        playlists = crawler.get_playlists(first_result['detailUrl'])

        if not playlists:
            print("播放列表为空")
            exit(1)

        # 显示播放列表
        for playlist in playlists:
            print(f"\n{playlist['name']} ({playlist['episodeCount']}集)")
            for ep in playlist['episodes'][:3]:  # 只显示前3集
                print(f"  - {ep['name']}: {ep['fullUrl']}")
            if playlist['episodeCount'] > 3:
                print(f"  ... 还有 {playlist['episodeCount'] - 3} 集")

        # 第3步: 提取视频链接
        print("\n=== 第3步: 提取视频链接 ===")
        first_episode = playlists[0]['episodes'][0]
        print(f"正在提取 {first_episode['name']} 的视频链接...")

        video_url = crawler.get_video_url(first_episode['playUrl'])

        if video_url:
            print(f"\n✅ 成功提取真实视频链接:")
            print(f"   {video_url}")
            print(f"\n可以使用以下命令下载:")
            print(f"   wget '{video_url}' -O 葬送的芙莉莲_第01集.mp4")
            print(f"   或")
            print(f"   ffmpeg -i '{video_url}' -c copy 葬送的芙莉莲_第01集.mp4")
        else:
            print("❌ 提取失败")

    finally:
        # 关闭爬虫
        crawler.close()
```

### 运行输出示例

```bash
$ python omofun_crawler.py

=== 第1步: 搜索番剧 ===
搜索 '葬送的芙莉莲' 找到 3 个结果
1. 葬送的芙莉莲 ～●●的魔法～
   详情页: https://omofun03.top/vod/detail/id/322870.html
2. 葬送的芙莉莲
   详情页: https://omofun03.top/vod/detail/id/169739.html
3. 葬送的芙莉莲[电影解说]
   详情页: https://omofun03.top/vod/detail/id/214968.html

=== 第2步: 获取播放列表 ===
找到 8 个播放列表,共 224 集

播放列表1 (28集)
  - 第01集: https://omofun03.top/vod/play/id/169739/sid/6/nid/1.html
  - 第02集: https://omofun03.top/vod/play/id/169739/sid/6/nid/2.html
  - 第03集: https://omofun03.top/vod/play/id/169739/sid/6/nid/3.html
  ... 还有 25 集

播放列表2 (28集)
  - 第01集: https://omofun03.top/vod/play/id/169739/sid/9/nid/1.html
  ...

=== 第3步: 提取视频链接 ===
正在提取 第01集 的视频链接...
提取视频链接成功: https://fe-video-qc.xhscdn.com/athena-creator/1040g0pg3104o5f8u5q5g5pebdah3...

✅ 成功提取真实视频链接:
   https://fe-video-qc.xhscdn.com/athena-creator/1040g0pg3104o5f8u5q5g5pebdah3cnu7o5c94v8?filename=1.mp4

可以使用以下命令下载:
   wget 'https://fe-video-qc.xhscdn.com/athena-creator/1040g0pg3104o5f8u5q5g5pebdah3cnu7o5c94v8?filename=1.mp4' -O 葬送的芙莉莲_第01集.mp4
   或
   ffmpeg -i 'https://fe-video-qc.xhscdn.com/athena-creator/1040g0pg3104o5f8u5q5g5pebdah3cnu7o5c94v8?filename=1.mp4' -c copy 葬送的芙莉莲_第01集.mp4
```

---

## 反爬虫机制分析

### 1. JavaScript 混淆

**机制**: 播放页面使用字符替换算法混淆核心代码。

**示例**:
```javascript
function a(a){
    var b={e:"P",w:"D",T:"y",...};  // 字符映射表
    return a.split("").map(function(a){
        return void 0!==b[a]?b[a]:a
    }).join("")
}
var b=a(`wUOJxWvZzKl7_2(F6O2cYa...`);  // 混淆后的代码
new Function(b)();  // 执行解密后的代码
```

**破解方法**:
- 方法1: 使用 WebView 让浏览器自动执行
- 方法2: 逆向解密算法 (不推荐,维护成本高)

### 2. 动态域名

**机制**: 视频 CDN 域名不固定,经常变化。

**示例**:
- `https://fe-video-qc.xhscdn.com/...`
- `https://fe-video.xhscdn.com/...`
- `https://sns-video-bd.xhscdn.com/...`

**应对方法**: 实时提取,不要硬编码域名。

### 3. Referer 检查

**机制**: 视频 URL 可能检查 HTTP Referer 头。

**应对方法**:
```python
headers = {
    'Referer': 'https://omofun03.top/',
    'User-Agent': 'Mozilla/5.0 ...'
}
requests.get(video_url, headers=headers)
```

### 4. 防盗链

**机制**: 视频 URL 带有时效性 token 参数。

**特征**:
```
https://fe-video-qc.xhscdn.com/athena-creator/1040g0pg3104o5f8u5q5g5pebdah3cnu7o5c94v8?filename=1.mp4
                                                     ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
                                                     这部分可能是时效性 token
```

**应对方法**: 提取后立即下载,不要缓存 URL。

---

## 技术栈选择

### 不同编程语言的实现方案

| 语言 | HTML 解析库 | 无头浏览器 | 推荐度 |
|------|------------|-----------|--------|
| **Python** | `lxml`, `BeautifulSoup` | `Selenium`, `Playwright` | ⭐⭐⭐⭐⭐ |
| **JavaScript/Node.js** | `cheerio`, `jsdom` | `Puppeteer`, `Playwright` | ⭐⭐⭐⭐⭐ |
| **Kotlin/Java (Android)** | `Jsoup` | `WebView` | ⭐⭐⭐⭐ |
| **Dart (Flutter)** | `html`, `xpath_selector` | `WebView` | ⭐⭐⭐⭐ |
| **Go** | `goquery` | `chromedp` | ⭐⭐⭐ |

### Python 库安装

```bash
# HTML 解析
pip install lxml requests

# Selenium (推荐)
pip install selenium
# 下载 ChromeDriver: https://chromedriver.chromium.org/

# 或使用 Playwright (更现代)
pip install playwright
playwright install chromium
```

### Node.js 库安装

```bash
# HTML 解析
npm install cheerio axios

# Puppeteer (推荐)
npm install puppeteer

# 或使用 Playwright
npm install playwright
npx playwright install chromium
```

---

## 总结

### 完整流程图

```
用户输入关键词: "葬送的芙莉莲"
         ↓
┌────────────────────────────────┐
│  第1步: 搜索                    │
│  GET /vod/search.html?wd=葬送  │
│  XPath 提取搜索结果             │
└────────────────────────────────┘
         ↓
用户选择: "葬送的芙莉莲" (id=169739)
         ↓
┌────────────────────────────────┐
│  第2步: 详情页                  │
│  GET /vod/detail/id/169739.html│
│  XPath 提取播放列表 (8个列表)   │
└────────────────────────────────┘
         ↓
用户选择: "播放列表1" → "第01集"
         ↓
┌────────────────────────────────┐
│  第3步: 播放页                  │
│  GET /vod/play/id/169739/       │
│      sid/6/nid/1.html          │
│  执行 JavaScript                │
└────────────────────────────────┘
         ↓
         ↓ (JavaScript 创建 player_aaaa 变量)
         ↓
┌────────────────────────────────┐
│  第4步: 提取视频链接            │
│  方法1: Selenium 提取变量       │
│  方法2: 正则匹配 player_aaaa    │
│  方法3: 拦截网络请求            │
└────────────────────────────────┘
         ↓
真实视频 URL:
https://fe-video-qc.xhscdn.com/
athena-creator/1040g0pg3104o5f8u5q5g5pebdah3cnu7o5c94v8?filename=1.mp4
         ↓
下载视频或在线播放
```

### 关键要点

1. **必须执行 JavaScript**: 视频链接不在 HTML 中,需要执行 JavaScript 才能获取
2. **推荐使用无头浏览器**: Selenium 或 Puppeteer 是最稳定的方案
3. **注意反爬虫**: 设置正确的 User-Agent 和 Referer
4. **URL 时效性**: 视频链接可能有时效,提取后立即使用

### 性能对比

| 方案 | 搜索 | 详情页 | 播放页 | 总耗时 |
|------|------|--------|--------|--------|
| **纯 HTTP + XPath** | 0.5s | 0.5s | ❌ 失败 | - |
| **HTTP + Selenium** | 0.5s | 0.5s | 3s | ~4s |
| **HTTP + 正则** | 0.5s | 0.5s | 0.5s | ~1.5s (不稳定) |

### 法律声明

⚠️ **重要提示**:
- 本文档仅供技术学习和研究使用
- 请遵守网站的 robots.txt 和服务条款
- 不要用于商业目的或大规模爬取
- 尊重版权,支持正版

---

**文档版本**: 1.0
**最后更新**: 2025-12-11
**适用网站**: omofun03.top
