# 用本模板开新项目

本文是**唯一入口**：先看「使用说明」按顺序做完 8 步，再看「注意事项」避免踩坑。

配套文档：[SIGNING.md](SIGNING.md)（签名细节）、[CHECKLIST.md](CHECKLIST.md)（交付前自检）。

---

## 快捷方式：一键初始化（推荐）

下面 8 步里的第 1、2、3 步已经脚本化，**在 Git Bash 中一条命令完成**：

```bash
./scripts/init-project.sh com.yourcompany.myapp MyApp
# 参数 1：新包名（至少两段小写）；参数 2：新工程名
```

脚本做的事：复制工程（排除 `.git` / `build` / `.idea` / `.workbuddy` / 本机配置）→ 替换包名（含测试代码）→
移动源码目录 → 替换工程名/应用名 → **同步替换文档里的工程名与包名** → 删除模板的评测报告 →
**把 `template.usePlaceholderReleaseKey` 置为 false** → 自动跑 `assembleDevDebug` 验证 → `git init` 并首次提交。

它对 `local.properties` 的处理是**只继承 `sdk.dir`**：`RELEASE_*` / `SIGN_SECRET` 一律不带走，
避免新项目"看起来配好了签名"，实际用的还是模板那把公开过的占位密钥。

**脚本做完后仍需手工的**（也可见下文第 4-8 步）：换应用图标、重新生成 release 签名、
改 base_url / H5_HOST。想了解每一步在干什么、验证命令是什么，继续读下文。

---

## 一、使用说明（8 步，约 10 分钟）

每一步都给了验证命令，**验证通过再进下一步**，出问题容易定位。

### 1. 复制工程

```bash
cp -r TemplateAPP MyApp && cd MyApp
rm -rf .git && git init      # 断开模板的历史，重新开始
```

验证：`ls build.gradle.kts settings.gradle.kts` 都有。

### 2. 改工程名与包名

先改工程名（`settings.gradle.kts`）：

```kotlin
rootProject.name = "MyApp"
```

再改包名。模板里有两个包，**必须分别替换，不要一把梭**：

| 原包 | 改成 | 涉及位置 |
|---|---|---|
| `com.template.app` | `com.yourcompany.myapp` | `app/build.gradle.kts` 的 `namespace` + `applicationId`、`app/src/main/java/` 目录、所有 `package` / `import`、`AndroidManifest` 里的相对类名（`.App` 不用改） |
| `com.template.core` | `com.yourcompany.myapp.core` | `core/build.gradle.kts` 的 `namespace`、`core/src/main/java/` 目录、所有 `package` / `import` |

```bash
# 先替换长的，再替换短的，避免 com.template.app 被拆坏
grep -rl "com\.template\.core" --include=*.kt --include=*.kts --include=*.xml . \
  | xargs sed -i 's/com\.template\.core/com.yourcompany.myapp.core/g'
grep -rl "com\.template\.app" --include=*.kt --include=*.kts --include=*.xml . \
  | xargs sed -i 's/com\.template\.app/com.yourcompany.myapp/g'
```

然后移动源码目录（`sed` 只改内容，不改路径）：

```bash
mkdir -p app/src/main/java/com/yourcompany
mv app/src/main/java/com/template/app app/src/main/java/com/yourcompany/myapp

# core 多一级：目标是 .../yourcompany/myapp/core，父目录必须先建出来
mkdir -p core/src/main/java/com/yourcompany/myapp
mv core/src/main/java/com/template/core core/src/main/java/com/yourcompany/myapp/core

rm -rf app/src/main/java/com/template core/src/main/java/com/template
```

> 这里用 `mv` 而不是 `git mv`：第 1 步刚 `git init`，文件还没被跟踪，`git mv` 会报
> `fatal: not under version control`。如果你已经先 commit 过一次，换成 `git mv` 更好（能保留历史）。
>
> `core` 那行容易漏：`mv` 是把源目录**放进**目标父目录，父目录 `.../yourcompany/myapp` 不存在就会报
> `No such file or directory`。所以先 `mkdir -p` 到 `myapp` 这一层。

> 为什么 core 要带 `.core` 后缀：两个模块各有自己的 `R` 类与 `BuildConfig`，
> 包名相同会在某些工具链下互相覆盖。保持独立是成本最低的做法。

验证：`./gradlew :app:assembleDevDebug`（应成功；失败多半是目录没跟着改）。

### 3. 应用名

- `app/build.gradle.kts` 里三个 flavor 的 `resValue("string", "app_name", ...)`
- `app/src/main/res/values/strings.xml` 中的 `app_name`（作为兜底默认值）

### 4. 应用图标

替换 `app/src/main/res/mipmap-*`（5 档 PNG）与 `mipmap-anydpi-v26/ic_launcher.xml` 引用的
`drawable/ic_launcher_background.xml` / `ic_launcher_foreground.xml`。
推荐用 Android Studio：右键 `res` → New → Image Asset。

