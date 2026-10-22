# 模板验收清单

改完模板或新项目初始化后逐条勾选。每条都给了**可执行的验证方式**，不要凭印象打勾。

## 安全

- [ ] 仓库里**只有** `app/keystore/debug.jks` 这一个 `.jks`
      `git ls-files | grep -E "\.jks|local\.properties"` → 只输出 `app/keystore/debug.jks`
- [ ] debug 密钥固定为团队证书
      `./gradlew :app:signingReport` → 所有 `*Debug` 的 SHA-256 均为 `74:5E:5D:89:…:E1:2A`（见 [SIGNING.md](SIGNING.md)）
- [ ] **【新项目必查】已换 release 密钥，且占位开关已关**
      `gradle.properties` 中 `template.usePlaceholderReleaseKey=false`，
      且 `./gradlew :app:verifyReleaseSigning` 输出 `release 使用自有密钥` 而非 `模板占位密钥`
- [ ] release 签名口令来自 `local.properties` 或环境变量，构建脚本无硬编码
      `grep -rn "storePassword\s*=" app/build.gradle.kts` → 只应出现变量引用
- [ ] `./gradlew :app:verifyReleaseSigning` 缺签名时失败、配齐时通过
- [ ] `AndroidManifest` 中 `usesCleartextTraffic=false`，仅 debug 变体放开
      `grep -n usesCleartextTraffic app/src/main/AndroidManifest.xml app/src/debug/AndroidManifest.xml` → false / true
- [ ] `allowBackup=false`
- [ ] 接口签名密钥 `SIGN_SECRET` 不写死在代码里（走 `BuildConfig.SIGN_SECRET`）

## 架构一致性

- [ ] 没有 ViewModel 直接依赖 `ApiService`
      **首选**：`./gradlew :app:testDevDebugUnitTest --tests "*ArchTest"`（Konsist 架构测试，含 UI 层禁触网络栈、State 命名、Repository 位置 4 条规则）
      人工复核：`grep -rn ": ApiService\|apiService:" app/src/main/java --include=*ViewModel.kt` → 输出为空
- [ ] 没有 `catch (_: Exception) {}` 这类静默吞错
      `grep -rn "catch (_: Exception)" app core --include=*.kt` → 输出为空
- [ ] 页面状态统一 `UiState`，副作用统一 `UiEffect`
- [ ] Repository 失败抛 `AppException`（由 `safeApi` 统一包装），不裸抛 IO/HTTP 异常
- [ ] 分页页面不硬套 `UiState`（`ArticleListViewModel` 即正面示例）
- [ ] 分页的 `Pager` 组装在 Repository，不在 ViewModel

## 能力可用性

- [ ] 401 能自动刷新并**重写请求体 token**（`TokenAuthenticatorTest` 通过）
- [ ] 刷新失败会清理登录态并跳登录页（`SessionManager.expire()` → `App.kt` 监听）
- [ ] 统一 Header 真实发出（抓包可见 `Device-Type` / `Client-Id`）
- [ ] `StatefulLayout` 至少在两个页面使用（首页、分类页、收藏页、历史页）
- [ ] `BaseListAdapter` 至少在列表页使用（`ArticleListAdapter`：收藏页、历史页）；多类型列表见 `DiscoverAdapter`
- [ ] **分页列表用 `PagingDataAdapter`，定长列表用 `ListAdapter`**，两者不混用
      `grep -rn "PagingDataAdapter" app/src/main/java` → 只有 `ArticleAdapter`
- [ ] 搜索页可用：首页标题栏 → 输入关键词 → 300ms 后出结果，连续改词不串结果
      `grep -n "flatMapLatest\|debounce" app/src/main/java/com/template/app/ui/search/SearchViewModel.kt`
- [ ] 设置页可用：我的 → 设置 → 拨开关 → 杀进程重进，状态被记住（DataStore 生效）
- [ ] 图片选择可用：我的 → 选择图片 → 系统相册选一张 → 页面显示 `content://` 路径
- [ ] 图片选择**没有**申请存储权限（PhotoPicker 不需要）
      `grep -n "READ_MEDIA_IMAGES\|READ_EXTERNAL_STORAGE" app/src/main/AndroidManifest.xml` → 输出为空
