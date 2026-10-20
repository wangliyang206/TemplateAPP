# TemplateAPP

安卓 MVVM 模板工程：双模块 + Hilt + 单向数据流 + 可运行测试，克隆即可开新项目。

- **技术栈**：Kotlin 2.0 / AGP 8.7 / compileSdk 35 / minSdk 24 / Hilt(KSP) / Retrofit / Room / DataStore / ViewBinding
- **范式**：一个 State（StateFlow）、一个事件通道（SharedFlow）、一套错误模型（AppError）
- **无后端可跑**：debug 默认 `IS_DEBUG_DATA=true`，全部接口走内置 Mock

## 开新项目先读这三份

| 文档 | 什么时候看 |
|---|---|
| **[docs/NEW_PROJECT.md](docs/NEW_PROJECT.md)** | **第一步**。改名清单（8 步）+ 使用说明 + 注意事项，含照抄就踩的坑 |
| **[docs/SIGNING.md](docs/SIGNING.md)** | 第 6 步必读。模板自带的是**占位签名密钥**，新项目必须换掉 |
| **[docs/CHECKLIST.md](docs/CHECKLIST.md)** | 交付前自检，逐条可验证 |

> **最低限度**：不改包名直接用会出现两个应用抢同一 applicationId；不换签名密钥等于用一把
> 公开的私钥上架。这两条都写在 NEW_PROJECT.md 的「注意事项」里。

## 快速开始

```bash
# 1. 打开项目（Android Studio Koala 及以上）或用命令行
./gradlew :app:assembleDevDebug

# 2. 跑测试（不需要模拟器）
./gradlew :core:testDebugUnitTest :app:testDevDebugUnitTest

# 3. 装到设备
./gradlew :app:installDevDebug
```

> 环境变量：JDK 17、Android SDK Platform 35。`local.properties` 里的 `sdk.dir` 需自行配置。

## 模块结构

```
:app    壳工程 —— Application、导航、flavors、DI 装配、页面
:core   能力库 —— 网络 / 数据库 / 存储 / 状态容器 / 基类 / UI 组件
```

依赖方向固定为 `app → core`，禁止反向依赖。通用库由 core 以 `api` 暴露，app 不再重复声明版本。

## 目录约定（app）

```
com.template.app
├── App.kt                  # Application：网络配置 + 全局登录态监听
├── MainActivity.kt         # 单 Activity 壳：BottomNavigation + NavHostFragment
├── di/                     # Hilt 模块（BaseUrl / ApiService / 数据库 / DataStore）
├── data/
│   ├── api/                # ApiService（接口定义）+ MockApiService
│   ├── model/              # 请求/响应模型
│   ├── paging/             # PagingSource
│   ├── session/            # SessionRepository：本地登录态唯一入口
│   └── repository/         # 跨页面复用的数据仓储
├── ui/
│   ├── splash/             # 启动页（SplashScreen API）
│   ├── home/               # 首页（Banner + Tab + Paging 列表）
│   ├── category/           # 分类页（列表 + 四态，BaseListAdapter 范例）
│   ├── login/ register/    # 表单校验 + 登录态落库
│   ├── mine/               # 登录态展示 / 登出
│   ├── webview/            # WebView 容器 + JsBridge
│   └── adapter/            # 列表 Adapter
└── update/                 # APK 升级：检查 / 弹窗 / 下载 / 安装
```

## 架构规则（新页面照抄）

**1. 三层单向数据流**

```
Fragment/Activity  ──Event──▶  ViewModel  ──suspend/Flow──▶  Repository  ──▶  ApiService / Dao / DataStore
        ▲                          │
        └────── UiState ───────────┘
```

- ViewModel **不允许**直接注入 `ApiService`（这是上一版最严重的范式污染）
- Repository 只抛 `AppException`，内部把网络异常和业务错误码统一成 `AppError`
- 页面只渲染 `UiState`：`Idle / Loading / Empty / Success / Error`

**2. 状态与副作用分离**

```kotlin
private val _state = MutableStateFlow<UiState<HomeData>>(UiState.Idle)
val state: StateFlow<UiState<HomeData>> = _state.asStateFlow()

fun refresh() = _state.launchWithState { repository.load() }
```

Toast / 导航属于一次性副作用，走 `effects: SharedFlow<UiEffect>`，
通过 `LifecycleOwner.collectEffects(viewModel)` 收集。**绝不塞进 UiState。**

**3. 新建页面用脚手架**

```bash
./scripts/new-feature.sh profile   # 生成 Fragment/ViewModel/Repository/Layout 四件套
```

