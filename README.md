# WeatherForecastApp

一个面向通勤场景的极简天气应用。

项目主打两件事：
- 打开就能快速看到天气，不被复杂信息打断
- 常用城市切换顺手，适合上下班、出门前、跨城通勤时快速确认天气

## 项目定位

WeatherForecastApp 不是一个追求信息堆砌的“大全型天气 App”，而是一个强调 **简洁、方便、启动快** 的轻量天气客户端。

适用场景：
- 早上出门前快速确认当前位置天气
- 通勤路上切换查看常驻城市和工作城市
- 网络不稳定时优先展示最近一次缓存结果，减少白屏等待

## 核心功能

- 当前定位天气
- 城市搜索与天气预览
- 收藏城市管理
- 默认城市置顶
- 首页左右滑动切换“当前位置 / 收藏城市”
- 离线缓存回退
- 启动快照恢复，做到“先秒开缓存，再后台刷新”

## 工程亮点

### 1. 离线优先 + 启动秒开

项目实现了一个轻量的 `stale-while-revalidate` 策略：
- 启动时优先读取 Room 中最近一次天气缓存
- 同时根据 SharedPreferences 中保存的启动快照，恢复“上次正在看的城市卡片”
- UI 先展示离线缓存，避免启动白屏
- 后台继续请求定位与网络天气，成功后无感刷新页面

这条链路对应的核心价值是：
- 弱网下仍然有可用内容
- 首屏响应更快
- 通勤场景里更接近真实可用的产品体验

### 2. 多城市管理体验闭环

项目支持：
- 搜索城市并预览天气
- 一键添加到收藏
- 删除收藏城市
- 设置默认城市
- 首页滑动卡片快速切换当前城市

这样首页不再只是“单城市详情页”，而是具备了日常使用价值的轻量多城市天气首页。

### 3. 现代 Android 架构实践

- Jetpack Compose + Material 3
- MVVM + StateFlow
- Room 本地缓存
- Retrofit + Kotlinx Serialization 网络层
- AppContainer 手动依赖注入
- ViewModel 统一管理首页、定位、收藏城市、搜索预览等状态

## 技术栈

- Kotlin
- Jetpack Compose
- Material 3
- Coroutines + Flow
- Retrofit
- OkHttp
- Kotlinx Serialization
- Room
- KSP
- FusedLocationProviderClient + LocationManager fallback

## 架构说明

### 依赖创建链

`WeatherApplication -> DefaultAppContainer -> WeatherRepository`

### 运行时数据流

`WeatherScreen -> WeatherViewModel -> WeatherRepository -> Retrofit / Room -> WeatherUiState -> Compose UI`

## 关键模块

- `app/src/main/java/com/example/weatherforecastapp/ui/screen/WeatherScreen.kt`
  - 页面路由、权限处理、定位请求入口
- `app/src/main/java/com/example/weatherforecastapp/ui/screen/WeatherViewModel.kt`
  - 首页卡片、收藏状态、搜索状态、启动快照恢复
- `app/src/main/java/com/example/weatherforecastapp/data/repository/WeatherRepository.kt`
  - 网络请求、本地缓存、默认城市、离线回退
- `app/src/main/java/com/example/weatherforecastapp/data/local/WeatherDao.kt`
  - 天气缓存与收藏城市本地数据读写
- `app/src/main/java/com/example/weatherforecastapp/data/store/StartupSnapshotStore.kt`
  - 启动快照恢复，支撑“秒开缓存”能力

## 当前完成度

已完成：
- 天气 API 接入
- 本地缓存
- 定位权限与系统定位兜底
- 城市搜索
- 收藏城市
- 默认城市
- 首页滑动城市卡片
- 离线优先 + 启动秒开
- 极简风格 UI 改造

下一步可继续补强：
- 单元测试 / ViewModel 测试
- 下拉刷新动效
- 天气预警 / 生活指数模块
- 桌面小组件
- 手动排序收藏城市

## 本地运行

1. 在 `local.properties` 中配置 API Key

```properties
API_KEY=你的和风天气Key
```

2. 使用 Android Studio 打开项目并同步 Gradle
3. 连接真机或模拟器运行 `app`

## 项目价值

如果把这个项目放在简历里，它更适合被描述为：

> 一个面向通勤场景的极简天气应用，重点优化了多城市切换体验与弱网场景下的启动可用性，实现了离线优先、启动秒开、定位回退与现代 Android 架构分层。

## 仓库附加文档

- `README_AI`：给 AI / 自动化工具读取的项目上下文摘要
- `docs/project-intro.md`：面向面试讲述的项目介绍稿
- `docs/project-upgrade-log.md`：项目阶段性升级记录
