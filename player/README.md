# Player 模块

视频播放器模块，目标是实现类似 BiliBili 的播放器体验（含弹幕）。

## 技术栈

- **UI框架**: Jetpack Compose
- **播放器**: Media3 ExoPlayer 1.5.1
- **架构**: MVI (Model-View-Intent)

## 模块结构

```
player/
├── src/main/java/com/gaoshiqi/player/
│   ├── PlayerTestActivity.kt      # 测试入口
│   ├── ui/
│   │   ├── PlayerScreen.kt        # 主播放页面
│   │   ├── VideoPlayer.kt         # ExoPlayer封装
│   │   ├── PlayerControls.kt      # 播放控制栏
│   │   └── theme/
│   │       └── Theme.kt           # Compose主题
│   └── viewmodel/
│       ├── PlayerState.kt         # 播放状态定义
│       ├── PlayerIntent.kt        # 用户意图定义
│       └── PlayerViewModel.kt     # MVI ViewModel
└── src/main/res/
    └── drawable/
        ├── ic_play.xml            # 播放图标
        └── ic_pause.xml           # 暂停图标
```

## 开发记录

### 2024-12-31 - 第一阶段：最简播放功能

#### 完成内容

1. **项目配置**
   - 在 `libs.versions.toml` 添加 Compose BOM 和 Media3 依赖
   - 根项目添加 `kotlin-compose` 插件
   - player 模块启用 Compose 编译

2. **MVI 架构**
   - `PlayerState`: 定义播放状态（Idle, Loading, Playing, Paused, Error）
   - `PlayerIntent`: 定义用户意图（SetUrl, Play, Pause, SeekTo, Retry, Release）
   - `PlayerViewModel`: 封装 ExoPlayer 实例和状态管理

3. **Compose UI**
   - `VideoPlayer`: 使用 AndroidView 包装 ExoPlayer PlayerView
   - `PlayerControls`: 播放/暂停按钮 + 进度条
   - `PlayerScreen`: 整合播放器、控制栏、URL输入

4. **集成入口**
   - 在 TestActivity（开发者选项）添加"视频播放器测试"按钮

5. **ExoPlayer 日志系统**
   - 添加 `PlayerLog` 数据类，支持分类日志（LIFECYCLE, PLAYBACK, MEDIA, ERROR, USER_ACTION）
   - 监听 ExoPlayer 完整生命周期回调：
     - `onPlaybackStateChanged` - 播放状态变化（IDLE/BUFFERING/READY/ENDED）
     - `onIsPlayingChanged` - 播放/暂停切换
     - `onPlayerError` - 错误处理（含详细错误码解析）
     - `onVideoSizeChanged` - 视频尺寸
     - `onTracksChanged` - 音视频轨道信息
     - `onTimelineChanged` - 时间线变化
     - `onMediaItemTransition` - 媒体切换
     - `onRenderedFirstFrame` - 首帧渲染
     - `onPositionDiscontinuity` - 播放位置跳变
   - UI 展示带颜色标签的实时日志列表

#### 当前功能

- [x] 输入视频URL
- [x] 播放/暂停控制
- [x] 进度条拖动
- [x] 播放状态显示
- [x] 错误处理
- [x] 快捷测试按钮（芙莉莲、间谍过家家、鬼灭之刃等）
- [x] ExoPlayer 生命周期日志展示

#### 待开发功能

- [ ] 横屏全屏播放
- [ ] 视频缓存
- [ ] 弹幕系统
- [ ] 清晰度切换
- [ ] 倍速播放
- [ ] 列表内播放
- [ ] 视频源解析

## 使用方式

1. 进入应用的"设置"页面
2. 点击"开发者选项"
3. 点击"视频播放器测试"
4. 输入视频URL（支持 MP4、HLS 等格式）
5. 点击"播放"按钮

## 测试视频

可以使用以下公开测试视频URL：

```
# Big Buck Bunny (MP4)
https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4

# Sintel (MP4)
https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/Sintel.mp4

# HLS 测试流
https://test-streams.mux.dev/x36xhzz/x36xhzz.m3u8

# 芙莉莲
https://fe-video-qc.xhscdn.com/athena-creator/1040g0pg3104o5f8u5q5g5pebdah3cnu7o5c94v8?filename=1.mp4

#间谍过家家
https://sns-video-hw.xhscdn.com/spectrum/1040g0jg31n7jtmot4u005p2qf72k4m7r92mijqo

# 鬼灭之刃剧场版
https://hn.bfvvs.com/play/b688Ynle/index.m3u8
```

## 依赖版本

| 库 | 版本 |
|---|---|
| Compose BOM | 2024.12.01 |
| Media3 ExoPlayer | 1.5.1 |
| Lifecycle ViewModel Compose | 2.9.1 |