> 不要用 1×1 的占位 PNG：release 构建的资源压缩阶段会让 AAPT2 崩溃（模板已踩过）。

### 5. 接口地址

`app/build.gradle.kts` 里每个 flavor 的 `buildConfigField`：

```kotlin
buildConfigField("String", "API_HOST", "\"https://api.yourdomain.com/\"")
buildConfigField("String", "H5_HOST", "\"https://h5.yourdomain.com/\"")
buildConfigField("boolean", "IS_DEBUG_DATA", "false")   // 接真实后端后关掉 Mock
```

`API_HOST` 会被 `di/NetworkModule.kt` 注入给 Retrofit 的 `BASE_URL`，不要在别处硬编码域名。
`H5_HOST` 由 `WebViewActivity.start(context, "/活动路径", "标题")` 消费：传 `/` 开头的相对路径时
自动拼上当前 flavor 的 H5 域名，传完整 `http(s)://` 时按原样加载。

### 6. 签名配置

**模板自带的是"占位密钥"，必须换掉。** 完整原理见 [SIGNING.md](SIGNING.md)，这里是要执行的动作。

1. 关掉占位开关（`gradle.properties`）：

```properties
template.usePlaceholderReleaseKey=false
```

2. 生成自己的密钥（RSA 至少 2048 位，商店不接受 1024 位）：

```bash
keytool -genkeypair -v -keystore app/keystore/release.jks -alias myapp \
        -keyalg RSA -keysize 2048 -validity 10000
```

3. 在根目录 `local.properties` 追加（该文件已被 `.gitignore`）：

```properties
RELEASE_STORE_FILE=app/keystore/release.jks
RELEASE_STORE_PASSWORD=你的store口令
RELEASE_KEY_ALIAS=myapp
RELEASE_KEY_PASSWORD=你的key口令
SIGN_SECRET=接口签名密钥
```

4. 验证：

```bash
./gradlew :app:verifyReleaseSigning    # 应输出「release 使用自有密钥，SHA-256: ...」
./gradlew :app:assembleProdRelease     # 应产出 app-prod-release.apk（无 -unsigned）
```

> 如果第 1 步做了、第 2 步没做，这里会报「仍是模板占位密钥…已中止」，这是**故意设计**的。

5. 把 `release.jks` 和口令放进团队密码管理器 + 一份离线备份。丢了就再也无法更新已上架应用。

6. CI：`release.jks` 做 base64 存进 `RELEASE_KEYSTORE_BASE64`，其余存 Secrets，
见 `.github/workflows/android-ci.yml`。

> debug 密钥（`app/keystore/debug.jks`）**不用动**，随模板继承。它保证所有人签出来是同一个证书，
> 否则同事之间覆盖安装会报 `INSTALL_FAILED_UPDATE_INCOMPATIBLE`，微信/地图/推送也会因签名校验失败而失效。

### 7. 清理示例代码

按需删除，删的时候同步移除 `res/navigation/mobile_navigation.xml` 与
`res/menu/bottom_nav_menu.xml` 里的对应项，否则导航会崩：

| 目录/文件 | 说明 |
|---|---|
| `ui/home/` | 首页示例（Banner + Tab + Paging），建议先留着当参考 |
| `ui/category/` | 列表 + 四态参考实现 |
| `ui/search/` | 搜索页（防抖 + 取消上次请求）。删它要同时去掉首页标题栏的 `ivSearch` 与导航图里的 `searchFragment` |
| `ui/settings/`、`data/settings/` | 设置页 + DataStore 业务键示例。删它要同时去掉「我的」页的设置按钮与 `settingsFragment` |
| `ui/login/`、`ui/register/` | 账号体系示例，连同 `SessionRepository` 一起删 |
| `update/` | APK 升级模块 |
| `data/api/MockApiService.kt` | 有真实后端后删，同时把 flavor 的 `IS_DEBUG_DATA` 置 `false` |

> 删页面**必须同步导航**：`res/navigation/mobile_navigation.xml`（目的地与 action）和
> `res/menu/bottom_nav_menu.xml`（底部项）。只删类不删导航，运行时会
> `IllegalArgumentException: navigation destination ... not found`。

### 8. 收尾检查

```bash
./gradlew clean :app:assembleDevDebug :app:testDevDebugUnitTest
./gradlew detekt spotlessCheck :app:lintDevDebug
./scripts/check-docs.sh                               # 文档引用的代码路径必须真实存在
./scripts/check-encoding.sh                            # 源文件不得带 BOM（会让 @file:Suppress 失效）
./scripts/test-count.sh                               # 用例数以脚本输出为准
git ls-files | grep -E "\.jks|local\.properties"   # 应只有 app/keystore/debug.jks
```

最后对照 [CHECKLIST.md](CHECKLIST.md) 逐条勾一遍。