## 环境与构建

3 个 flavor × 2 个 buildType = 6 个变体，共用同一份业务代码，只有配置不同。
**变体之间的真实包名不同**（`debug` 额外带 `.debug` 后缀），所以同一 flavor 的 debug 与 release 也能共存：

| 变体 | 实际 applicationId | versionName | 数据源 | 用途 |
|---|---|---|---|---|
| `devDebug` | `com.template.app.dev.debug` | `1.0.0-dev` | Mock，允许明文 HTTP | 日常开发 |
| `devRelease` | `com.template.app.dev` | `1.0.0-dev` | Mock | 一般不分发 |
| `stagingDebug` | `com.template.app.staging.debug` | `1.0.0-staging` | Mock | 测试排查 |
| `stagingRelease` | `com.template.app.staging` | `1.0.0-staging` | Mock | 测试分发包 |
| `prodDebug` | `com.template.app.debug` | `1.0.0` | 真实接口 | 线上问题排查 |
| `prodRelease` | `com.template.app` | `1.0.0` | 真实接口 | 应用商店正式包 |

> flavor 决定「连哪个后端、包名前缀、应用名」，buildType 决定「是否混淆、用哪把密钥、是否带 `.debug` 后缀」。
> 包名以 `aapt2 dump badging` 实测为准，本文表格即实测结果。

```bash
./gradlew :app:assembleDevDebug       # 开发
./gradlew :app:assembleStagingRelease # 测试包（需签名）
./gradlew :app:assembleProdRelease    # 正式包（需签名）
./gradlew verifyReleaseSigning        # CI 卡口：签名缺失直接失败
./gradlew :app:signingReport          # 查看当前变体用了哪份证书、指纹是多少
```

**签名采用双密钥模型**，两个密钥的处理方式相反：

| 密钥 | 文件 | 是否入库 | 为什么 |
|---|---|---|---|
| debug 团队密钥 | `app/keystore/debug.jks` | ✅ **必须入库** | 不固定就会每台机器各签各的 → 互相覆盖安装报 `INSTALL_FAILED_UPDATE_INCOMPATIBLE`，且微信/地图/推送等按「包名+签名」校验的 SDK 全部失效 |
| release 发布密钥 | 由 `local.properties` / CI Secrets 指向 | ⚠ 模板阶段用 `app/keystore/release.jks` 占位，正式项目**永不入库** | 泄露等于应用被冒名顶替 |

debug 默认口令 `android` / `androiddebugkey`，是标准 AOSP Debug 证书，不含业务机密，允许随仓库分发。

> **⚠ 模板占位密钥**：当前 release 用的是模板自带的 `release.jks`（`CN=beanSproutAssistant`），
> 它曾公开在 git 历史里，只能用于内部演示。`gradle.properties` 的
> `template.usePlaceholderReleaseKey=true` 允许它工作并告警；**新项目必须换成自己的密钥并把开关改成 `false`**，
> 否则 `verifyReleaseSigning` 会直接失败。见 [docs/SIGNING.md](docs/SIGNING.md) §七。

完整说明、跨机器一致性校验方法、排错表见 **[docs/SIGNING.md](docs/SIGNING.md)**。

## 测试

| 层 | 位置 | 说明 |
|---|---|---|
| core 错误映射 / Repository | `core/src/test` | AppError 归一化、safeApi 行为 |
| Token 刷新 | `core/src/test/.../TokenAuthenticatorTest` | 请求体 token 重写、single-flight、失败清理 |
| ViewModel | `app/src/test` | 用 Fake ApiService 驱动，不依赖 Robolectric |

新写 ViewModel 时，直接抄 `HomeViewModelTest` 的骨架 + `MainDispatcherRule`。

## 已内置能力

网络（统一请求体 / 签名 / 统一 Header / 401 自动刷新 / Mock 切换）、
存储（Room 迁移框架 + DataStore + MMKV）、
UI（四态容器 `StatefulLayout`、`BaseListAdapter`、Banner、下拉刷新、Paging3）、
基础设施（崩溃捕获、权限、WebView、APK 升级、Glide 扩展）。

## 开新项目

不要直接 `git clone` 后就写业务，先按 **[docs/NEW_PROJECT.md](docs/NEW_PROJECT.md)** 走一遍改名清单（8 步，约 10 分钟）。
交付前用 **[docs/CHECKLIST.md](docs/CHECKLIST.md)** 自检。

## 决策记录

关键取舍写在 `docs/ADR/`，包括为什么不要 domain 层、为什么保留 Gson、为什么还留着 Jetifier。