- [ ] Room 未使用 `fallbackToDestructiveMigration`
      `grep -rn "fallbackToDestructiveMigration" core` → 输出为空
- [ ] **升 Room 版本时补了迁移 + 迁移测试**（`MigrationTest` 会逐列比对 schema JSON）
      `./gradlew :core:testDebugUnitTest --tests "*MigrationTest"` 通过；
      `ls core/schemas/com.template.core.db.AppDatabase/` 里最新版本的 json 已提交
- [ ] 收藏 / 历史可用：详情页收藏 → 我的 → 我的收藏出现该文章；取消收藏后列表自动少一条
- [ ] 浏览历史上限生效（`HistoryRepository.MAX_ENTRIES`），超限后最旧的被裁掉

## 构建与质量

- [ ] `./gradlew :app:assembleDevDebug` 通过
- [ ] `./gradlew :core:testDebugUnitTest :app:testDevDebugUnitTest` 通过
- [ ] 用例数用脚本核对，不靠记忆：`./scripts/test-count.sh`（应包含 Konsist 架构测试 4 条）
- [ ] `./gradlew :core:koverVerify :app:koverVerify` 通过（非回退底线：core 35% / app 5%）
- [ ] `./gradlew detekt spotlessCheck` 全绿（detekt 为 0 容忍，任何违规都会失败）
- [ ] `./gradlew :app:lintDevDebug` **无 error 且无 warning**（基线为 0 warning，新增告警必须当场处理）
- [ ] `./gradlew :app:connectedDevDebugAndroidTest` 真机冒烟 2/2 通过
- [ ] `./gradlew :app:assembleProdRelease` 成功，产物**无** `-unsigned` 后缀
- [ ] `proguard-rules.pro` 无整包 keep、无写死包名（改名后规则必须仍然生效）
      `grep -n "keep class \*\*\|com.template" app/proguard-rules.pro core/*.pro` → 输出为空
- [ ] 依赖版本全部来自 `libs.versions.toml`，模块内无硬编码版本号
- [ ] 公共 Android 配置只在 `build-logic` 改（模块脚本无 compileSdk/minSdk/compileOptions 重复声明）
- [ ] CI 在 PR 上全绿

## 模板可用性

- [ ] 6 个变体包名与 README 表格一致
      `./gradlew :app:assembleDevDebug :app:assembleProdDebug` 后 `aapt2 dump badging *.apk | grep package`
- [ ] 三个 flavor 的包名互不冲突，可同时安装
- [ ] 断网时首页显示错误态 + 重试可恢复
- [ ] **下拉刷新连续 3 次不崩溃**
      Paging 要求 `pagingSourceFactory` 每次返回新实例，传实例会抛
      `IllegalStateException: An instance of PagingSource was re-used`（本仓库踩过，只在刷新时触发，
      首屏加载正常，容易漏测）。`ArticleRepository.pager()` 的参数是 `() -> ArticlePagingSource`，
      就是为了让"传实例"在类型层面写不出来
- [ ] 二级页（搜索 / 设置）有返回按钮，进入时底部导航隐藏，返回后恢复
- [ ] 搜索页进入即弹键盘，空态文案居中显示
- [ ] 脚手架用当前命名空间生成且可直接编译
      `./scripts/new-feature.sh demo && ./gradlew :app:assembleDevDebug`（验完记得删掉生成的 4 个文件）
- [ ] 文档引用的代码路径全部存在（脚本化，CI 也会跑）
      `./scripts/check-docs.sh` → 输出 `OK: 文档引用的 N 个代码路径全部存在`
- [ ] 源文件无 UTF-8 BOM（BOM 会让 `@file:Suppress` 静默失效，增量构建查不出来）
      `./scripts/check-encoding.sh` → 输出 `OK: 检查 N 个文件，无 BOM`
- [ ] 文档里的能力描述都有对应调用点（"已支持"必须 grep 得到）
      `grep -rn "AutoSize\|blankj" app core --include=*.kt` → 输出为空（已删除的依赖不得再出现在文档能力表里）
- [ ] 无空 catch 块（吞异常且不记日志）
      `grep -rn "catch (_: Exception)" app core --include=*.kt` → 仅允许出现在注释/文档字符串里，代码中为 0 处
- [ ] [NEW_PROJECT.md](NEW_PROJECT.md) 的 8 步在干净环境里能走通
