# Camera 模块

基于 CameraX + Jetpack Compose 实现的相机模块，采用 MVI 架构，提供拍照、相册浏览、图片查看等功能。

## 功能特性

### 拍照功能
- 4:3 比例拍摄，预览与成片一致
- 前后摄像头切换（带画面定格过渡）
- 点击对焦 + 聚焦框动画
- 双指缩放
- 拍照反馈：快门按钮缩放动画 + 画面定格 + 黑闪效果

### 相册功能
- 网格布局浏览已拍照片
- 长按进入多选模式，支持批量删除
- 照片按时间倒序排列

### 图片查看器
- 左右滑动切换图片
- 双指缩放查看细节
- 单指拖拽浏览放大后的图片
- 智能边缘检测：图片拖到边缘后继续滑动可切换图片
- 双击快速切换缩放状态

## 架构设计

```
camera/
├── CameraActivity.kt              # Activity 入口
├── viewmodel/
│   ├── CameraIntent.kt            # 用户意图（MVI - Intent）
│   ├── CameraState.kt             # UI 状态（MVI - State）
│   └── CameraViewModel.kt         # ViewModel（MVI - Model）
├── ui/
│   ├── CameraScreen.kt            # 主屏幕路由
│   ├── CameraPreview.kt           # 相机预览组件
│   ├── CameraControls.kt          # 底部控制栏
│   ├── PhotoPreviewScreen.kt      # 拍照后预览确认
│   ├── GalleryScreen.kt           # 相册网格
│   └── PhotoViewerScreen.kt       # 图片查看器
└── util/
    └── PhotoManager.kt            # 照片存储管理
```

### MVI 数据流

```
用户操作 → CameraIntent → ViewModel.handleIntent() → 更新 CameraUiState → UI 重组
```

## 使用方式

### 启动相机

```kotlin
// 方式1：直接启动 Activity
CameraActivity.start(context)

// 方式2：预热相机（可在 Application 或 Splash 页调用，加速首次打开）
CameraActivity.warmUp(context)
```

### 集成到项目

```kotlin
// app/build.gradle.kts
dependencies {
    implementation(project(":camera"))
}
```

## 技术栈

| 组件 | 技术选型 |
|-----|---------|
| 相机 | CameraX 1.4.1 |
| UI | Jetpack Compose + Material3 |
| 权限 | Accompanist Permissions |
| 图片加载 | Glide Compose |
| 架构 | MVI (Intent → ViewModel → State → UI) |

## 手势交互说明

### 相机预览页
| 手势 | 行为 |
|-----|------|
| 单击画面 | 点击对焦 |
| 双指捏合 | 缩放预览 |

### 图片查看器
| 手势 | 图片状态 | 行为 |
|-----|---------|------|
| 单指左右滑 | 未放大 | 切换图片 |
| 单指左右滑 | 已放大，未到边缘 | 拖拽查看细节 |
| 单指左右滑 | 已放大，到达边缘 | 切换图片 |
| 双指捏合 | 任意 | 缩放图片 |
| 双击 | 未放大 | 放大到 2.5x |
| 双击 | 已放大 | 恢复原始大小 |

## 照片存储

- 存储位置：应用私有目录 `filesDir/camera_photos/`
- 命名格式：`IMG_yyyyMMdd_HHmmss.jpg`
- 无需存储权限（Android 10+）
- 注意：卸载应用时照片会被删除

## 权限要求

```xml
<uses-permission android:name="android.permission.CAMERA" />
<uses-feature android:name="android.hardware.camera" android:required="false" />
```

## 关键实现细节

### 拍照画面定格
使用 `PreviewView.bitmap` 在拍照瞬间捕获当前帧，覆盖在预览上方，避免用户看到拍照后的画面卡顿。

### 摄像头切换过渡
切换摄像头时先定格当前画面，等待新摄像头绑定完成后再恢复实时预览，实现平滑过渡。

### 图片查看器边缘检测
```kotlin
val atLeftEdge = offsetX >= maxOffsetX   // 图片已拖到最左
val atRightEdge = offsetX <= -maxOffsetX // 图片已拖到最右
val shouldPassToPager = (atLeftEdge && swipingRight) || (atRightEdge && swipingLeft)
```
当图片到达边缘且继续向外滑动时，不消费事件，让 HorizontalPager 处理页面切换。
