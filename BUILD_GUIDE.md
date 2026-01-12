# 如何打包APK（安卓安装包）

## 📱 什么是APK？

APK是Android应用程序的安装包文件，类似于Windows上的.exe文件。你只需要把APK文件传输到安卓手机上，点击安装就能使用了。

## 🚀 最简单的方法：使用GitHub自动构建（推荐）

### 步骤：

1. **注册GitHub账号**（如果没有的话）
   - 访问 https://github.com
   - 点击"Sign up"注册

2. **创建新的代码仓库**
   - 登录后点击右上角的"+" → "New repository"
   - 仓库名称填写：`MusicPlayer`（或其他名称）
   - 选择"Public"（公开）
   - 勾选"Add a README file"
   - 点击"Create repository"

3. **上传项目文件**
   - 在新建的仓库页面，点击"uploading an existing file"
   - 将项目文件夹中的**所有文件和文件夹**拖拽上传
   - 等待上传完成
   - 滚动到页面底部，点击绿色按钮"Commit changes"

4. **启动自动构建**
   - 在仓库页面点击"Actions"标签
   - 选择左侧的"Build APK"工作流
   - 点击右侧的"Run workflow"按钮
   - 点击绿色的"Run workflow"按钮开始构建

5. **下载APK**
   - 等待几分钟（通常2-5分钟）
   - 构建完成后，在Actions页面点击完成的构建任务
   - 在页面底部的"Artifacts"区域找到"musicplayer-debug"
   - 点击下载，得到一个`.apk`文件

6. **安装到手机**
   - 将下载的APK文件传到你的安卓手机
   - 在手机上点击APK文件
   - 允许安装来自未知来源的应用
   - 点击"安装"即可

## 🔧 其他方法

### 方法2：使用Android Studio（标准方式）

1. **下载安装Android Studio**
   - 访问：https://developer.android.com/studio
   - 下载并安装（约1GB）

2. **安装JDK**
   - 下载JDK 17或更高版本
   - 推荐：https://adoptium.net/

3. **打开项目**
   - 启动Android Studio
   - 选择"Open"
   - 选择项目文件夹

4. **构建APK**
   - 点击菜单：Build → Build Bundle(s) / APK(s) → Build APK(s)
   - 等待构建完成
   - 点击通知中的"locate"查看生成的APK
   - APK位置：`app/build/outputs/apk/debug/app-debug.apk`

### 方法3：使用在线IDE

使用以下在线平台（无需安装）：

- **GitHub Codespaces** + GitHub Actions（推荐）
- **Gitpod**: https://gitpod.io
- **Replit**: https://replit.com

这些平台可以直接在浏览器中构建项目。

## 📲 安装APK到手机

### 方法1：通过USB传输
```
手机 → 电脑复制APK → 手机文件管理器 → 点击APK安装
```

### 方法2：通过云端
```
上传APK到网盘（百度网盘/Google Drive等） → 手机下载 → 安装
```

### 方法3：通过微信/QQ
```
电脑发送APK文件 → 手机接收 → 点击安装
```

## ⚠️ 注意事项

1. **权限设置**：首次运行需要授予存储权限和通知权限
2. **API配置**：在线音乐功能需要配置有效的音乐API地址
   - 位置：`app/src/main/java/com/musicplayer/api/MusicApiService.kt`
   - 修改`BASE_URL`变量
3. **网络**：确保手机有网络连接（在线音乐功能需要）
4. **音乐文件**：本地音乐功能需要手机上有音频文件

## 🎵 快速测试

如果没有配置API，可以先测试本地音乐功能：
1. 确保手机上有MP3等音频文件
2. 打开应用，授予存储权限
3. 应用会自动扫描并显示所有音乐
4. 点击播放即可

## 📞 需要帮助？

如果遇到问题，可以检查：
1. GitHub Actions是否成功运行（绿色勾）
2. APK文件是否完整下载
3. 手机是否允许安装未知来源应用
4. Android版本是否满足要求（Android 7.0+）

祝使用愉快！🎶