---

## 二、注意事项（照抄会踩的坑）

### 2.1 包名与身份

| 坑 | 后果 | 正确做法 |
|---|---|---|
| 只改 `namespace` 不改目录 | 编译报 `Unresolved reference: R` 或包名不符 | 目录必须跟着包名一起 `mv` |
| 忘了改 `rootProject.name` | 产物、IDE 工程名、CI 缓存键都还是 TemplateAPP | 改 `settings.gradle.kts` |
| 用模板默认 `applicationId` 直接上架 | 和模板 demo 抢同一个包名，装不上 | 第 2 步必须改 `applicationId` |
| 以为 dev 变体包名就是 `com.template.app.dev` | 实际是 `com.template.app.dev.debug`（debug 多一层后缀） | 见 README「环境与构建」的 6 变体表 |

### 2.2 签名

| 坑 | 后果 | 正确做法 |
|---|---|---|
| 直接沿用模板的 `release.jks` | 这把私钥曾公开在 git 历史里，等于人人可签你的包 | 第 6 步重新生成 |
| 把 `release.jks` 提交进 git | 一旦仓库外泄，应用可被冒名更新 | 它已被 `.gitignore` 忽略，**不要** `git add -f` |
| 把 `debug.jks` 也删了 | 团队每人各签各的，覆盖安装、第三方 SDK 全部失效 | 它是**故意入库**的，保留 |
| 只改 `local.properties` 没关 `template.usePlaceholderReleaseKey` | 换没换密钥无人校验 | 第 6 步先关开关 |

### 2.3 构建与环境

| 坑 | 后果 | 正确做法 |
|---|---|---|
| `local.properties` 缺 `sdk.dir` | Gradle 同步失败 | 用 Android Studio 打开会自动生成，或手写一行 |
| JDK 版本不是 17 | Kotlin 2.0 / AGP 8.7 编译或 KSP 报错 | `Settings → Build Tools → Gradle → Gradle JDK` 选 17 |
| `lint { abortOnError = true }` 撞到新代码的 `NewApi` | release 构建失败（`lintVitalRelease`） | 按提示加 `@RequiresApi` / 用 `values-vNN` 分目录，别关开关 |
| 想关 jetifier | `SmartRefreshLayout` 直接引用 support-v4 类，编译失败 | 保持 `android.enableJetifier=true`，见 `docs/ADR/0004` |
| 新增页面后忘了注册导航 | 点进去白屏 / `IllegalArgumentException: navigation destination not found` | 在 `mobile_navigation.xml` 注册，脚手架脚本最后一行也有提示 |

### 2.4 代码范式（新页面照这个写）

- ViewModel **不要**注入 `ApiService`，走 Repository；新页面直接跑 `./scripts/new-feature.sh <name>`
  （脚本会从 `app/build.gradle.kts` 读 `namespace`，改名后不用改脚本）
- 页面状态只用 `UiState`（`Idle / Loading / Empty / Success / Error`），
  Toast / 导航走 `effects: SharedFlow<UiEffect>`
- Repository 继承 `BaseRepository`，用 `apiCall { safeApi { ... } }`，**不要** `catch (_: Exception) {}`
- Mock 开关是 `BuildConfig.IS_DEBUG_DATA`，`dev` / `staging` 都是 `true`，只有 `prod` 是 `false`；
  别以为"release 就一定连真实接口"

### 2.5 数据库

Room 已开 `exportSchema = true`，`core/schemas/` 里是迁移基线。

- 改表结构必须**升 version + 写 Migration**，禁止加 `fallbackToDestructiveMigration()`（等于删库）
- 新增 Entity 后同步登记到 `AppDatabase.entities` 和 `MIGRATIONS`

---

## 三、常见报错速查

| 报错 | 原因 | 处理 |
|---|---|---|
| `INSTALL_FAILED_UPDATE_INCOMPATIBLE` | 同包名不同签名 | 卸载重装；根治靠统一 debug 密钥 |
| `Missing class ...` / R8 报缺类 | 混淆规则 keep 不精确 | 在 `app/proguard-rules.pro` 按需补 keep，别整包 keep |
| `verifyReleaseSigning` 提示缺少签名配置 | `local.properties` 没配 `RELEASE_*` | 按第 6 步配 |
| `verifyReleaseSigning` 提示仍是占位密钥 | 开关关了，密钥没换 | 生成新密钥 |
| `ProductFlavor names cannot start with 'test'` | flavor 名以 test 开头，AGP 保留字 | 改名（模板把 test 叫 staging 就是这个原因） |
| `Cannot access 'android.support.v4.view.NestedScrollingParent'` | 关掉了 jetifier | 保持为 `true` |
| 构建日志出现 `[signing] 未找到团队 debug 密钥` | `app/keystore/debug.jks` 丢了 | `git checkout app/keystore/debug.jks` |
