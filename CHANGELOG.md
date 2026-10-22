# Changelog

本模板的所有显著变更记录在此。
格式遵循 [Keep a Changelog](https://keepachangelog.com/zh-CN/1.1.0/)。

## [2.2.1] - 2026-10-22

> 主题：**修掉两个会静默骗过门禁的问题**——BOM 让 `@file:Suppress` 失效、Mock 图片绑死外网图床。

### Fixed 修复

- **BOM 导致 `@file:Suppress` 静默失效（16 个文件）**
  `MockApiService.kt` 首行带 UTF-8 BOM，文件头写的 `@file:Suppress("MagicNumber")` 完全没生效，
  干净构建下 detekt 报 8 条 MagicNumber；去掉 BOM 后同一份代码立刻通过。
  这类问题在增量构建里看不出来（`:app:detekt` 一直 UP-TO-DATE），只有 CI 干净构建才炸——
  而此前 CI 的 push 分支写错了（见 2.2.0），所以一直没有暴露。
  已清理全部 16 个带 BOM 的源文件（15 个 `.kt` + `core/src/main/AndroidManifest.xml`）。

- **Mock 图片不再依赖外网图床**
  原先 Banner / 头像 / 封面全部指向 `picsum.photos`，国内网络实测 `SocketTimeoutException`（2.5s 超时），
  真机首屏 6 张轮播图**全部加载失败且无兜底**，界面一片空白。
  现改为打包进 APK 的本地图（`app/src/main/res/drawable-nodpi/`）：
  `banner_1` ~ `banner_6`、`avatar_default`、`cover_placeholder`，模板不联网也有完整 UI。
  Mock 里写 `"res://banner_1"` 即可，接真实后端后换成 https 地址，UI 层零改动。
  摄影分类的 5 个示例链接也从 picsum 换成国内可访问的摄影社区。

- **Glide 扩展补齐加载失败兜底**
  `placeholder` 之前只在加载中显示，失败后仍是空白。现在同时作为 `error` 图，
  网络异常时保留占位图而不是白块。

### Added 新增

- **`res://` 本地资源协议**（`core/ext/GlideExt.kt`）：`load/loadCircle/loadRounded/loadCircleOrDrawable`
  四个方法统一支持，`res://名称` 解析成 drawable 资源 id，带 `ConcurrentHashMap` 缓存避免反复反射。
  名称找不到时返回 `null` 走 error 图，不崩溃。
- **`scripts/check-encoding.sh`**：BOM 检查，已接入 CI。
- **StrictMode 系统噪音过滤**（`core/BaseApplication.kt`）：API 28+ 用 `penaltyListener` 过滤
  `com.miui.server.` / `com.android.server.` 栈帧的违规。这类日志是系统进程读盘（如 MIUI 应用锁
  检查密码文件）被归因到调用方，与业务代码无关，不过滤会把真实违规淹掉。

## [2.2.0] - 2026-10-22

> 主题：**按《模板审计报告》清 P0/P1/P2**——先让文档与代码对齐，再补核心链路测试，最后补示例与工具。

### Added 新增
- **搜索页**（`ui/search/`）：`debounce` + `distinctUntilChanged` + `flatMapLatest` 三件套，
  换词即取消上次请求；复用 `ArticlePagingSource`（新增 `keyword` 参数）与 `ArticleAdapter`
- **设置页**（`ui/settings/` + `data/settings/SettingsStore`）：DataStore 业务键示例，
  与会话存储**分库**（登出不清偏好），回填状态用 `isRendering` 标志位防自激
- **图片选择**（`core/ext/PhotoPickerExt.kt`）：PhotoPicker 封装，**不需要存储权限**；
  「我的」页给出选图示例
- **工具扩展**：`NumberExt`（金额/千分位/文件大小/百分比）、`DateExt` + `DatePattern`（格式化/相对时间/是否今天）、
  `ViewExt`（防连点/`isVisible`/dp2px）、`NetExt`（网络可用/计费网络）
- **测试 +23**：`SignUtilsTest`(5)、`RequestPipelineTest`(4, MockWebServer 真实发请求验头与 token)、
  `SessionManagerTest`(2)、`AppDatabaseTest`(4, Robolectric + schema 与版本一致性守卫)、
  `NumberExtTest`(6)、`DateExtTest`(6)
- **`local.properties.example`**：配置模板（不含任何口令），首次构建不再靠口口相传
- **`scripts/check-docs.sh`**：文档引用的代码路径存在性检查，已接入 CI
- **`scripts/test-count.sh`**：用例数统一由脚本统计，文档不再写死数字

### Changed 变更
- **CI push 分支对齐实际**：`main/develop` → `master/devlop`（此前 push 永远不会触发 CI，只有 PR 会跑）
- **`CONTRIBUTING.md` 分支模型对齐**：明确"本地 devlop，推送 `devlop:master`"，删掉不存在的 develop 流程
- `init-project.sh`：新增「同步文档里的工程名/包名」「删除模板评测报告」「把占位密钥开关置 false」
  「`git init` + 首提交」；`local.properties` 改为**只继承 sdk.dir**；复制时排除 `.workbuddy`
- **删除零调用依赖**：`AndroidAutoSize`、`utilcodex`（声明了但全项目没有调用点）
- **覆盖率门禁上调**：core 15% → 35%，app 1% → 5%（实测 core 39.4% / app 5.4%；目标 40% / 15%）
- `dev` flavor 的 `API_HOST` 从真实第三方域名改为 `http://api-dev.example.com/`
- `GsonBuilder.setLenient()`（Gson 2.11 已废弃）改为 `setStrictness(LENIENT)`，消掉编译告警

### Fixed 修复
- **文档与代码不符 14 处**（审计报告 §1）：
  - 删除文档里已不存在的 `PermissionHelper.requestPhone`、MMKV、`mmkv/MMKVManager.kt`、
    `ui/splash/`、View/LiveData 扩展、`core/schemas/1.json` 错误路径
  - 术语更正：LICENSE 已存在（MIT）、用例数改为脚本统计、`verifyReleaseSigning` 写作 `:app:verifyReleaseSigning`

## [2.1.1] - 2026-10-20

### Fixed 修复
- **切 Tab 回首页无数据**：`HomeFragment.pagerReady` 是实例字段，底部导航切走销毁 View 后未重置，
  切回时 ViewPager 未重新绑定 adapter。已在 `onDestroyView` 重置
- **WebView 外链 404/断网无兜底**：WebView 包进 StatefulLayout，主 frame 网络错误或
  HTTP >= 400 时展示错误态 + 重试按钮（外链内容失效无法在客户端根除，容器兜底是正解）
- **上传示例必然失败**：dev 构建（IS_DEBUG_DATA）下 FileUploadManager 走原生 OkHttp 直打真实后端，
  演示端点不存在。新增 `MockUploadInterceptor`（NetworkConfig.mockUploadEnabled 开关，默认关），
  仅短路演示端点返回伪 200；附 3 条 JVM 测试

## [2.1.0] - 2026-10-20

### Added 新增
- **文件上传 app 侧完整示例**：`UploadRepository`（cacheDir 生成演示文件 → 带进度 PUT），
  「我的」页新增上传入口，进度/成功/失败三态展示，补齐 `FileUploadManager` 从封装到调用的闭环
- 单元测试 +7：`LoginViewModelTest`（4，含登录→写会话全链路与失败重试）、
  `ArticleListViewModelTest`（3，paging-testing `asSnapshot` 验证首屏/翻页/分类切换）

### Changed 变更
- `DataStoreManager` 抽象出 `SessionStore` 接口（与 `TokenStore` 同套路），
  `SessionRepository` 面向接口依赖，登录链路可在纯 JVM 下用内存假实现测试

### Fixed 修复
- READ_PHONE_STATE 敏感权限移除（权限清单 + 死代码 `PermissionHelper.requestPhone` 采集链路）
- `StatefulLayout` 空态/加载/失败默认文案改走资源，修复 i18n 漏网
- `TokenAuthenticator` 增加刷新请求重入保护（`X-Skip-Auth-Refresh`），杜绝刷新接口 401 递归
- lint `warningsAsErrors=true`；kover `verify` 覆盖率非回退底线入 CI
- 文档漂移修正（静态检查/测试描述与代码对齐）

## [2.0.0] - 2026-10-20

一次全面的模板化整改：架构、质量门禁、性能、文档同步升级。

### Added 新增
- **build-logic convention plugins**（`template.android.application` / `template.android.library`）：
  compileSdk/minSdk/targetSdk/jvmTarget/lint/packaging 收敛为单一真相源，模块脚本只留特有配置
- **Konsist 架构守护测试**（`ArchTest`，4 条规则，通配包名防改名失效）：
  VM 禁直连 ApiService、UI 禁触网络栈、State 命名统一、Repository 位置约束
- **一键初始化脚本** `scripts/init-project.sh`：复制工程 → 改包名 → 移目录 → 改名 → 构建验证
- 静态检查门禁：detekt（`config/detekt/detekt.yml`，maxIssues: 0）+ spotless(ktlint) + kover 覆盖率
- Renovate 依赖自动更新策略（AGP/Kotlin/KSP/Hilt 大版本人工评估）
- Glide KSP 注入（`TemplateGlideModule`，内存/磁盘缓存统一配置）
- SplashScreen API 启动屏（`core-splashscreen`，删除人为 1500ms 延迟的 SplashActivity）
- debug 构建 StrictMode（主线程 I/O、Activity/Closeable 泄漏检测，release 零开销）
- 暗色模式：`values-night` 语义色板（surface/text_* /bg_gray），状态栏图标随主题切换
- 英文资源：`values-en`（app + core 全量文案），布局硬编码中文清零
- 治理文件：`LICENSE` / `CONTRIBUTING.md` / `CHANGELOG.md` / `.gitattributes` / `.github/CODEOWNERS`
- CI 接入 detekt + spotlessCheck 门禁
- 单元测试：Robolectric `StatefulLayoutTest`（真实布局加载，覆盖 `<merge/>` 崩溃场景）
- UI 冒烟测试：Hilt 化 `MainActivitySmokeTest`（真机通过）
- ADR-0006：登录态双入口（core `SessionManager` 与 app `SessionRepository`）的决策与不变量

### Fixed 修复
- **`StatefulLayout` 启动崩溃**：`<merge/>` 布局二次 inflate 导致 InflateException，
  改为代码构造 `StateCardView` 并校验子 View 数量
- 登录/注册页初始状态误置 `Loading`，进入页面按钮被禁用 → 改 `Idle`
- `FileUploadManager` 私建 OkHttp 绕过 TokenInterceptor，上传接口 401 → 复用全局 client
- `AppDownloadManager` 损坏的 import 与裸字节数组、过宽 catch
- 全局异常处理器写崩溃日志失败被静默吞掉 → 记录日志
- `DeviceInfoCollector` 4 处空 catch → 统一 `safeRead` + 日志
- WebView 返回键废弃 API → `OnBackPressedDispatcher`

### Changed 变更
- ProGuard 规则全部通配化（`**.data.model.**` 等），开新项目改名后规则不失效；
  删除 core 整包 keep，`consumer-rules.pro` 收敛到最小集合
- 删除死代码：MMKV/`MMKVManager`/`LiveDataExt`/`ViewExt`/`MineRepository`/`SplashActivity`
  及 5 个死资源
- 37 条 lint 告警清零（Overdraw、Autofill、UnusedResources、IconDuplicates、
  IconLauncherShape、SetTextI18n、DataExtractionRules…）
- `ic_launcher_round.png` 重制为真正圆形（5 档密度）
- 文档：`README.md` / `docs/SIGNING.md` / `docs/NEW_PROJECT.md` / `docs/CHECKLIST.md`
  与代码全量对齐

### Security 安全
- 签名双钥模型固化：debug key 入库保证跨机器一致，release key 仅 CI 注入
  （详见 `docs/SIGNING.md`）
