# 🎵 音乐播放器 (Music Player)

<div align="center">

一款功能完整的 Android 音乐播放器应用，支持本地音乐、在线音乐和歌词同步显示。

[![Android](https://img.shields.io/badge/Android-7.0%2B-green.svg)](https://android.com)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.0.21-blue.svg)](https://kotlinlang.org)
[![API](https://img.shields.io/badge/API-24%2B-brightgreen.svg)](https://android.com)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

[功能特性](#-功能特性) • [快速开始](#-快速开始) • [技术栈](#-技术栈) • [项目结构](#-项目结构) • [使用说明](#-使用说明)

</div>

---

## ✨ 功能特性

| 功能 | 描述 |
|------|------|
| 🎵 **本地音乐播放** | 自动扫描设备上的所有音乐文件 |
| 🌐 **在线音乐搜索** | 搜索并播放在线音乐（需配置 API） |
| 📝 **歌词同步显示** | 支持 LRC 格式歌词的解析和同步滚动 |
| 📋 **播放列表** | 管理和播放音乐列表 |
| 🔔 **后台播放** | 支持前台服务和通知栏控制 |
| 🎨 **Material Design 3** | 精美的现代化界面设计 |
| ⏮️⏭️ **播放控制** | 播放、暂停、上一首、下一首、进度调节 |

---

## 🚀 快速开始

### 系统要求

- **最低 Android 版本**: Android 7.0 (API 24)
- **目标 Android 版本**: Android 14 (API 34)
- **存储空间**: 约 50 MB

### 下载安装

#### 方式一：下载 APK（推荐）

1. 前往 [Releases](../../releases) 页面下载最新的 `app-release.apk`
2. 在安卓手机上安装 APK 文件
3. 授予存储权限和通知权限
4. 开始使用！

#### 方式二：GitHub 自动构建

详见 [BUILD_GUIDE.md](BUILD_GUIDE.md)

#### 方式三：自己构建

```bash
# 克隆仓库
git clone https://github.com/你的用户名/MusicPlayer.git

# 进入项目目录
cd MusicPlayer

# 构建 APK
./gradlew assembleDebug

# APK 位置
app/build/outputs/apk/debug/app-debug.apk
```

---

## 🛠️ 技术栈

| 类别 | 技术 |
|------|------|
| **语言** | Kotlin 2.0.21 |
| **最低 SDK** | Android 7.0 (API 24) |
| **目标 SDK** | Android 14 (API 34) |
| **架构** | MVVM + Repository Pattern |
| **播放引擎** | ExoPlayer (Media3) 1.4.1 |
| **网络请求** | Retrofit 2.11.0 + OkHttp 4.12.0 |
| **图片加载** | Glide 4.16.0 |
| **异步处理** | Coroutines 1.8.1 + Flow |
| **UI 组件** | Material Components 1.12.0 |

---

## 📂 项目结构

```
app/src/main/java/com/musicplayer/
├── adapter/              # RecyclerView 适配器
├── api/                  # 在线音乐 API
├── lyrics/               # 歌词解析器
├── model/                # 数据模型
├── player/               # 播放器管理器
├── scanner/              # 本地音乐扫描器
├── service/              # 后台播放服务
└── ui/                   # UI 组件和 Activity
    ├── MainActivity.kt           # 主界面
    ├── FullPlayerActivity.kt     # 全屏播放器
    ├── LocalMusicFragment.kt     # 本地音乐页
    ├── OnlineMusicFragment.kt    # 在线音乐页
    └── LyricsView.kt             # 歌词视图
```

---

## 📖 使用说明

### 🎵 本地音乐

1. 首次打开应用会请求存储权限
2. 授权后应用自动扫描设备上的音乐文件
3. 点击任意歌曲开始播放
4. 点击底部播放器可打开全屏播放界面

### 🌐 在线音乐

> ⚠️ **注意**：需要配置有效的音乐 API 才能使用

1. 切换到"在线音乐"标签
2. 在搜索框中输入歌曲名或歌手名
3. 点击搜索结果中的歌曲开始播放
4. 播放前会自动获取音乐 URL

### 📝 歌词显示

1. 在全屏播放器界面，歌词会自动显示
2. 歌词会随播放进度自动滚动
3. 当前播放的歌词行会高亮显示

---

## ⚙️ 配置在线音乐 API

默认配置使用示例 API 地址，实际使用时需要替换为可用的音乐 API：

### 配置步骤

1. 打开 `app/src/main/java/com/musicplayer/api/MusicApiService.kt`
2. 修改 `BASE_URL` 为实际的音乐 API 地址
3. 根据API文档调整请求和响应的数据模型

### 推荐的免费音乐 API

- 网易云音乐 API
- QQ 音乐 API
- 其他第三方音乐 API

---

## 📄 权限说明

应用需要以下权限：

| 权限 | 用途 |
|------|------|
| `INTERNET` | 在线音乐播放 |
| `READ_EXTERNAL_STORAGE` / `READ_MEDIA_AUDIO` | 访问本地音乐文件 |
| `FOREGROUND_SERVICE` | 后台音乐播放 |
| `POST_NOTIFICATIONS` | 显示播放通知 |

---

## ⚠️ 注意事项

1. **在线音乐 API**: 需要配置有效的音乐 API 才能使用在线音乐功能
2. **歌词文件**: 歌词需要从 API 获取或手动添加，支持标准 LRC 格式
3. **权限**: Android 13+ 需要使用 `READ_MEDIA_AUDIO` 权限
4. **网络**: 已配置 `usesCleartextTraffic` 支持 HTTP 请求

---

## 🚀 未来改进

- [ ] 添加播放列表管理功能
- [ ] 支持均衡器设置
- [ ] 添加歌曲收藏功能
- [ ] 支持桌面歌词
- [ ] 添加睡眠定时器
- [ ] 支持多种音频格式
- [ ] 优化性能和内存使用

---

## 🤝 贡献

欢迎贡献代码！请遵循以下步骤：

1. Fork 本仓库
2. 创建特性分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 开启 Pull Request

---

## 📝 开源协议

本项目采用 MIT 协议 - 详见 [LICENSE](LICENSE) 文件

---

## 👨‍💻 作者

Created with Claude Code

---

## 🙏 致谢

- [ExoPlayer](https://github.com/google/ExoPlayer) - 强大的媒体播放库
- [Retrofit](https://square.github.io/retrofit/) - 优秀的网络请求库
- [Glide](https://github.com/bumptech/glide) - 高效的图片加载库
- [Material Components](https://github.com/material-components/material-components-android) - Material Design 组件

---

<div align="center">

**如果这个项目对你有帮助，请给个 ⭐ Star 支持一下！**

</div>

---

## 📅 更新日志

### v1.0.0 (2026-01-12)
- ✨ 初始版本发布
- ✨ 支持本地和在线音乐播放
- ✨ 实现歌词同步滚动显示
- ✨ 添加 Material Design 3 UI
- 🐛 修复 LyricsParser 编译错误
- ♻️ 优化代码结构，移除重复代码
- 📦 更新依赖到最新版本
