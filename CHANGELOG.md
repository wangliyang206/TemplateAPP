# Changelog

本模板的所有显著变更记录在此。
格式遵循 [Keep a Changelog](https://keepachangelog.com/zh-CN/1.1.0/)。

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
