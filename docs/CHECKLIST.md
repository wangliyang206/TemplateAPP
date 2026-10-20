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
      `grep -rn ": ApiService\|apiService:" app/src/main/java --include=*ViewModel.kt` → 输出为空
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
- [ ] `StatefulLayout` 至少在两个页面使用（首页、分类页）
- [ ] `BaseListAdapter` 至少在列表页使用（`CategoryAdapter`）
- [ ] Room 未使用 `fallbackToDestructiveMigration`
      `grep -rn "fallbackToDestructiveMigration" core` → 输出为空

## 构建与质量

- [ ] `./gradlew :app:assembleDevDebug` 通过
- [ ] `./gradlew :core:testDebugUnitTest :app:testDevDebugUnitTest` 通过，用例数 ≥ 15（当前 19）
- [ ] `./gradlew :app:lintDevDebug` 无 error
- [ ] `./gradlew :app:assembleProdRelease` 成功，产物**无** `-unsigned` 后缀
- [ ] `proguard-rules.pro` 无整包 keep
      `grep -n "keep class \*\*" app/proguard-rules.pro` → 输出为空
- [ ] 依赖版本全部来自 `libs.versions.toml`，模块内无硬编码版本号
- [ ] CI 在 PR 上全绿

## 模板可用性

- [ ] 6 个变体包名与 README 表格一致
      `./gradlew :app:assembleDevDebug :app:assembleProdDebug` 后 `aapt2 dump badging *.apk | grep package`
- [ ] 三个 flavor 的包名互不冲突，可同时安装
- [ ] 断网时首页显示错误态 + 重试可恢复
- [ ] 脚手架用当前命名空间生成且可直接编译
      `./scripts/new-feature.sh demo && ./gradlew :app:assembleDevDebug`（验完记得删掉生成的 4 个文件）
- [ ] 文档中引用的文件/任务都真实存在（无 `RetrofitClient`、`ApiErrorHandler`、`local.properties.example` 之类残留）
      `CHECKLIST.md` 自身（举例必然带这些词）与 `docs/ADR/`（历史记录，允许出现已删文件名）需排除：
      `grep -rn "RetrofitClient\|ApiErrorHandler\|local.properties.example\|PROJECT_STRUCTURE" *.md docs README.md | grep -vE "docs/(CHECKLIST\.md|ADR/)"` → 输出为空
- [ ] 无空 catch 块（吞异常且不记日志）
      `grep -rn "catch (_: Exception)" app core --include=*.kt` → 仅允许出现在注释/文档字符串里，代码中为 0 处
- [ ] [NEW_PROJECT.md](NEW_PROJECT.md) 的 8 步在干净环境里能走通
