# SeaLens App

SeaLens 的跨平台客户端骨架：Kotlin Multiplatform + Compose Multiplatform，目标是
Android 与 Windows Desktop 两个平台。

> 当前为内存版作品管理：可管理作品、板块与章节并编辑正文；数据只在内存中，
> 重启后清空。尚未接入数据库、云盘、DOCX 或 AI。

## 当前功能（内存版）

- 首页：顶部栏（SeaLens + 搜索/设置占位入口）、“我的作品”卡片列表、“＋ 新建作品”卡片
- 作品：新建、修改名称/简介、进入工作区、删除前确认（仅从内存列表移除）
- 工作区：左侧“正文 / 番外 / 设定集 / 正文修改记录 / 杂谈 / 废稿”板块导航
- 板块章节：新建章节、重命名、删除、点击进入章节页
- 章节页：多行文本编辑、实时字数、保存（更新内存中的 content 与 modifiedTime）
- 数据关系：作品 → 板块 → 章节 → 正文内容，作品之间完全独立
- 视觉：莫兰迪蓝灰（主色约 `#657896`）、浅灰白背景、低密度布局，无渐变

## 目录结构

| 路径 | 作用 |
| --- | --- |
| `shared/` | 共享 UI 与业务代码（KMP 模块）。以后小说存档/编辑核心逻辑应优先放在这里 |
| `shared/src/commonMain/` | 跨平台共享代码：`App.kt`（页面切换）、`theme/`、`ui/`、`model/`、`data/` |
| `shared/src/androidMain/` | Android 平台实现（如 `platformName()`） |
| `shared/src/jvmMain/` | Windows Desktop（JVM）平台实现 |
| `androidApp/` | Android 应用壳：`MainActivity`、AndroidManifest、资源 |
| `desktopApp/` | Windows Desktop 应用壳：`main()` 入口与打包配置 |
| `gradle/libs.versions.toml` | 统一版本目录（Gradle Version Catalog） |

## 使用的版本

- Gradle Wrapper：9.7.1
- Kotlin：2.4.10
- Compose Multiplatform：1.12.0
- Android Gradle Plugin：9.4.0
- Android compileSdk / targetSdk：37

## 环境要求

- JDK 17 或更高（通过环境变量 `JAVA_HOME` 配置）
- Android SDK（通过 `local.properties` 中的 `sdk.dir` 指向本机 SDK）
- 不需要 Visual Studio 2022
- 首次构建需要联网下载依赖

## 在 Windows 上运行

```powershell
cd D:\SeaLens\app
.\gradlew.bat :desktopApp:run
```

生成可分发的 Windows 应用目录：

```powershell
.\gradlew.bat :desktopApp:createDistributable
```

产物在 `desktopApp/build/compose/binaries/main/` 下。

## 在 Android 上运行

先连接手机（开启 USB 调试）或启动 Android 模拟器，然后：

```powershell
cd D:\SeaLens\app
.\gradlew.bat :androidApp:installDebug
```

只构建 APK 而不安装：

```powershell
.\gradlew.bat :androidApp:assembleDebug
```

APK 位于 `androidApp/build/outputs/apk/debug/androidApp-debug.apk`，也可以直接用
`adb install` 安装。

## Android Studio 是否必需？

不是必需。命令行 Gradle 即可完成编译、打包和运行。

Android Studio 只是可选的开发工具：直接打开 `D:\SeaLens\app` 文件夹即可编辑和调试；
它会自动识别 Gradle/Kotlin Multiplatform 配置。

## UI 设计约束

主要开发与使用环境：Windows，屏幕 2560 × 1600，系统显示缩放 175%（高 DPI）。

后续 Windows Desktop UI 的设计与调整统一遵守：

- 以 175% 高 DPI 环境下的实际视觉舒适度为主要参考，不要简单把所有 dp 值乘以 1.75
- 导航、章节列表、按钮、文字、编辑区域保持舒适易读，避免组件过小
- 桌面端优先利用较大的横向与纵向空间
- Android 端保持独立的响应式布局，不直接照搬桌面端固定尺寸
- 调整 UI 时优先考虑实际可读性、点击舒适度与空间利用率

## 备注

- `local.properties` 保存本机 Android SDK 路径，属于个人环境配置，不应提交到版本库。
- `.gradle-user-home/` 是本机构建时使用的 Gradle 缓存目录，已加入 `.gitignore`。
