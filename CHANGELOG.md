# Changelog

本模板的所有显著变更记录在此。
格式遵循 [Keep a Changelog](https://keepachangelog.com/zh-CN/1.1.0/)。

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
